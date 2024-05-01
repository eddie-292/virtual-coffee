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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import mygroup.entity.Team;
import mygroup.entity.UserFavorite;
import mygroup.entity.UserJoinTeamRelation;
import mygroup.mapper.TeamMapper;
import mygroup.mapper.UserFavoriteMapper;
import mygroup.mapper.UserJoinTeamRelationMapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * 加入/退出团队
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Join_Team)
public class JoinTeamConsumer {

    @Autowired
    private UserJoinTeamRelationMapper userJoinTeamRelationMapper;
    @Autowired
    private TeamMapper teamMapper;

    @RabbitHandler
    public void process(String jsonData) {
        Map map = JSON.parseObject(jsonData, Map.class);
        String teamId = map.get("teamId").toString();
        String userId = map.get("userId").toString();

        QueryWrapper<UserJoinTeamRelation> eq = new QueryWrapper<UserJoinTeamRelation>()
                .eq("user_id", userId)
                .eq("team_id", teamId);
        Long count = userJoinTeamRelationMapper.selectCount(eq);
        if (count > 0) {
            Team team = teamMapper.selectById(teamId);
            Long creatorUserId = team.getCreatorUserId();
            if (Objects.equals(creatorUserId, Long.valueOf(userId))) {
                //如果是团队创建者，则不可以退出
                log.info("用户{}尝试退出团队{}，但是该团队是当前用户的，无法退出", userId, teamId);
                return;
            }

            userJoinTeamRelationMapper.delete(eq);

            // 减少团队成员数量
            teamMapper.subMemberCount(Long.valueOf(teamId));
        } else {
            UserJoinTeamRelation userJoinTeamRelation = new UserJoinTeamRelation();
            userJoinTeamRelation.setUserId(Long.valueOf(userId));
            userJoinTeamRelation.setTeamId(Long.valueOf(teamId));
            userJoinTeamRelation.setJoinTime(new Date());
            userJoinTeamRelationMapper.insert(userJoinTeamRelation);

            // 增加团队成员数量
            teamMapper.addMemberCount(Long.valueOf(teamId));
        }
    }

}
