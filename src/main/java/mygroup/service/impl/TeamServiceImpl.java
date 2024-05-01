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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import mygroup.common.constant.QueueConstant;
import mygroup.common.constant.UserMsgType;
import mygroup.common.enums.Permissions;
import mygroup.common.exception.BisException;
import mygroup.common.exception.PermissionDeniedException;
import mygroup.config.StarConfig;
import mygroup.dto.LastPostInGroupDTO;
import mygroup.dto.TeamJoinApplyResult;
import mygroup.dto.common.ConditionQuery;
import mygroup.dto.common.PageQuery;
import mygroup.entity.*;
import mygroup.mapper.*;
import mygroup.service.ITeamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mygroup.service.IUserInfoService;
import mygroup.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:团队 服务接口实现类
 * @author Eddie
 * @date 2024-02-07
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements ITeamService {

	@Autowired
	private TeamMapper teamMapper;
	@Autowired
	private TeamLinkMapper teamLinkMapper;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private AmqpTemplate rabbitTemplate;
	@Autowired
	private UserJoinTeamRelationMapper userJoinTeamRelationMapper;
	@Autowired
	private UserFavoriteMapper userFavoriteMapper;
	@Autowired
	private TeamJoinApplyMapper teamJoinApplyMapper;
	@Autowired
	private StarConfig starConfig;
	@Autowired
	private IUserInfoService userInfoService;
	@Autowired
	private SensitiveFilter sensitiveFilter;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public IPage<Team> queryByPage(PageQuery pageQuery) {
		IPage<Team> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

		QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
		queryWrapper.orderByDesc(StringUtils.defaultIfEmpty(pageQuery.getOrderBy(), "daily_post_count"));
		IPage<Team> teamIPage = teamMapper.selectPage(page, queryWrapper);

		Long userId = TokenUtil.userId(request);
		if (userId != null) {
			Set<Long> teamIds = userJoinTeamRelationMapper.getTeamId(userId);
			for (Team record : teamIPage.getRecords()) {
				record.setJoin(teamIds.contains(record.getTeamId()));
			}
		}

		for (Team record : teamIPage.getRecords()) {
			if (!record.getIsPublic()) {
				continue;
			}

			//获取最后发言人
			String key = GetKey.getLastPostInGroupKey(String.valueOf(record.getTeamId()));
			Object lpg = redisTemplate.opsForValue().get(key);
			if (lpg != null) {
				LastPostInGroupDTO lastPostInGroupDTO = JSON.parseObject(lpg.toString(), LastPostInGroupDTO.class);
				long time = lastPostInGroupDTO.getTime();
				//计算time距离现在的时间
				lastPostInGroupDTO.setTimeDesc(TimeDescUtil.get(time));
				record.setLastPost(lastPostInGroupDTO);
			}
		}

		return teamIPage;
	}

	@Override
	public List<Team> queryByCondition(ConditionQuery conditionQuery) {
		return null;
	}

	@Override
	public void saveTeamInfo(Team entity) {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			throw new BisException("请先登录");
		}

		if (!userInfoService.hasPermission(userId, Permissions.CREATE_GROUP.getCode())) {
			throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
		}

		if (!userInfoService.checkEconomy(userId, starConfig.getCreateGroup())) {
			throw new BisException("星星不足");
		}

		if (StringUtils.isBlank(entity.getTeamName())) {
			throw new BisException("请填写组名称");
		}

		if (entity.getTeamName().length() > 20) {
			throw new BisException("组名称不能大于20个字符");
		}

		if (sensitiveFilter.exist(entity.getTeamName())) {
			throw new BisException("组名称包含敏感词", ReqUtil.id(request));
		}

		if (StringUtils.isBlank(entity.getTeamImageUrl())) {
			throw new BisException("请上传组图片");
		}

		if (StringUtils.isBlank(entity.getHomeBackgroundImage())) {
			throw new BisException("请上传背景横幅图片");
		}

		//组名称
		if (StringUtils.isNotBlank(entity.getTeamDescription())) {
			if (entity.getTeamDescription().length() > 300) {
				throw new BisException("组描述不能大于300个字符");
			}

			if (sensitiveFilter.exist(entity.getTeamDescription())) {
				throw new BisException("组描述包含敏感词", ReqUtil.id(request));
			}
		}

		entity.setTeamId(IdUtil.getSnowflakeNextId());
		entity.setCreatorUserId(userId);
		entity.setCreateTime(new Date());
		entity.setUpdateTime(new Date());
		entity.setTeamMemberCount(0);
		entity.setDailyPostCount(0);

		if (this.teamMapper.insert(entity) > 0) {

			// 自动加入团队
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("teamId", entity.getTeamId());
			rabbitTemplate.convertAndSend(QueueConstant.Join_Team, JSON.toJSONString(hashMap));

			// 经济处理
			Map<String, Object> economy = new HashMap<>();
			economy.put("userId", userId);
			economy.put("economy", - starConfig.getCreateGroup());
			economy.put("msgType", UserMsgType.CREATE_GROUP);
			economy.put("description", UserMsgType.CREATE_GROUP);
			rabbitTemplate.convertAndSend(QueueConstant.Economy, JSON.toJSONString(economy));
		}

	}

	@Override
	public void removeTeamInfoByKey(List<String> ids) {
		this.teamMapper.deleteBatchIds(ids);
	}

	@Override
	public void updateTeamInfoByKey(Team entity) {
		Team old = this.teamMapper.selectById(entity.getTeamId());
		if (old == null) {
			return;
		}

		old.setTeamName(entity.getTeamName());
		old.setTeamImageUrl(entity.getTeamImageUrl());
		old.setHomeBackgroundImage(entity.getHomeBackgroundImage());
		old.setIsPublic(entity.getIsPublic());
		old.setTeamDescription(entity.getTeamDescription());
		old.setUpdateTime(new Date());
		this.teamMapper.updateById(old);
	}

	@Override
	public void joinTeam(Team entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("teamId", entity.getTeamId());
			rabbitTemplate.convertAndSend(QueueConstant.Join_Team, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public Team get(String id) {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			return null;
		}

		Team old = this.teamMapper.selectById(id);
		if (old == null) {
			return null;
		}

		Set<Long> teamIds = userJoinTeamRelationMapper.getTeamId(userId);
		if (CollectionUtil.isNotEmpty(teamIds)) {
			old.setJoin(teamIds.contains(old.getTeamId()));
		}
		return old;
	}

	@Override
	public List<Team> newListLimit(int limit) {
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
		queryWrapper.orderByDesc("create_time");
		queryWrapper.last("limit " + limit);
		List<Team> teams = this.teamMapper.selectList(queryWrapper);
		return teams;
	}

	@Override
	public void favorite(Team entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("otherId", entity.getTeamId());
			rabbitTemplate.convertAndSend(QueueConstant.User_Favorite, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public Team getTeam(String id) {
		Team team = this.teamMapper.selectById(id);
		if (team == null) {
			return null;
		}
		
		Long userId = TokenUtil.userId(request);

		if (userId != null) {
			//查询我的收藏信息
			QueryWrapper<UserFavorite> eq = new QueryWrapper<UserFavorite>()
					.eq("user_id", userId)
					.eq("other_id", id);
			Long count = userFavoriteMapper.selectCount(eq);
			team.setFavorite(count > 0);

			//查询我是否加入了
			QueryWrapper<UserJoinTeamRelation> eq2 = new QueryWrapper<UserJoinTeamRelation>()
					.eq("user_id", userId)
					.eq("team_id", team.getTeamId());
			Long count2 = userJoinTeamRelationMapper.selectCount(eq2);
			team.setJoin(count2 > 0);

			//链接
			List<TeamLink> teamLinks = teamLinkMapper.selectList(new LambdaQueryWrapper<TeamLink>().eq(TeamLink::getTeamId, id));
			teamLinks.sort(Comparator.comparing(TeamLink::getId).reversed());
			team.setTeamLinks(teamLinks);

		}

		return team;
	}

	@Override
	public void joinApply(Team entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId && null != entity.getTeamId()) {
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("applyUserId", userId);
			hashMap.put("teamId", entity.getTeamId());
			rabbitTemplate.convertAndSend(QueueConstant.Team_Join_Apply, JSON.toJSONString(hashMap));
		}

	}

	@Override
	public List<TeamJoinApplyResult> joinApplyList(Long userId) {
		return teamJoinApplyMapper.list(userId);
	}

	@Override
	public void joinApplyReview(TeamJoinApply req) {
		Integer processResult = req.getProcessResult();

		// 0 或者 1
		if (processResult != 2 && processResult != 1) {
			throw new BisException("处理错误");
		}

		TeamJoinApply teamJoinApply = teamJoinApplyMapper.selectById(req.getId());
		teamJoinApply.setProcessResult(processResult);
		teamJoinApply.setProcessTime(new Date());
		teamJoinApplyMapper.updateById(teamJoinApply);

		if (processResult == 1) {
			// 审核通过
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", teamJoinApply.getApplyUserId());
			hashMap.put("teamId", teamJoinApply.getTeamId());
			rabbitTemplate.convertAndSend(QueueConstant.Join_Team, JSON.toJSONString(hashMap));
		}


	}

	@Override
	public List<Team> moveList() {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			return Collections.emptyList();
		}

		if (!userInfoService.hasPermission(userId, Permissions.MOVE_FEED.getCode())) {
			throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
		}

		return teamMapper.selectList(
				new LambdaQueryWrapper<Team>()
						.select(Team::getTeamId, Team::getTeamName, Team::getIsPublic)
						.eq(Team::getIsPublic, 1)
						.orderByDesc(Team::getCreateTime)
		);
	}

	@Override
	public void saveLink(TeamLink teamLink) {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			return;
		}

		if (null == teamLink.getTeamId()) {
			return;
		}

		if (StringUtils.isAnyBlank(teamLink.getTitle(), teamLink.getUrl())) {
			throw new BisException("请填写标题和链接");
		}

		//标题不能大于20个字符
		if (teamLink.getTitle().length() > 50) {
			throw new BisException("标题不能大于50个字符");
		}

		if (sensitiveFilter.exist(teamLink.getTitle())) {
			throw new BisException("标题包含敏感词", ReqUtil.id(request));
		}

		if (!URLValidator.isValidHttpOrHttpsUrl(teamLink.getUrl())) {
			throw new BisException("链接格式不正确 https://example.com or http://example.com");
		}

		if (URLValidator.isIllegalWebsite(teamLink.getUrl())) {
			throw new BisException("链接不合法");
		}

		Team team = teamMapper.selectById(teamLink.getTeamId());
		if (null == team) {
			return;
		}

		// 验证是否是创建者
		if (!team.getCreatorUserId().equals(userId)) {
			throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
		}

		teamLink.setUserId(userId);
		teamLinkMapper.insert(teamLink);
	}

	@Override
	public void delLink(TeamLink teamLink) {
		Long userId = TokenUtil.userId(request);
		if (null == userId) {
			return;
		}

		if (null == teamLink.getId()) {
			return;
		}

		teamLinkMapper.delete(new LambdaQueryWrapper<>(teamLink).eq(TeamLink::getUserId, userId).eq(TeamLink::getId, teamLink.getId()));
	}

	@Override
	public List<Map> favoriteList(Long userId) {
		return teamMapper.favoriteList(userId);
	}
}
