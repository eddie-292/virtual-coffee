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
import java.util.*;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import mygroup.dto.ArticleDoc;
import mygroup.entity.Comments;
import mygroup.entity.Notifications;
import mygroup.entity.UserInfo;
import mygroup.mapper.CommentsMapper;
import mygroup.mapper.NotificationsMapper;
import mygroup.mapper.UserInfoMapper;
import mygroup.service.IUserInfoService;
import mygroup.util.TextContentUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * 评论
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Comment)
public class CommentConsumer {

    public static String TYPE_Article = "Article";

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private CommentsMapper commentsMapper;
    @Autowired
    private NotificationsMapper notificationsMapper;
    @Autowired
    private IUserInfoService userInfoService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @RabbitHandler
    public void process(String jsonData) {

        Map map = JSON.parseObject(jsonData, Map.class);
        Long bizId = Long.valueOf(map.get("bizId").toString());
        Long userId = Long.valueOf(map.get("userId").toString());
        String content = map.get("content").toString();
        String type = map.get("type").toString();
        String title = map.get("title").toString();

        Comments comments = new Comments();
        comments.setUserId(userId);
        comments.setContent(content);
        comments.setPublishTime(new Date());
        comments.setBizId(bizId);
        comments.setLikeCount(0);
        comments.setType(type);

        UserInfo userInfo = null;

        // atUsers返回不为空说明Feed中有@用户，需要发送被@通知
        Set<String> atUsers = TextContentUtils.atUser(comments.getContent());
        List<Long> receiverIds = new ArrayList<>();
        if (null != atUsers && CollectionUtil.isNotEmpty(atUsers)) {
            userInfo = userInfoMapper.selectById(userId);
            //在atUsers中删除自己
            atUsers.remove(userInfo.getUsername());
            if (CollectionUtil.isNotEmpty(atUsers)) {
                String senderAtLink = TextContentUtils.getAtLink(userInfo.getUserId().toString(), ObjectUtils.defaultIfNull(userInfo.getUsername(), userInfo.getNickname()));
                String topicLink = TextContentUtils.getTopicLink(bizId.toString(), title);
                List<UserInfo> userInfos = userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>().in(UserInfo::getUsername, atUsers));
                if (CollectionUtil.isNotEmpty(userInfos)) {
                    //给 @用户 发送通知
                    for (UserInfo atUser : userInfos) {
                        Notifications ntf = new Notifications();
                        ntf.setNotificationId(IdUtil.getSnowflakeNextId());
                        ntf.setSenderId(userId);
                        ntf.setReceiverId(atUser.getUserId());
                        ntf.setContent(String.format("%s 在回复 %s 时提到了你", senderAtLink, topicLink));
                        ntf.setCreatedAt(new Date());
                        ntf.setIsRead(0);
                        if (notificationsMapper.insert(ntf) > 0) {
                            //在缓存中增加接收者的未读通知数
                            userInfoService.addUnreadNotificationCount(ntf.getReceiverId());
                            //记录已经发送通知的用户
                            receiverIds.add(ntf.getReceiverId());
                        }
                    }
                    //发送完通知还需要将content中@用户部分替换为链接
                    for (UserInfo atUser : userInfos) {
                        Long atUserId = atUser.getUserId();
                        String username = atUser.getUsername();
                        content = content.replaceAll("@" + username, TextContentUtils.getAtLink(atUserId.toString(), username));
                    }
                    comments.setContent(content);
                }
            }
        }

        //如果是文章回復，需要給文章作者發送通知
        if (type.equals(TYPE_Article)) {
            //查询文档的作者userId
            Query queryAuthor = new Query(Criteria.where("_id").is(bizId));
            queryAuthor.fields().include("creatorUserId", "title");
            ArticleDoc articleDoc = mongoTemplate.findOne(queryAuthor, ArticleDoc.class, ArticleRechargeConsumer.COLLECTION_NAME);
            Long authorId = articleDoc.getCreatorUserId();

            //如果作者与评论者是同一个人，不再发送通知
            if (!authorId.equals(userId)) {
                if (null == userInfo) {
                    userInfo = userInfoMapper.selectById(userId);
                }
                //如果作者在@用户中，不再发送通知
                if (!receiverIds.contains(authorId)) {
                    String senderAtLink = TextContentUtils.getAtLink(userId.toString(), userInfo.getUsername());
                    String topicLink = TextContentUtils.getTopicLink(articleDoc.getArticleId(), articleDoc.getTitle());
                    Notifications ntf = new Notifications();
                    ntf.setNotificationId(IdUtil.getSnowflakeNextId());
                    ntf.setSenderId(userId);
                    ntf.setReceiverId(authorId);
                    ntf.setContent(String.format("%s 在 %s 中创建了回复", senderAtLink, topicLink));
                    ntf.setCreatedAt(new Date());
                    ntf.setIsRead(0);
                    notificationsMapper.insert(ntf);

                    //在缓存中增加接收者的未读通知数
                    userInfoService.addUnreadNotificationCount(ntf.getReceiverId());
                }
            }
        }

        if (map.containsKey("replyId")) {
            Long replyId = Long.valueOf(map.get("replyId").toString());
            if (null == userInfo) {
                userInfo = userInfoMapper.selectById(userId);
            }
            if (null != userInfo) {
                comments.setReplyId(userInfo.getUserId());
                comments.setReplyUserName(userInfo.getNickname());
            } else {
                log.info("用户不存在");
            }
        }

        if (commentsMapper.insert(comments) > 0) {
            //给回复者发消息
        }

    }

}
