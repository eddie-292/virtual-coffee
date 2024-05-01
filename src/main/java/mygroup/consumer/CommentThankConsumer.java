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

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import mygroup.common.constant.UserMsgType;
import mygroup.common.exception.BisException;
import mygroup.dto.ArticleDoc;
import mygroup.dto.StarTransfer;
import mygroup.entity.Comments;
import mygroup.entity.Notifications;
import mygroup.entity.UserInfo;
import mygroup.mapper.CommentsMapper;
import mygroup.mapper.NotificationsMapper;
import mygroup.mapper.UserInfoMapper;
import mygroup.service.IUserInfoService;
import mygroup.util.GetKey;
import mygroup.util.TextContentUtils;
import org.apache.commons.lang3.ObjectUtils;
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
 * 评论感谢
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Comment_Like)
public class CommentThankConsumer {

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
    private CommentsMapper commentsMapper;
    @Autowired
    private NotificationsMapper notificationsMapper;

    @RabbitHandler
    public void process(String jsonData) {
        String starTransferKey = null;
        StarTransfer starTransfer = null;
        String commentId = null;
        Long userId = null;
        Comments comments = null;
        Long senderUserId = null;
        Long receiverUserId = null;
        String senderUserName = null;

        try {
            Map map = JSON.parseObject(jsonData, Map.class);
            commentId = map.get("commentId").toString();
            userId = Long.valueOf(map.get("userId").toString());

            //查询评论的作者userId
            comments = commentsMapper.selectById(commentId);
            if (null == comments) {
                log.info("评论不存在");
                return;
            }
            Long authorId = comments.getUserId();

            //获取交易
            starTransferKey = GetKey.getStarTransferKey(userId);
            if (!redisTemplate.hasKey(starTransferKey)) {
                log.info("用户[{}]没有交易", userId);
                return;
            }

            Object starTransferObj = redisTemplate.opsForValue().get(starTransferKey);
            starTransfer = JSON.parseObject(starTransferObj.toString(), StarTransfer.class);
            starTransfer.setReceiverId(authorId); //设置接收者

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
            String receiverUserName = userInfo.getNickname();
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
            senderMsg.put("msgType", UserMsgType.THANK);
            senderMsg.put("amount", - Double.valueOf(1));
            senderMsg.put("type", "sub");
            senderMsg.put("description", "你 " + UserMsgType.THANK + " " + memberNameTag(receiverUserId, receiverUserName));
            starMsgList.add(senderMsg);
            // ====================================== 接收者保存用户星星记录
            Map<String, Object> receiverMsg = new HashMap<>();
            receiverMsg.put("userId", starTransfer.getReceiverId());
            receiverMsg.put("msgType", UserMsgType.THANK);
            receiverMsg.put("amount", Double.valueOf(1));
            receiverMsg.put("type", "add");
            receiverMsg.put("description", "收到 " + memberNameTag(senderUserId, senderUserName) + " " + UserMsgType.THANK);
            starMsgList.add(receiverMsg);
            for (Map<String, Object> msg : starMsgList) {
                rabbitTemplate.convertAndSend(QueueConstant.User_Star_Msg, JSON.toJSONString(msg));
            }

        } catch (Exception ignored) {

        } finally {
            if (null != starTransfer) {
                if (starTransfer.isSenderSent() && starTransfer.isReceiverReceived()) {
                    // ====================================== 给Feed新增经济数量
                    comments = commentsMapper.selectById(commentId);
                    comments.setLikeCount(ObjectUtils.defaultIfNull(comments.getLikeCount(), 0) + 1);
                    if (commentsMapper.updateById(comments) <= 0) {
                        log.info("给评论点赞数更新失败");
                    } else {
                        log.info("用户[{}]感谢了[{}]", userId, commentId);

                        //给接收者发送通知
                        if (senderUserId != null) {
                            String senderAtLink = TextContentUtils.getAtLink(senderUserId.toString(), senderUserName);
                            Notifications ntf = new Notifications();
                            ntf.setNotificationId(IdUtil.getSnowflakeNextId());
                            ntf.setSenderId(senderUserId);
                            ntf.setReceiverId(starTransfer.getReceiverId());
                            ntf.setContent(String.format("%s %s了你的回复", senderAtLink, UserMsgType.THANK));
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
            redisTemplate.delete(starTransferKey);
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
