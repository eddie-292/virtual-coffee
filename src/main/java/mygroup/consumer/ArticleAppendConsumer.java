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
import mygroup.config.StarConfig;
import mygroup.dto.ArticleAppendDoc;
import mygroup.dto.ArticleDoc;
import mygroup.util.SummaryExtractor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * 主题追加
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Article_Append)
public class ArticleAppendConsumer {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    private StarConfig starConfig;

    public static final String COLLECTION_NAME = "ArticleAppend";
    public static final String COLLECTION_NAME_P = "Article";

    @RabbitHandler
    public void process(String jsonData) {

        Map map = JSON.parseObject(jsonData, Map.class);
        String articleId = map.get("articleId").toString();
        Long userId = Long.valueOf(map.get("userId").toString());
        String content = map.get("content").toString();
        String platform = map.get("platform").toString();

        //先查询ArticleDoc的appendCount是否小于3
        Criteria criteria1 = Criteria.where("_id").is(articleId).and("creatorUserId").is(userId);
        Query query1 = new Query(criteria1);
        ArticleDoc articleDoc = mongoTemplate.findOne(query1, ArticleDoc.class, COLLECTION_NAME_P);
        if (articleDoc == null || ObjectUtils.defaultIfNull(articleDoc.getAppendCount(), 0) >= 3) {
            log.error("{} 追加失败，追加次数已达上限", articleId);
            return;
        }

        ArticleAppendDoc appendDoc = new ArticleAppendDoc();
        appendDoc.setPlatform(platform);
        appendDoc.setAppendId(IdUtil.getSnowflakeNextIdStr());
        appendDoc.setArticleId(articleId);
        appendDoc.setContent(content);
        appendDoc.setCreateTime(new Date());
        mongoTemplate.save(appendDoc, COLLECTION_NAME);

        //增加附言数
        Criteria criteria = Criteria.where("_id").is(articleId).and("creatorUserId").is(userId);
        Query query = new Query(criteria);
        Update update = new Update()
                .inc("appendCount", 1)
                .set("updateTime", new Date());
        mongoTemplate.upsert(query, update, ArticleDoc.class, COLLECTION_NAME_P);

        // 经济处理
        Map<String, Object> economy = new HashMap<>();
        economy.put("userId", userId);
        economy.put("economy", - starConfig.getPublishArticle());
        economy.put("msgType", UserMsgType.APPEND_ARTICLE);
        economy.put("description", UserMsgType.APPEND_ARTICLE);
        rabbitTemplate.convertAndSend(QueueConstant.Economy, JSON.toJSONString(economy));
    }

}
