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

package mygroup.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import mygroup.dto.TeamJoinApplyResult;
import mygroup.entity.ResourceUploadRecord;
import mygroup.entity.TeamJoinApply;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Eddie.BaoZeBing
 * @date 2024/3/23 21:20
 */
public interface TeamJoinApplyMapper extends BaseMapper<TeamJoinApply> {

    @Select("\n" +
            "SELECT \n" +
            "a.id, a.apply_time applyTime, a.apply_user_id applyUserId, b.nickname, b.avatar, c.team_name teamName \n" +
            "\n" +
            "FROM team_join_apply a \n" +
            "LEFT JOIN user_info b ON b.user_id = a.apply_user_id \n" +
            "LEFT JOIN team c ON a.team_id = c.team_id\n" +
            "\n" +
            "WHERE a.process_result = 0 AND a.team_creator_user_id = #{userId} ORDER BY a.apply_time")
    List<TeamJoinApplyResult> list(Long userId);

}
