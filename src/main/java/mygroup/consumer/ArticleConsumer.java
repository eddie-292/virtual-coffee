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
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import mygroup.common.constant.UserMsgType;
import mygroup.config.StarConfig;
import mygroup.dto.ArticleDoc;
import mygroup.dto.CodeSnippetDoc;
import mygroup.entity.CodeSnippet;
import mygroup.entity.UserInfo;
import mygroup.mapper.CodeSnippetMapper;
import mygroup.mapper.UserInfoMapper;
import mygroup.service.IUserInfoService;
import mygroup.util.SummaryExtractor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * 文章新增
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Article_Add)
public class ArticleConsumer {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private StarConfig starConfig;
    @Autowired
    private IUserInfoService userInfoService;
    @Autowired
    private AmqpTemplate rabbitTemplate;
    public static final String COLLECTION_NAME = "Article";

    @RabbitHandler
    public void process(String jsonData) {

        Map map = JSON.parseObject(jsonData, Map.class);
        String docId = map.get("articleId").toString();
        String platform = map.get("platform").toString();

        Long userId = Long.valueOf(map.get("userId").toString());
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (null == userInfo) {
            log.error("用户不存在");
            return;
        }

        if (!userInfoService.checkEconomy(userInfo.getUserId(), starConfig.getPublishArticle())) {
            log.error("{} 发布文章失败，用户余额不足", userInfo.getUserId());
        }

        if (map.get("title") == null || map.get("content") == null || map.get("category") == null ) {
            return;
        }

        ArticleDoc articleDoc = new ArticleDoc();
        articleDoc.setPlatform(platform);
        articleDoc.setArticleId(docId);
        articleDoc.setCreatorUserId(userId);
        articleDoc.setCreatorUsername(userInfo.getNickname());
        articleDoc.setCreateTime(new Date());
        articleDoc.setTitle(map.get("title").toString());
        articleDoc.setContent(map.get("content").toString());
        articleDoc.setCategory(map.get("category").toString());
        articleDoc.setCategoryTitle(map.get("categoryTitle").toString());
        articleDoc.setType(Integer.valueOf(map.get("type").toString()));
        articleDoc.setSummary(SummaryExtractor.extractSummary(articleDoc.getContent()));
        articleDoc.setBatteryLevel(0);
        if (map.get("mainImageUrl") != null) {
            articleDoc.setMainImageUrl(map.get("mainImageUrl").toString());
        }
        mongoTemplate.save(articleDoc, COLLECTION_NAME); //存在就执行更新*/
        log.info("新增文章:{},ID:{}", articleDoc.getTitle(), articleDoc.getArticleId());

        // 经济处理
        Map<String, Object> economy = new HashMap<>();
        economy.put("userId", userId);
        economy.put("economy", - starConfig.getPublishArticle());
        economy.put("msgType", UserMsgType.PUBLISH_ARTICLE);
        economy.put("description", UserMsgType.PUBLISH_ARTICLE);
        rabbitTemplate.convertAndSend(QueueConstant.Economy, JSON.toJSONString(economy));
    }

}
