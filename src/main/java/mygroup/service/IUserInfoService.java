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

package mygroup.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import mygroup.dto.*;
import mygroup.dto.common.ConditionQuery;
import mygroup.dto.common.PageQuery;
import mygroup.entity.Notifications;
import mygroup.entity.UserBadge;
import mygroup.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import mygroup.entity.UserStarRecords;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description:用户信息 服务接口定义
 * @author Eddie
 * @date 2024-02-07
 */
public interface IUserInfoService extends IService<UserInfo> {

    SignUpSuccessDTO signUp(SignupDTO entity);

    SigninSuccessDTO signIn(SigninDTO signinDTO);

    SigninSuccessDTO signUpComplete(SignUpCompleteDTO entity);

    void signOut();

    SigninSuccessDTO updateInfo(UserInfo entity);

    List<UserInfo> whoFollows();

    void follow(String userId);

    SigninSuccessDTO myProfile(Long userId);

    SigninSuccessDTO updateMyProfile(Long userId);

    SigninSuccessDTO googleLogin(GoogleUserInfo googleUserInfo);

    SigninSuccessDTO updateUserImage(UserInfo entity);

    Double dailyRewards();

    boolean checkEconomy(Long userId);

    boolean checkEconomy(Long userId, Double amount);

    boolean followed(long userId, long followedUserId);

    void updateUserPwd(UserPwd userPwd);

    IPage<UserStarRecords> starsRecord(Integer p);

    List<UserBadge> badges(String uId);

    IPage<Notifications> getNotifications(Long userId, Integer p);

    void addNotifications(NotificationsDTO notifications);

    IPage<UserInfo> getMembers(Integer p);

    Boolean hasPermission(Long userId, String permissionCode);

    List<Map<String, String>> permissions(Long userId);

    void addUnreadNotificationCount(Long receiverId);

    Integer unreadNoticeCount(Long userId);

    Integer unreadNoticeCountClear(Long userId);

    List<MyFollowsDTO> myFollows();

    void setLastPostInGroup(String teamId, Long userId, String username, Date date);

    SigninSuccessDTO updateUserPrivate(UserInfo entity);

    boolean feedOpen(Long userId);

    boolean topicOpen(Long userId);

}
