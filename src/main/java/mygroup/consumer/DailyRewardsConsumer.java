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

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import mygroup.common.constant.UserMsgType;
import mygroup.dto.ArticleDoc;
import mygroup.entity.UserInfo;
import mygroup.mapper.UserInfoMapper;
import mygroup.service.IUserInfoService;
import mygroup.util.GetKey;
import mygroup.util.SummaryExtractor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * 每日奖励
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Daily_Rewards)
public class DailyRewardsConsumer {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate rabbitTemplate;

    public static String getKey(Long userId) {
        //年月日
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        return "dailyRewards:" + date + ":" + userId;
    }

    @RabbitHandler
    public void process(String jsonData) {

        try {
            Map map = JSON.parseObject(jsonData, Map.class);
            Long userId = Long.valueOf(map.get("userId").toString());
            Double number = Double.valueOf(map.get("number").toString());
            String key = DailyRewardsConsumer.getKey(userId);

            if (redisTemplate.hasKey(key)) {
                log.error("用户{}今日已领取奖励", userId);
                return;
            }

            UserInfo userInfo = userInfoMapper.selectById(userId);
            if (null == userInfo) {
                log.error("用户不存在");
                return;
            }

            // 新增经济处理
            Map<String, Object> economy = new HashMap<>();
            economy.put("userId", userId);
            economy.put("economy", number);
            economy.put("msgType", UserMsgType.DAILY_REWARDS);
            economy.put("description", UserMsgType.DAILY_REWARDS);
            rabbitTemplate.convertAndSend(QueueConstant.Economy, JSON.toJSONString(economy));

            redisTemplate.opsForValue().set(key, userId.toString(), 1, TimeUnit.DAYS);

            // 删除个人信息缓存
            redisTemplate.delete(GetKey.getPyProfileKey(userId));

            // 保存消息
//            Map<String, Object> hashMap = new HashMap<>();
//            hashMap.put("userId", userId);
//            hashMap.put("msgType", UserMsgType.DAILY_REWARDS);
//            hashMap.put("amount", number);
//            hashMap.put("type", "add");
//            hashMap.put("description", UserMsgType.DAILY_REWARDS);
//            rabbitTemplate.convertAndSend(QueueConstant.User_Star_Msg, JSON.toJSONString(hashMap));

        } catch (Exception e) {
            log.error("daily rewards error", e);
        }

    }

}
