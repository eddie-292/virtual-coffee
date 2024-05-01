/*
 * Copyright Statement and License Information for Virtual Coffee Kafeih.com Community
 *
 * Copyright Owner：Kafeih.com Community and its contributors, since the inception of the project.
 *
 * License Type：All code, documentation, and design works related to the Kafeih.com Community are licensed under the GNU Affero General Public License (AGPL) v3 or any later version.
 *
 * Use and Distribution：You are free to use, copy, modify, and distribute the code, documentation, and design works of the Kafeih.com Community, subject to the following conditions:
 *
 * 1. You must include the original copyright and license notices in all copies distributed or made publicly available.
 * 2. If you modify the code or design, or derive new works from those provided by the community, you must release these modifications or derivative works under the terms of the AGPLv3 license.
 * 3. Important Note: If you use the code or design of this community to provide network services, you must ensure that all users accessing the service through the network can access the corresponding source code.
 *
 * No Warranty：The Kafeih.com Community and its code, documentation, and design works are provided "as is" without any warranty, including but not limited to warranties of merchantability, fitness for a particular purpose, and non-infringement.
 *
 * License Acquisition：The complete text of the GNU Affero General Public License (AGPL) v3 can be found on the GNU official website.
 *
 * Please note that the above statement only applies to the Kafeih.com Community and the code, documentation, and design works provided by it. Third-party links or resources may be subject to different licenses from their respective owners or publishers. When using these resources, please be sure to comply with the terms of their respective licenses.
 */

package mygroup.consumer;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import mygroup.common.constant.UserMsgType;
import mygroup.common.exception.BisException;
import mygroup.dto.CodeSnippetDoc;
import mygroup.dto.StarTransfer;
import mygroup.entity.Notifications;
import mygroup.entity.UserInfo;
import mygroup.mapper.NotificationsMapper;
import mygroup.mapper.UserInfoMapper;
import mygroup.service.IUserInfoService;
import mygroup.util.GetKey;
import mygroup.util.TextContentUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * 给Feed充电
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Code_Snippet_Recharge)
public class FeedsRechargeConsumer {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    private IUserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private NotificationsMapper notificationsMapper;
    public static final String COLLECTION_NAME = "CodeSnippet";

    @RabbitHandler
    public void process(String jsonData) {
        String starTransferKey = null;
        StarTransfer starTransfer = null;
        String docId = null;
        Long userId = null;
        String senderUserName = null;
        Long senderUserId = null;
        String receiverUserName = null;
        Long receiverUserId = null;

        try {
            Map map = JSON.parseObject(jsonData, Map.class);
            docId = map.get("id").toString();
            userId = Long.valueOf(map.get("userId").toString());

            //查询代码块CodeSnippetDoc的作者userId
            Query queryAuthor = new Query(Criteria.where("_id").is(docId));
            queryAuthor.fields().include("userId");
            CodeSnippetDoc codeSnippetDoc = mongoTemplate.findOne(queryAuthor, CodeSnippetDoc.class, COLLECTION_NAME);
            Long authorId = codeSnippetDoc.getUserId();

            //获取交易
            starTransferKey = GetKey.getStarTransferKey(userId);
            if (!redisTemplate.hasKey(starTransferKey)) {
                log.info("用户[{}]没有交易", userId);
                return;
            }

            Object starTransferObj = redisTemplate.opsForValue().get(starTransferKey);
            starTransfer = JSON.parseObject(starTransferObj.toString(), StarTransfer.class);
            starTransfer.setReceiverId(authorId); //设置接收者

            //如果发送者和接受者是同一人则停止执行
            if (starTransfer.getSenderId().equals(starTransfer.getReceiverId())) {
                log.info("用户[{}]和[{}]是同一个人，交易结束", starTransfer.getSenderId(), starTransfer.getReceiverId());
                return;
            }

            //校验用户是否存在
            List<UserInfo> userInfos = userInfoMapper.selectBatchIds(ListUtil.toList(starTransfer.getSenderId(), starTransfer.getReceiverId()));
            if (userInfos.size() != 2) {
                throw new BisException("用户不存在");
            }

            // ===================================== 判断发送者是否经济不足
            if (!userInfoService.checkEconomy(starTransfer.getSenderId())) {
                log.info("用户[{}]星星不足，交易结束", starTransfer.getSenderId());
                throw new BisException("星星不足");
            }
            // ===================================== 判断发送者是否经济不足


            // ====================================== 给发送者扣除经济处理
            UserInfo userInfo = userInfoMapper.selectById(starTransfer.getSenderId());
            if (null == userInfo) {
                log.error("用户不存在");
                throw new BisException("用户不存在");
            }
            senderUserName = userInfo.getNickname();
            senderUserId = userInfo.getUserId();
            BigDecimal defaultVal = ObjectUtils.defaultIfNull(userInfo.getEconomy(), BigDecimal.ZERO);
            BigDecimal economyResult = defaultVal.subtract(BigDecimal.ONE);
            userInfo.setEconomy(economyResult);
            int i = userInfoMapper.updateById(userInfo);
            if (i != 1) {
                log.error("更新用户[{}]经济失败", userId);
                throw new BisException("更新用户经济失败");
            }
            starTransfer.setSenderSent(true);
            log.info("用户[{}]扣除1个星星", userId);
            // ====================================== 给发送者扣除经济处理


            // ====================================== 给作者新增经济处理
            userInfo = userInfoMapper.selectById(starTransfer.getReceiverId());
            if (null == userInfo) {
                log.error("用户不存在");
                throw new BisException("用户不存在");
            }
            receiverUserName = userInfo.getNickname();
            receiverUserId = userInfo.getUserId();
            defaultVal = ObjectUtils.defaultIfNull(userInfo.getEconomy(), BigDecimal.ZERO);
            economyResult = defaultVal.add(BigDecimal.ONE);
            userInfo.setEconomy(economyResult);
            int i1 = userInfoMapper.updateById(userInfo);
            if (i1 != 1) {
                log.error("更新用户[{}]经济失败", authorId);
                throw new BisException("更新用户经济失败");
            }
            starTransfer.setReceiverReceived(true);
            log.info("用户[{}]给作者[{}]充值了1个星星", userId, authorId);
            // ====================================== 给作者新增经济处理


            // ====================================== 发送者保存用户星星记录
            List<Map<String, Object>> starMsgList = new ArrayList<>();
            Map<String, Object> senderMsg = new HashMap<>();
            senderMsg.put("userId", starTransfer.getSenderId());
            senderMsg.put("msgType", UserMsgType.REWARD);
            senderMsg.put("amount", - Double.valueOf(1));
            senderMsg.put("type", "sub");
            senderMsg.put("description", "你 " + UserMsgType.REWARD + " " + memberNameTag(receiverUserId, receiverUserName));
            starMsgList.add(senderMsg);
            // ====================================== 接收者保存用户星星记录
            Map<String, Object> receiverMsg = new HashMap<>();
            receiverMsg.put("userId", starTransfer.getReceiverId());
            receiverMsg.put("msgType", UserMsgType.REWARD);
            receiverMsg.put("amount", Double.valueOf(1));
            receiverMsg.put("type", "add");
            receiverMsg.put("description", "收到 " + memberNameTag(senderUserId, senderUserName) + " " + UserMsgType.REWARD);
            starMsgList.add(receiverMsg);
            for (Map<String, Object> msg : starMsgList) {
                rabbitTemplate.convertAndSend(QueueConstant.User_Star_Msg, JSON.toJSONString(msg));
            }

        } catch (Exception ignored) {

        } finally {
            if (null != starTransfer) {
                if (starTransfer.isSenderSent() && starTransfer.isReceiverReceived()) {
                    // ====================================== 给Feed新增经济数量
                    Criteria criteria = Criteria.where("_id").is(docId);
                    Query query = new Query(criteria);
                    Update update = new Update().inc("batteryLevel", 1);
                    UpdateResult result = mongoTemplate.upsert(query, update, CodeSnippetDoc.class, COLLECTION_NAME);
                    if (result.getMatchedCount() <= 0) {
                        log.info("Feed充电失败，没有匹配到任何数据");
                    } else {
                        log.info("用户[{}]给代码块[{}]充电了", userId, docId);

                        //给接收者发送通知
                        if (senderUserId != null) {
                            String senderAtLink = TextContentUtils.getAtLink(senderUserId.toString(), senderUserName);
                            Notifications ntf = new Notifications();
                            ntf.setNotificationId(IdUtil.getSnowflakeNextId());
                            ntf.setSenderId(senderUserId);
                            ntf.setReceiverId(starTransfer.getReceiverId());
                            ntf.setContent(String.format("%s %s了你的Feed", senderAtLink, UserMsgType.REWARD));
                            ntf.setCreatedAt(new Date());
                            ntf.setIsRead(0);
                            if (notificationsMapper.insert(ntf) > 0) {
                                //在缓存中增加接收者的未读通知数
                                userInfoService.addUnreadNotificationCount(ntf.getReceiverId());
                            }
                        }
                    }
                    // ====================================== 给Feed新增经济数量

                    // 删除个人信息缓存
                    redisTemplate.delete(GetKey.getPyProfileKey(starTransfer.getSenderId()));
                    redisTemplate.delete(GetKey.getPyProfileKey(starTransfer.getReceiverId()));
                }
            }

            if (StringUtils.isNotBlank(starTransferKey)) {
                redisTemplate.delete(starTransferKey);
            }
        }

    }

    public static String memberNameTag(Long userId, String name) {
        return "<a target='_blank' href=\"/member/" + userId + "\">" + name + "</a>";
    }

    public static void main(String[] args) {
        //System.out.println(memberNameTag(123123L, "AAA") + " " + UserMsgType.REWARD + " " + memberNameTag(222222L, "BBBB"));
        //System.out.println("你 " + UserMsgType.REWARD + " " + memberNameTag(123123L, "AAA"));
        //System.out.println("收到 " + memberNameTag(123123L, "AAA") + " " + UserMsgType.REWARD);
    }
}
