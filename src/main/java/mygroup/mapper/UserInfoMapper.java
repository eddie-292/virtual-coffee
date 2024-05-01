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

import mygroup.dto.MyFollowsDTO;
import mygroup.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Eddie
 * @description:用户信息 dao接口，该接口内置了普通的增删查改
 * @date 2024-02-07
 */
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    /**
     * 查询关注列表
     * 列表中剔除自己
     *
     * @param userId
     * @return
     */
    @Select("SELECT " +
            "a.subscriber, a.introduction,a.user_id AS userId,a.nickname,a.username,a.avatar, IF(b.user_id IS NOT NULL,true,false) AS followed " +
            "FROM `user_info` a LEFT JOIN user_follow b ON a.user_id = b.followed_user_id AND b.user_id = #{userId} WHERE a.user_id != #{userId} AND a.member_number > 0 " +
            "ORDER BY a.`register_time` DESC LIMIT 10 ")
    List<UserInfo> whoFollows(long userId);

    // 查询是否关注了某用户
    @Select("SELECT IF(COUNT(*) > 0, true, false) AS followed FROM user_follow WHERE user_id = #{userId} AND followed_user_id = #{followedUserId}")
    boolean followed(long userId, long followedUserId);

    void updatePostCount(Long userId);

    /**
     * 追随者数量 -1
     *
     * @param userId
     */
    void subFollowerCount(Long userId);

    /**
     * 追随者数量 +1
     *
     * @param userId
     */
    void addFollowerCount(Long userId);

    /**
     * 关注数量 -1
     *
     * @param userId
     */
    void subFollowingCount(Long userId);

    /**
     * 关注数量 +1
     *
     * @param userId
     */
    void addFollowingCount(Long userId);

    @Select("select b.user_id userId, b.username, b.avatar from user_follow a LEFT JOIN user_info b ON a.followed_user_id = b.user_id WHERE a.user_id = #{userId} ORDER BY username")
    List<MyFollowsDTO> myFollows(Long userId);

}
