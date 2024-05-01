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

import com.alibaba.fastjson2.JSON;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * 文章删除
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Article_Del)
public class ArticleDeleteConsumer {

    @Autowired
    private MongoTemplate mongoTemplate;
    public static final String COLLECTION_NAME = "Article";

    @RabbitHandler
    public void process(String jsonData) {
        Map map = JSON.parseObject(jsonData, Map.class);
        String articleId = map.get("articleId").toString();
        String userId = map.get("userId").toString();

        Criteria criteria = Criteria.where("_id").is(articleId);
        criteria.and("creatorUserId").is(Long.valueOf(userId));
        criteria.and("batteryLevel").lte(0); // 电量小于等于0的才能删除

        Query query = new Query(criteria);
        DeleteResult deleteResult = mongoTemplate.remove(query, COLLECTION_NAME);
        if (deleteResult.getDeletedCount() > 0) {
            log.info("删除Topic成功，id:{} userId:{}", articleId, userId);
        } else {
            log.info("删除Topic失败，id:{} userId:{}", articleId, userId);
        }
    }

}
