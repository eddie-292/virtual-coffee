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

package mygroup.controller;

import java.util.List;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import mygroup.common.annotation.ApiDesc;
import mygroup.common.annotation.Free;
import mygroup.common.annotation.RequestLimit;
import mygroup.common.enums.Permissions;
import mygroup.dto.*;
import mygroup.entity.UserBadge;
import mygroup.entity.UserStarRecords;
import mygroup.util.TokenUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


import mygroup.dto.common.ConditionQuery;
import mygroup.dto.common.PageQuery;
import mygroup.dto.common.RestData;
import mygroup.entity.UserInfo;
import mygroup.service.IUserInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import javax.servlet.http.HttpServletRequest;

/**
 * @description:用户信息控制器
 * @author Eddie
 * @date 2024-02-07
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {
	@Autowired
	private IUserInfoService userInfoService;
	@Autowired
	private HttpServletRequest request;

	/**
	 * 每日奖励
	 * @return
	 */
	@RequestLimit(second = 10)
	@PostMapping("/daily-rewards")
	public RestData<Double> dailyRewards() {
		return RestData.success(userInfoService.dailyRewards());
	}

	/**
	 * 关注用户
	 * @param userId
	 * @return
	 */
	@RequestLimit
	@PostMapping("/follow/{userId}")
	public RestData<String> follow(@PathVariable("userId") String userId) {
		userInfoService.follow(userId);
		return RestData.success();
	}

	/**
	 * 关注谁列表
	 * @return
	 */
	@Free
	@GetMapping("/who-follows")
	public RestData<List<UserInfo>> whoFollows() {
		List<UserInfo> follow = userInfoService.whoFollows();
		return RestData.success(follow);
	}

	/**
	 * 我关注的人列表
	 * @return
	 */
	@GetMapping("/my-follows")
	public RestData<List<MyFollowsDTO>> myFollows() {
		List<MyFollowsDTO> follow = userInfoService.myFollows();
		return RestData.success(follow);
	}

	/**
	 * 用户的徽章
	 * @param uId
	 * @return
	 */
	@Free
	@GetMapping("/badges")
	public RestData<List<UserBadge>> badges(String uId) {
		List<UserBadge> badges = userInfoService.badges(uId);
		return RestData.success(badges);
	}

	@ApiDesc(value = "星星记录")
	@GetMapping("/stars-record")
	public RestData<IPage<UserStarRecords>> starsRecord(String p) {
		if (StringUtils.isBlank(p) || !NumberUtil.isNumber(p)) {
			p = "1";
		}
		IPage<UserStarRecords> list = userInfoService.starsRecord(Integer.valueOf(p));
		return RestData.success(list);
	}

	/**
	 * @description:退出登录
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@ApiDesc(value = "退出登录")
	@PostMapping("/sign-out")
	public RestData<String> signOut() {
		userInfoService.signOut();
		return RestData.success();
	}
	
	/**
 	* @description:注册
    * @author Eddie
    * @date 2024-02-07
    */
	@RequestLimit
	@PostMapping("/sign-up")
	public RestData<SignUpSuccessDTO> signUp(@RequestBody SignupDTO entity) {
		SignUpSuccessDTO signUpSuccessDTO = userInfoService.signUp(entity);
		return RestData.success(signUpSuccessDTO);
	}

	/**
	 * @description:继续完成注册
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@RequestLimit
	@ApiDesc(value = "继续完成注册")
	@PostMapping("/sign-up-complete")
	public RestData<SigninSuccessDTO> signUpComplete(@RequestBody SignUpCompleteDTO entity) {
		SigninSuccessDTO signinSuccessDTO = userInfoService.signUpComplete(entity);
		return RestData.success(signinSuccessDTO);
	}
	
	/**
 	* @description:登录
    * @author Eddie
    * @date 2024-02-07
    */
	@ApiDesc(value = "登录")
	@PostMapping("/sign-in")
	public RestData<SigninSuccessDTO> signIn(@RequestBody SigninDTO signinDTO) {
		SigninSuccessDTO signinSuccessDTO = userInfoService.signIn(signinDTO);
		return RestData.success(signinSuccessDTO);
	}
	
	/**
 	* @description:修改用户信息
    * @author Eddie
    * @date 2024-02-07
    */
	@ApiDesc(value = "修改用户信息")
	@PostMapping("/info/update")
	public RestData<SigninSuccessDTO> updateUserInfoInfoByKey(@RequestBody UserInfo entity) {
		SigninSuccessDTO signinSuccessDTO = userInfoService.updateInfo(entity);
		return RestData.success(signinSuccessDTO);
	}

	/**
	 * @description:修改用户配置
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@ApiDesc(value = "修改用户配置")
	@PostMapping("/private/update")
	public RestData<SigninSuccessDTO> updateUserPrivate(@RequestBody UserInfo entity) {
		SigninSuccessDTO signinSuccessDTO = userInfoService.updateUserPrivate(entity);
		return RestData.success(signinSuccessDTO);
	}

	/**
 	* @description:修改用户密码
    * @author Eddie
    * @date 2024-02-07
    */
	@ApiDesc(value = "修改用户密码")
	@PostMapping("/pwd/update")
	public RestData<String> updateUserPwd(@RequestBody UserPwd userPwd) {
		userInfoService.updateUserPwd(userPwd);
		return RestData.success();
	}

	/**
	 * @description:修改用户头像
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@ApiDesc(value = "修改用户头像/背景图片")
	@PostMapping("/info/update-image")
	public RestData<SigninSuccessDTO> updateUserImage(@RequestBody UserInfo entity) {
		SigninSuccessDTO signinSuccessDTO = userInfoService.updateUserImage(entity);
		return RestData.success(signinSuccessDTO);
	}

	/**
 	* @description:按照主键查询用户信息
    * @author Eddie
    * @date 2024-02-07
    */
	@Free
	@ApiDesc(value = "按照主键查询用户信息")
	@GetMapping("/info/{id}")
	public RestData<SigninSuccessDTO> getUserInfoInfoByKey(@PathVariable String id) {
		if (StringUtils.isBlank(id) || !NumberUtil.isNumber(id)) {
			return RestData.error("参数错误");
		}

		UserInfo userInfo = userInfoService.getById(id);
		if (userInfo == null) {
			return RestData.error("用户不存在");
		}

		SigninSuccessDTO signinSuccessDTO = new SigninSuccessDTO();
		BeanUtil.copyProperties(userInfo, signinSuccessDTO);
		signinSuccessDTO.setSubscriberMark(ObjectUtils.defaultIfNull(userInfo.getSubscriber(), 0) > 0);

		Long userId = TokenUtil.userId(request);
		if (null != userId) {
			boolean followed = userInfoService.followed(userId, userInfo.getUserId());
			signinSuccessDTO.setFollowed(followed);
		}

		signinSuccessDTO.setEconomy(null);
		signinSuccessDTO.setEmail(null);
		signinSuccessDTO.setEmotionalStatus(null);
		signinSuccessDTO.setBirthday(null);
		signinSuccessDTO.setUpdateTime(null);
		signinSuccessDTO.setAccountStatus(null);
		signinSuccessDTO.setPhoneNumber(null);

		return RestData.success(signinSuccessDTO);
	}

	/**
	 * @description:获取自己的数据
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@Free
	@ApiDesc(value = "获取自己的数据")
	@GetMapping("/my-profile")
	public RestData<SigninSuccessDTO> myProfile() {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			return RestData.success();
		}

		SigninSuccessDTO signinSuccessDTO = userInfoService.myProfile(userId);
		if (signinSuccessDTO == null) {
			return RestData.error("用户不存在");
		}
		return RestData.success(signinSuccessDTO);
	}

	/**
	 * 获取权限列表
	 */
	@Free
	@ApiDesc(value = "获取权限列表")
	@GetMapping("/permissions")
	public RestData<List<Map<String, String>>> permissions() {
		List<Map<String, String>> permissions = userInfoService.permissions(TokenUtil.userId(request));
		return RestData.success(permissions);
	}


	@ApiDesc(value = "获取用户未读通知数")
	@GetMapping("/unread-notice-count")
	public RestData<Integer> unreadNoticeCount() {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			return RestData.success(0);
		}
		return RestData.success(userInfoService.unreadNoticeCount(userId));
	}

	@ApiDesc(value = "消除用户未读通知数")
	@PostMapping("/unread-notice-count/clear")
	public RestData<Integer> unreadNoticeCountClear() {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			return RestData.success(0);
		}
		return RestData.success(userInfoService.unreadNoticeCountClear(userId));
	}

}
