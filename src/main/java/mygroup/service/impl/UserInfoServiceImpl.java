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

package mygroup.service.impl;
import java.util.Date;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.constant.QueueConstant;
import mygroup.common.constant.UserMsgType;
import mygroup.common.enums.Permissions;
import mygroup.common.enums.UserRole;
import mygroup.common.enums.AccountStatus;
import mygroup.common.exception.BisException;
import mygroup.common.exception.PermissionDeniedException;
import mygroup.config.StarConfig;
import mygroup.consumer.DailyRewardsConsumer;
import mygroup.dto.*;
import mygroup.entity.Notifications;
import mygroup.entity.UserBadge;
import mygroup.entity.UserInfo;
import mygroup.entity.UserStarRecords;
import mygroup.mapper.NotificationsMapper;
import mygroup.mapper.UserBadgeMapper;
import mygroup.mapper.UserInfoMapper;
import mygroup.mapper.UserStarRecordsMapper;
import mygroup.service.IInvitationCodesService;
import mygroup.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mygroup.service.RoleService;
import mygroup.util.*;
import mygroup.util.pwd.PasswordUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description:用户信息 服务接口实现类
 * @author Eddie
 * @date 2024-02-07
 */
@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
	@Autowired
	private UserInfoMapper userInfoMapper;
	@Autowired
	private UserStarRecordsMapper userStarRecordsMapper;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private AmqpTemplate rabbitTemplate;
	@Autowired
	private StarConfig starConfig;
	@Autowired
	private UserBadgeMapper userBadgeMapper;
	@Autowired
	private NotificationsMapper notificationsMapper;
	@Autowired
	private EmailService emailService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private IInvitationCodesService invitationCodesService;

	@Transactional
	@Override
	public SignUpSuccessDTO signUp(SignupDTO entity) {
		String email = entity.getEmail();
		String password = entity.getPassword();
		boolean agree = entity.isAgree();
		String invitationCode = entity.getInvitationCode();

		if (!agree) {
			throw new BisException("使用本网站前须同意用户服务协议与隐私条款", ReqUtil.id(request));
		}
		if (StringUtils.isAnyBlank(email, password)) {
			throw new BisException("未填写完整信息", ReqUtil.id(request));
		}
		if (!EmailValidator.isValidEmail(email)) {
			throw new BisException("无效的邮箱或不是来自于常用服务商", ReqUtil.id(request));
		}
		// 邀请码是否开启(1开启,0关闭)
		Object invitationEnable = WebsiteConfigUtil.getWebsiteConfig("website.invitation");
		if (Integer.valueOf(ObjectUtils.defaultIfNull(invitationEnable, 0).toString()) == 1) {
			if (StringUtils.isBlank(invitationCode)) {
				throw new BisException("邀请码不能为空", ReqUtil.id(request));
			}
			if (!invitationCodesService.queryInvitationCode(invitationCode)) {
				throw new BisException("邀请码错误", ReqUtil.id(request));
			}
		}
		if (this.baseMapper.selectCount(new QueryWrapper<UserInfo>().eq("email", email)) > 0) {
			throw new BisException("该邮箱已被注册", ReqUtil.id(request));
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setUserId(IdUtil.getSnowflakeNextId());
		userInfo.setEmail(entity.getEmail());
		userInfo.setRegisterTime(new Date());
		userInfo.setPostCount(0);
		userInfo.setFollowerCount(0);
		userInfo.setFollowingCount(0);
		userInfo.setPassword(PasswordUtils.encode(entity.getPassword()));
		userInfo.setAccountStatus(AccountStatus.UNFINISHED.getCode());
		userInfo.setUserRole(UserRole.MEMBER.getCode());
		userInfo.setInvitationCode(invitationCode);
		this.baseMapper.insert(userInfo);

		String tokenId = IdUtil.fastSimpleUUID();
		redisTemplate.opsForValue().set(tokenId, userInfo.getUserId().toString(), 7, TimeUnit.DAYS);

		SignUpSuccessDTO signUpSuccessDTO = new SignUpSuccessDTO();
		signUpSuccessDTO.setAccountStatus(AccountStatus.UNFINISHED.getCode());
		signUpSuccessDTO.setUserId(userInfo.getUserId());
		signUpSuccessDTO.setToken(tokenId);
		signUpSuccessDTO.setEmail(userInfo.getEmail());
		return signUpSuccessDTO;
	}

	@Override
	public SigninSuccessDTO signIn(SigninDTO entity) {
		String email = entity.getEmail();
		String password = entity.getPassword();
		boolean agree = entity.isAgree();
		if (!agree) {
			throw new BisException("使用本网站前须同意用户服务协议与隐私条款", ReqUtil.id(request));
		}

		if (StringUtils.isAnyBlank(email, password)) {
			throw new BisException("未填写完整信息", ReqUtil.id(request));
		}

		List<UserInfo> userInfos = this.baseMapper.selectList(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, email));
		if (CollectionUtil.isEmpty(userInfos)) {
			throw new BisException("登录信息错误", ReqUtil.id(request));
		}

		UserInfo userInfo = userInfos.get(0);

		if (!PasswordUtils.matches(password, userInfo.getPassword())) {
			throw new BisException("登录信息错误", ReqUtil.id(request));
		}

		String tokenId = IdUtil.fastSimpleUUID();
		redisTemplate.opsForValue().set(tokenId, userInfo.getUserId().toString(), 7, TimeUnit.DAYS);

		SigninSuccessDTO signinSuccessDTO = updateMyProfile(userInfo.getUserId());
		BeanUtil.copyProperties(userInfo, signinSuccessDTO);
		signinSuccessDTO.setToken(tokenId);
		signinSuccessDTO.setSubscriberMark(ObjectUtils.defaultIfNull(userInfo.getSubscriber(), 0) > 0);
		return signinSuccessDTO;
	}

	@Override
	public SigninSuccessDTO signUpComplete(SignUpCompleteDTO entity) {
		String avatar = entity.getAvatar();
		String nickname = entity.getNickname().trim();
		String emailCode = entity.getEmailCode();
		if (StringUtils.isAnyBlank(avatar, nickname, emailCode)) {
			throw new BisException("未填写完整信息(昵称和头像)", ReqUtil.id(request));
		}

		Set<String> checked = NicknameUtils.checkNickname(nickname);
		if (CollectionUtil.isNotEmpty(checked)) {
			String errMsg = String.join("@", checked);
			throw new BisException(errMsg);
		}

		Long userId = TokenUtil.userId(request);
		UserInfo userInfo = this.baseMapper.selectById(userId);
		if (userInfo == null) {
			throw new BisException("用户不存在", ReqUtil.id(request));
		}

		//验证邮箱验证码
		if (!emailService.verifyEmailCode(userInfo.getEmail(), emailCode)) {
			throw new BisException("邮箱验证码错误", ReqUtil.id(request));
		}

		userInfo.setAvatar(avatar);
		userInfo.setNickname(nickname);
		userInfo.setUsername(nickname);
		userInfo.setUpdateTime(new Date());
		userInfo.setAccountStatus(AccountStatus.FINISHED.getCode());
		userInfo.setMemberNumber(Math.toIntExact(redisTemplate.opsForValue().increment(GetKey.getMemberNumberKey())));
		userInfo.setEconomy(BigDecimal.valueOf(starConfig.getRegister())); // 初始资金??星星

		try {
			if (this.baseMapper.updateById(userInfo) > 0) {
				//保存消息
				Map<String, Object> hashMap = new HashMap<>();
				hashMap.put("userId", userId);
				hashMap.put("msgType", UserMsgType.REGISTER_REWARD);
				hashMap.put("amount", starConfig.getRegister());
				hashMap.put("type", "add");
				hashMap.put("description", UserMsgType.REGISTER_REWARD);
				rabbitTemplate.convertAndSend(QueueConstant.User_Star_Msg, JSON.toJSONString(hashMap));
			}

			//邀请码使用次数+1
			invitationCodesService.setInvitationCodeUsed(userInfo.getInvitationCode());

			SigninSuccessDTO signinSuccessDTO = new SigninSuccessDTO();
			BeanUtil.copyProperties(userInfo, signinSuccessDTO);

			//防止未注册完成时登录产生的缓存
			String userKey = GetKey.getPyProfileKey(userId);
			redisTemplate.delete(userKey);

			//删除验证码
			emailService.deleteEmailCode(userInfo.getEmail());
			return signinSuccessDTO;
		} catch (DuplicateKeyException e) {
			if (e.getMessage().contains("user_info.username_idx")) {
				throw new BisException("用户名已存在", ReqUtil.id(request));
			}
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		throw new BisException("注册未能完成", ReqUtil.id(request));
	}

	@Override
	public void signOut() {
		String tokenId = TokenUtil.tokenId(request);
		if (StringUtils.isNotBlank(tokenId)) {
			redisTemplate.delete(tokenId);
		}
	}

	@Override
	public SigninSuccessDTO updateInfo(UserInfo entity) {
		Long userId = TokenUtil.userId(request);
		UserInfo userInfo = this.baseMapper.selectById(userId);
		if (userInfo == null) {
			throw new BisException("用户不存在", ReqUtil.id(request));
		}

		//个人简介不能超过 300 个字符
		String introduction = entity.getIntroduction();
		if (StringUtils.isNotBlank(introduction)) {
			introduction = introduction.trim();
			if (introduction.length() > 300) {
				throw new BisException("个人简介不能超过 300 个字符");
			}
		}

		//昵称不能超过15个字符
		String nickname = entity.getNickname();
		if (StringUtils.isBlank(nickname)) {
			throw new BisException("用户名不能为空");
		}
		nickname = nickname.trim();
		Set<String> checked = NicknameUtils.checkNickname(nickname);
		if (CollectionUtil.isNotEmpty(checked)) {
			String errMsg = String.join("@", checked);
			throw new BisException(errMsg);
		}

		String city = entity.getCity();
		if (StringUtils.isNotBlank(city)) {
			city = city.trim();
			if (city.length() > 30) {
				throw new BisException("所在城市不能超过 30 个字符");
			}
		}
		String occupation = entity.getOccupation();
		if (StringUtils.isNotBlank(occupation)) {
			occupation = occupation.trim();
			if (occupation.length() > 40) {
				throw new BisException("职业不能超过 40 个字符");
			}
		}

		//手机号码不能超过 11 个字符
		String phoneNumber = entity.getPhoneNumber();
		if (StringUtils.isNotBlank(phoneNumber)) {
			phoneNumber = phoneNumber.trim();
			if (phoneNumber.length() > 11) {
				throw new BisException("手机号码不能超过 11 个字符");
			}
		}

		//username为空时才能修改并且用户名只能修改一次
		if (StringUtils.isBlank(userInfo.getUsername())) {
			//if (!userInfo.getNickname().equalsIgnoreCase(nickname)) {
			//}
			userInfo.setNickname(nickname);
			userInfo.setUsername(nickname);
		}
		userInfo.setBirthday(entity.getBirthday());
		userInfo.setCity(entity.getCity());
		userInfo.setOccupation(entity.getOccupation());
		userInfo.setEmotionalStatus(entity.getEmotionalStatus());
		userInfo.setPhoneNumber(entity.getPhoneNumber());
		userInfo.setIntroduction(introduction);
		userInfo.setAvatar(entity.getAvatar());
		userInfo.setHomeBackgroundImage(entity.getHomeBackgroundImage());
		userInfo.setUpdateTime(new Date());
		this.baseMapper.updateById(userInfo);

		SigninSuccessDTO signinSuccessDTO = updateMyProfile(userInfo.getUserId());
		BeanUtil.copyProperties(userInfo, signinSuccessDTO);
		return signinSuccessDTO;
	}

	@Override
	public List<UserInfo> whoFollows() {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			userId = 0L;
		}
		List<UserInfo> userInfos = baseMapper.whoFollows(userId);
		return userInfos;
	}

	@Override
	public void follow(String followedPeople) {
		Long userId = TokenUtil.userId(request);
		if (String.valueOf(userId).equals(followedPeople)) {
			return;
		}

		if (null != userId) {
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("followedPeople", followedPeople);
			rabbitTemplate.convertAndSend(QueueConstant.User_Follow, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public SigninSuccessDTO myProfile(Long userId) {
		String userKey = GetKey.getPyProfileKey(userId);
		Object userObj = redisTemplate.opsForValue().get(userKey);
		if (userObj != null) {
			return JSON.parseObject(userObj.toString(), SigninSuccessDTO.class) ;
		}

		UserInfo userInfo = this.baseMapper.selectById(userId);
		if (userInfo == null) {
			String tokenId = TokenUtil.tokenId(request);
			if (StringUtils.isNotBlank(tokenId)) {
				redisTemplate.delete(tokenId);
			}
			throw new BisException("用户不存在", ReqUtil.id(request));
		}

		SigninSuccessDTO signinSuccessDTO = new SigninSuccessDTO();
		BeanUtil.copyProperties(userInfo, signinSuccessDTO);
		signinSuccessDTO.setSubscriberMark(ObjectUtils.defaultIfNull(userInfo.getSubscriber(), 0) > 0);
		signinSuccessDTO.setDailyRewards(redisTemplate.hasKey(DailyRewardsConsumer.getKey(userId)));//是否领取了每日奖励
		redisTemplate.opsForValue().set(userKey, JSON.toJSONString(signinSuccessDTO), 1, TimeUnit.HOURS);
		return signinSuccessDTO;
	}

	@Override
	public SigninSuccessDTO updateMyProfile(Long userId) {
		String userKey = GetKey.getPyProfileKey(userId);
		redisTemplate.delete(userKey);

		return myProfile(userId);
	}

	@Override
	public SigninSuccessDTO googleLogin(GoogleUserInfo googleUserInfo) {
		String email = googleUserInfo.getEmail();
		String picture = googleUserInfo.getPicture();
		String name = googleUserInfo.getName();
		Date date = new Date();

		UserInfo userInfo = this.baseMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, email));
		if (userInfo == null) {
			userInfo = new UserInfo();
			userInfo.setUserId(IdUtil.getSnowflakeNextId());
			userInfo.setEmail(email);
			userInfo.setRegisterTime(date);
			userInfo.setUpdateTime(date);
			userInfo.setPostCount(0);
			userInfo.setFollowerCount(0);
			userInfo.setFollowingCount(0);
			userInfo.setAvatar(picture);
			userInfo.setNickname(name);
			userInfo.setCity(googleUserInfo.getLocale());
			userInfo.setPassword(PasswordUtils.encode(email));
			userInfo.setAccountStatus(AccountStatus.FINISHED.getCode());
			this.baseMapper.insert(userInfo);
		}


		String tokenId = IdUtil.fastSimpleUUID();
		redisTemplate.opsForValue().set(tokenId, userInfo.getUserId().toString(), 7, TimeUnit.DAYS);

		SigninSuccessDTO signinSuccessDTO = updateMyProfile(userInfo.getUserId());
		BeanUtil.copyProperties(userInfo, signinSuccessDTO);
		signinSuccessDTO.setToken(tokenId);
		return signinSuccessDTO;
	}

	@Override
	public SigninSuccessDTO updateUserImage(UserInfo entity) {
		Long userId = TokenUtil.userId(request);
		UserInfo userInfo = this.baseMapper.selectById(userId);
		if (userInfo == null) {
			throw new BisException("用户不存在", ReqUtil.id(request));
		}

		userInfo.setUpdateTime(new Date());

		if (StringUtils.isNotBlank(entity.getAvatar())) {
			userInfo.setAvatar(entity.getAvatar());
			this.baseMapper.updateById(userInfo);
		}

		if (StringUtils.isNotBlank(entity.getHomeBackgroundImage())) {
			userInfo.setHomeBackgroundImage(entity.getHomeBackgroundImage());
			this.baseMapper.updateById(userInfo);
		}

		SigninSuccessDTO signinSuccessDTO = updateMyProfile(userInfo.getUserId());
		BeanUtil.copyProperties(userInfo, signinSuccessDTO);
		return signinSuccessDTO;
	}

	@Override
	public Double dailyRewards() {
		Long userId = TokenUtil.userId(request);
		if (null != userId) {
			String key = DailyRewardsConsumer.getKey(userId);
			if (!redisTemplate.hasKey(key)) {
				Map<String, Object> hashMap = new HashMap<>();
				hashMap.put("userId", userId);
				hashMap.put("number", starConfig.getDailyRewards());
				rabbitTemplate.convertAndSend(QueueConstant.Daily_Rewards, JSON.toJSONString(hashMap));
			} else {
				log.info("用户{}今天已经领取过奖励", userId);
			}
		}
		return starConfig.getDailyRewards();
	}

	@Override
	public boolean checkEconomy(Long userId) {
		return checkEconomy(userId, 1D);
	}

	@Override
	public boolean checkEconomy(Long userId, Double amount) {
		UserInfo userInfo = userInfoMapper.selectById(userId);
		if (null == userInfo) {
			log.error("用户不存在");
			return false;
		}

		if (userInfo.getEconomy().compareTo(new BigDecimal(amount)) < 0) {
			log.error("用户{}的星星不足", userId);
			return false;
		}

		return true;
	}

	@Override
	public boolean followed(long userId, long followedUserId) {
		return userInfoMapper.followed(userId, followedUserId);
	}

	@Override
	public void updateUserPwd(UserPwd userPwd) {
		Long userId = TokenUtil.userId(request);
		if (null != userId) {
			UserInfo userInfo = userInfoMapper.selectById(userId);
			if (PasswordUtils.matches(userPwd.getCurrentPwd(), userInfo.getPassword())) {
				userInfo.setPassword(PasswordUtils.encode(userPwd.getNewPwd()));
				userInfoMapper.updateById(userInfo);
			} else {
				throw new BisException("旧密码错误", ReqUtil.id(request));
			}
		}
	}

	@Override
	public IPage<UserStarRecords> starsRecord(Integer p) {
		Long userId = TokenUtil.userId(request);
		int pageSize = 30;
		Page<UserStarRecords> page = new Page<>(p, pageSize);
		QueryWrapper<UserStarRecords> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_id", userId);
		queryWrapper.orderByDesc("time");
		IPage<UserStarRecords> userStarRecordsPage = userStarRecordsMapper.selectPage(page, queryWrapper);
		return userStarRecordsPage;
	}

	@Override
	public List<UserBadge> badges(String uId) {
		if (StringUtils.isBlank(uId) || !NumberUtil.isNumber(uId)) {
			return new ArrayList<>();
		}

		QueryWrapper<UserBadge> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_id", uId);
		queryWrapper.orderByAsc("get_time");
		List<UserBadge> badges = userBadgeMapper.selectList(queryWrapper);
		return badges;
	}

	@Override
	public IPage<Notifications> getNotifications(Long userId, Integer p) {
		if (null != userId) {
			//receiver_id = 0表示所有人可见
			Page<Notifications> page = new Page<>(p, 10);
			QueryWrapper<Notifications> queryWrapper = new QueryWrapper<>();
			queryWrapper.in("receiver_id", Arrays.asList(userId, 0));
			queryWrapper.orderByDesc("created_at");
			IPage<Notifications> notificationsIPage = notificationsMapper.selectPage(page, queryWrapper);
			return notificationsIPage;
		}
		return new Page<Notifications>();
	}

	@Override
	public void addNotifications(NotificationsDTO notifications) {
		Long userId = TokenUtil.userId(request);
		if (null != userId) {
			if (!this.hasPermission(userId, Permissions.POST_NOTICE.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}

			String title = notifications.getTitle();
			String content = notifications.getContent();
			List<String> receiverIds = notifications.getReceiverIds();
			if (CollectionUtil.isEmpty(receiverIds)) {
				receiverIds = new ArrayList<>();
				receiverIds.add("0");
			}
			for (String receiverId : receiverIds) {
				Notifications ntf = new Notifications();
				ntf.setNotificationId(IdUtil.getSnowflakeNextId());
				ntf.setSenderId(userId);
				ntf.setReceiverId(Long.valueOf(receiverId));
				ntf.setTitle(title);
				ntf.setContent(content);
				ntf.setCreatedAt(new Date());
				ntf.setIsRead(0);
				if (notificationsMapper.insert(ntf) > 0) {
					if (ntf.getReceiverId() == 0) {
						//给所有人发送通知
						List<UserInfo> userInfos = userInfoMapper.selectList(
								new LambdaQueryWrapper<UserInfo>()
										.select(UserInfo::getUserId)
										.ne(UserInfo::getUserId, userId)
										.eq(UserInfo::getAccountStatus, AccountStatus.FINISHED.getCode()));
						for (UserInfo userInfo : userInfos) {
							this.addUnreadNotificationCount(userInfo.getUserId());
						}
					} else {
						//给指定用户发送通知
						this.addUnreadNotificationCount(ntf.getReceiverId());
					}
				}
			}
		}
	}

	@Override
	public IPage<UserInfo> getMembers(Integer p) {
		Long userId = TokenUtil.userId(request);
		if (!this.hasPermission(userId, Permissions.POST_NOTICE.getCode())) {
			return new Page<>();
		}

		Page<UserInfo> page = new Page<>(p, 20);
		LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(UserInfo::getAccountStatus, AccountStatus.FINISHED.getCode());
		queryWrapper.select(UserInfo::getUserId,UserInfo::getNickname,UserInfo::getAvatar, UserInfo::getMemberNumber);
		queryWrapper.orderByAsc(UserInfo::getMemberNumber);
		IPage<UserInfo> users = userInfoMapper.selectPage(page, queryWrapper);
		return users;
	}

	@Override
	public Boolean hasPermission(Long userId, String permissionCode) {
		List<Map<String, String>> permissionList = this.permissions(userId);
		List<String> permissionCodeList = permissionList.stream().map(map -> map.get("permission_code")).collect(Collectors.toList());

		if (permissionCodeList.contains(Permissions.ALL_PERMISSIONS.getCode())) {
			return true;
		}

		return permissionCodeList.contains(permissionCode);
	}

	@Override
	public List<Map<String, String>> permissions(Long userId) {
		if (null == userId) {
			return new ArrayList<>();
		}

		return roleService.getPermissionListByUserId(userId);
	}

	@Override
	public void addUnreadNotificationCount(Long receiverId) {
		String key = GetKey.getUnreadNotificationCountKey(receiverId);
		redisTemplate.opsForValue().increment(key);
	}

	@Override
	public Integer unreadNoticeCount(Long userId) {
		String key = GetKey.getUnreadNotificationCountKey(userId);
		Object count = redisTemplate.opsForValue().get(key);
		if (null == count) {
			return 0;
		}
		return Integer.valueOf(count.toString());
	}

	@Override
	public Integer unreadNoticeCountClear(Long userId) {
		String key = GetKey.getUnreadNotificationCountKey(userId);
		redisTemplate.delete(key);
		return 0;
	}

	@Override
	public List<MyFollowsDTO> myFollows() {
		return userInfoMapper.myFollows(TokenUtil.userId(request));
	}

	@Override
	public void setLastPostInGroup(String teamId, Long userId, String username, Date date) {
		String key = GetKey.getLastPostInGroupKey(teamId);
		LastPostInGroupDTO lastPostInGroupDTO = new LastPostInGroupDTO();
		lastPostInGroupDTO.setUserId(String.valueOf(userId));
		lastPostInGroupDTO.setUsername(username);
		lastPostInGroupDTO.setTime(date.getTime());
		redisTemplate.opsForValue().set(key, JSON.toJSONString(lastPostInGroupDTO));
	}

	@Override
	public SigninSuccessDTO updateUserPrivate(UserInfo entity) {
		Long userId = TokenUtil.userId(request);
		UserInfo userInfo = this.baseMapper.selectById(userId);
		if (userInfo == null) {
			throw new BisException("用户不存在", ReqUtil.id(request));
		}

		userInfo.setPublicFeeds(entity.getPublicFeeds());
		userInfo.setPublicTopics(entity.getPublicTopics());
		this.baseMapper.updateById(userInfo);

		SigninSuccessDTO signinSuccessDTO = updateMyProfile(userInfo.getUserId());
		BeanUtil.copyProperties(userInfo, signinSuccessDTO);
		return signinSuccessDTO;
	}

	@Override
	public boolean feedOpen(Long userId) {
		UserInfo userInfo = this.baseMapper.selectById(userId);
		return userInfo.getPublicFeeds() == 1;
	}

	@Override
	public boolean topicOpen(Long userId) {
		UserInfo userInfo = this.baseMapper.selectById(userId);
		return userInfo.getPublicTopics() == 1;
	}

}
