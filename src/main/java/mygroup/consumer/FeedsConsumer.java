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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.extern.slf4j.Slf4j;
import mygroup.algorithm.tools.NaiveBayesClassifierTool;
import mygroup.common.constant.QueueConstant;
import mygroup.common.constant.UserMsgType;
import mygroup.config.StarConfig;
import mygroup.dto.ArticleDoc;
import mygroup.dto.CodeSnippetDoc;
import mygroup.entity.CodeSnippet;
import mygroup.entity.Notifications;
import mygroup.entity.Team;
import mygroup.entity.UserInfo;
import mygroup.mapper.*;
import mygroup.service.IUserInfoService;
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
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * Feed
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Code_Snippet)
public class FeedsConsumer {

    @Autowired
    private IUserInfoService userInfoService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private StarConfig starConfig;
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    private NotificationsMapper notificationsMapper;
    @Autowired
    private UserJoinTeamRelationMapper userJoinTeamRelationMapper;
    @Autowired
    private NaiveBayesClassifierTool tool;


    public static final String COLLECTION_NAME = "CodeSnippet";
    public static final String Article_COLLECTION_NAME = "Article";

    @RabbitHandler
    public void process(String jsonData) {

        Map map = JSON.parseObject(jsonData, Map.class);
        String docId = IdUtil.getSnowflakeNextIdStr();
        String teamId = ObjectUtils.defaultIfNull(map.get("teamId"), "").toString();
        String platform = map.get("platform").toString();

        CodeSnippet codeSnippet = new CodeSnippet();
        codeSnippet.setUserId(Long.valueOf(map.get("userId").toString()));
        codeSnippet.setCreateTime(new Date());
        codeSnippet.setBatteryLevel(0);
        codeSnippet.setCodeContentId(docId);

        if (!userInfoService.checkEconomy(codeSnippet.getUserId(), starConfig.getPublishCode())) {
            log.error("{} 发布Feed失败，用户余额不足", codeSnippet.getUserId());
            return;
        }

        if (StringUtils.isNotBlank(teamId)) {
            Set<Long> teamIds = userJoinTeamRelationMapper.getTeamId(codeSnippet.getUserId());
            if (!teamIds.contains(Long.valueOf(teamId))) {
                log.error("{} 发布Feed失败，用户不在团队中", codeSnippet.getUserId());
                return;
            }
        }

        CodeSnippetDoc codeSnippetDoc = new CodeSnippetDoc();
        BeanUtil.copyProperties(codeSnippet, codeSnippetDoc);
        codeSnippetDoc.setPlatform(platform);
        codeSnippetDoc.setId(docId);
        codeSnippetDoc.setContent(map.get("content").toString());
        codeSnippetDoc.setPosition(ObjectUtils.defaultIfNull(map.get("position"), "home").toString());
        codeSnippetDoc.setTeamId(teamId);
        codeSnippetDoc.setBatteryLevel(0);
        codeSnippetDoc.setSpam(tool.spam(codeSnippetDoc.getContent()) ? 1 : 0);

        //用戶发帖数加1
        userInfoMapper.updatePostCount(codeSnippet.getUserId());
        //更新用户信息缓存
        userInfoService.updateMyProfile(codeSnippet.getUserId());

        Team team = null;
        if (StringUtils.isNotBlank(teamId) && NumberUtil.isNumber(teamId)) {
            //更新团队帖子数
            teamMapper.updatePostCount(Long.valueOf(teamId));
            //如果在私有组中发布内容则需要将feed标记为私有
            team = teamMapper.selectById(teamId);
            if (null != team && !team.getIsPublic()) {
                codeSnippetDoc.setPrivateGroup(1);
            }
        }

        //发送者
        UserInfo userInfo = userInfoMapper.selectById(codeSnippetDoc.getUserId());

        // atUsers返回不为空说明Feed中有@用户，需要发送被@通知
        Set<String> atUsers = TextContentUtils.atUser(codeSnippetDoc.getContent());
        if (null != atUsers && CollectionUtil.isNotEmpty(atUsers)) {
            String senderAtLink = TextContentUtils.getAtLink(userInfo.getUserId().toString(), ObjectUtils.defaultIfNull(userInfo.getUsername(), userInfo.getNickname()));
            String ntfContent = String.format("%s 在Feed中提到了你", senderAtLink);

            //在atUsers中删除自己
            atUsers.remove(userInfo.getUsername());
            //如果实在Group中发的则需要在内容中加上团队连接
            if (StringUtils.isNotBlank(teamId)) {
                if (null != team) {
                    String groupLink = TextContentUtils.getGroupLink(teamId, team.getTeamName());
                    ntfContent = String.format("%s 在 %s Group中发布Feed并且提到了你", senderAtLink, groupLink);
                }
            }
            if (CollectionUtil.isNotEmpty(atUsers)) {

                List<UserInfo> userInfos = userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>().in(UserInfo::getUsername, atUsers));
                if (CollectionUtil.isNotEmpty(userInfos)) {
                    //给 @用户 发送通知
                    for (UserInfo atUser : userInfos) {
                        Notifications ntf = new Notifications();
                        ntf.setNotificationId(IdUtil.getSnowflakeNextId());
                        ntf.setSenderId(codeSnippet.getUserId());
                        ntf.setReceiverId(atUser.getUserId());
                        ntf.setContent(ntfContent);
                        ntf.setCreatedAt(new Date());
                        ntf.setIsRead(0);
                        notificationsMapper.insert(ntf);

                        //在缓存中增加接收者的未读通知数
                        userInfoService.addUnreadNotificationCount(ntf.getReceiverId());
                    }
                    //发送完通知还需要将content中@用户部分替换为链接
                    String content = codeSnippetDoc.getContent();
                    for (UserInfo atUser : userInfos) {
                        Long atUserId = atUser.getUserId();
                        String username = atUser.getUsername();
                        content = content.replaceAll("@" + username, TextContentUtils.getAtLink(atUserId.toString(), username));
                    }
                    codeSnippetDoc.setContent(content);
                }
            }
        }

        //关联主题
        if (map.containsKey("articleIds")) {
            List<String> articleIds = Arrays.asList(map.get("articleIds").toString().split(","));
            //List<String> 转 List<Long>
            List<Long> articleIdList = new ArrayList<>();
            articleIds.forEach(articleId -> articleIdList.add(Long.valueOf(articleId)));
            //从mongodb 中查询文章
            Query query = Query.query(Criteria.where("_id").in(articleIdList));
            query.addCriteria(Criteria.where("creatorUserId").is(codeSnippet.getUserId()));
            List<ArticleDoc> articleDocs = mongoTemplate.find(query, ArticleDoc.class, Article_COLLECTION_NAME);
            List<Map<String, String>> articleJsons = new ArrayList<>();
            for (ArticleDoc articleDoc : articleDocs) {
                Map<String, String> article = new HashMap<>();
                article.put("id", articleDoc.getArticleId());
                article.put("title", articleDoc.getTitle());
                articleJsons.add(article);
            }
            codeSnippetDoc.setArticles(JSON.toJSONString(articleJsons));
        }
        mongoTemplate.save(codeSnippetDoc, COLLECTION_NAME); //存在就执行更新

        //在redis中记录此group中最后发言的人和时间
        userInfoService.setLastPostInGroup(teamId, userInfo.getUserId(), userInfo.getUsername(), new Date());

        //经济处理
        Map<String, Object> economy = new HashMap<>();
        economy.put("userId", codeSnippet.getUserId());
        economy.put("economy", - starConfig.getPublishCode());
        economy.put("msgType", UserMsgType.PUBLISH_FEED);
        economy.put("description", UserMsgType.PUBLISH_FEED);
        rabbitTemplate.convertAndSend(QueueConstant.Economy, JSON.toJSONString(economy));
    }

    public static void main(String[] args) {
        String asd = "阿斯顿看哈来上课的哈说了很多阿斯顿了卡收到了@eddie 阿里收到啦收到啦@baozebing asdhkasjhdkg ";
        Set<String> user = TextContentUtils.atUser(asd);
        System.out.println(user);
        //String atLink = String.format(" <strong><a class='link-success link-underline link-underline-opacity-0' target='_blank' href='/member/%s'>@%s</a></strong> ", "1231312312", "eddie");
        //System.out.println(asd.replaceAll(atPattern, atLink));
    }

}
