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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import mygroup.entity.Team;
import mygroup.entity.TeamJoinApply;
import mygroup.entity.UserJoinTeamRelation;
import mygroup.mapper.TeamJoinApplyMapper;
import mygroup.mapper.TeamMapper;
import mygroup.mapper.UserJoinTeamRelationMapper;
import mygroup.util.TokenUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:04
 * 加入团队申请
 */
@Slf4j
@Component
@RabbitListener(queues = QueueConstant.Team_Join_Apply)
public class JoinTeamApplyConsumer {

    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private TeamJoinApplyMapper teamJoinApplyMapper;

    @RabbitHandler
    public void process(String jsonData) {
        try {
            Map map = JSON.parseObject(jsonData, Map.class);
            Long teamId = Long.valueOf(map.get("teamId").toString());
            Long applyUserId = Long.valueOf(map.get("applyUserId").toString());

        /*Long count = teamJoinApplyMapper.selectCount(new QueryWrapper<TeamJoinApply>().eq("team_id", teamId).eq("apply_user_id", applyUserId));
        if (count > 0) {
            log.info("该用户已经提交过申请加入该团队");
            return;
        }*/

            Team team = this.teamMapper.selectById(teamId);
            if (team.getIsPublic()) {
                log.info("该团队为公开团队，允许自由加入");
                return;
            }

            TeamJoinApply teamJoinApply = new TeamJoinApply();
            teamJoinApply.setId(IdUtil.getSnowflakeNextId());
            teamJoinApply.setTeamId(teamId);
            teamJoinApply.setApplyUserId(applyUserId);
            teamJoinApply.setApplyTime(new Date());
            teamJoinApply.setTeamCreatorUserId(team.getCreatorUserId());
            teamJoinApply.setProcessResult(0);
            teamJoinApplyMapper.insert(teamJoinApply);
        } catch (Exception e) {
            log.error("接收消息失败", e);
        }

    }

}
