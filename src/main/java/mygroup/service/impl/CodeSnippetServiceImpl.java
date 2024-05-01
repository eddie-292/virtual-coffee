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
import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.result.UpdateResult;
import mygroup.algorithm.tools.NaiveBayesClassifierTool;
import mygroup.common.constant.QueueConstant;
import mygroup.common.enums.Permissions;
import mygroup.common.exception.BisException;
import mygroup.common.exception.PermissionDeniedException;
import mygroup.config.StarConfig;
import mygroup.consumer.FeedsConsumer;
import mygroup.dto.*;
import mygroup.dto.common.ConditionQuery;
import mygroup.dto.common.PageQuery;
import mygroup.entity.CodeSnippet;
import mygroup.entity.Team;
import mygroup.entity.UserFavorite;
import mygroup.entity.UserInfo;
import mygroup.mapper.CodeSnippetMapper;
import mygroup.mapper.UserFavoriteMapper;
import mygroup.mapper.UserInfoMapper;
import mygroup.service.ICodeSnippetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mygroup.service.ITeamService;
import mygroup.service.IUserInfoService;
import mygroup.util.GetKey;
import mygroup.util.ReqUtil;
import mygroup.util.SensitiveFilter;
import mygroup.util.TokenUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description:Feed 服务接口实现类
 * @author Eddie
 * @date 2024-02-07
 */
@Service
public class CodeSnippetServiceImpl extends ServiceImpl<CodeSnippetMapper, CodeSnippet> implements ICodeSnippetService {
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private AmqpTemplate rabbitTemplate;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private UserInfoMapper userInfoMapper;
	@Autowired
	private UserFavoriteMapper userFavoriteMapper;
	@Autowired
	private NaiveBayesClassifierTool tool;
	@Autowired
	private IUserInfoService userInfoService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private SensitiveFilter sensitiveFilter;
	@Autowired
	private StarConfig starConfig;
	@Autowired
	private ITeamService teamService;

	@Override
	public IPage<CodeSnippetDoc> queryByPage(CodeSnippetQuery pageQuery) {
		Criteria criteria = new Criteria();
		String position = pageQuery.getPosition();
		String teamId = pageQuery.getTeamId();

		if (StringUtils.isBlank(position)) {
			return new Page<>();
		}

		//如果是团队的话，需要传入团队ID
		if (position.equals("team")) {
			if (StringUtils.isBlank(teamId)) {
				return new Page<>();
			}

			Team team = teamService.get(teamId);
			if (team == null) {
				return new Page<>();
			}

			//如果是私有团队，需要是团队成员
			if (!team.getIsPublic() && !team.isJoin()) {
				return new Page<>();
			}

			criteria.and("teamId").is(teamId);
		}

		criteria.and("position").is(position);
		AggregationOptions aggregationOptions = AggregationOptions.builder().cursorBatchSize(pageQuery.getPageNum()).allowDiskUse(true).build();
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(criteria),
				Aggregation.sort(Sort.by(Sort.Direction.DESC, "createTime")),
				Aggregation.skip(Long.valueOf((pageQuery.getPageNum() - 1) * pageQuery.getPageSize())),
				Aggregation.limit(pageQuery.getPageSize())
		).withOptions(aggregationOptions);

		String collectionName = FeedsConsumer.COLLECTION_NAME;
		List<CodeSnippetDoc> documentList = mongoTemplate.aggregate(aggregation, collectionName, CodeSnippetDoc.class).getMappedResults();
		if (CollectionUtils.isEmpty(documentList)) {
			return new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), 0, false);
		}

		//查询用户信息
		Set<Long> userIds = documentList.stream().map(CodeSnippetDoc::getUserId).collect(Collectors.toSet());
		List<UserInfo> userInfos = userInfoMapper.selectBatchIds(userIds);
		Map<Long, UserInfo> infoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUserId, i -> i));

		//查询收藏信息
		Long userId = TokenUtil.userId(request);
		if (userId != null) {
			QueryWrapper<UserFavorite> eq = new QueryWrapper<UserFavorite>()
					.eq("user_id", userId);
			List<UserFavorite> userFavorites = userFavoriteMapper.selectList(eq);
			if (CollectionUtil.isNotEmpty(userFavorites)) {
				Set<String> favoriteIds = userFavorites.stream().map(UserFavorite::getOtherId).collect(Collectors.toSet());
				documentList.forEach(doc -> {
					doc.setFavorite(favoriteIds.contains(doc.getId()));
				});
			}
		}

		//查询用户信息
		documentList.forEach(doc -> {
			UserInfo userInfo = infoMap.get(doc.getUserId());
			if (userInfo == null) {
				doc.setAuthor("匿名用户");
				doc.setAuthorImg("");
				doc.setAuthorDesc("");
				doc.setUserId(null);
				doc.setAnonymize(true);
			} else {
				doc.setAuthor(userInfo.getNickname());
				doc.setSubscriberMark(ObjectUtils.defaultIfNull(userInfo.getSubscriber(), 0) > 0);
				doc.setAuthorImg(userInfo.getAvatar());
				doc.setAuthorDesc(userInfo.getOccupation());
			}
		});

		Query query = new Query(criteria);
		long count = mongoTemplate.count(query, collectionName);

		IPage<CodeSnippetDoc> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), count, false);
		page.setRecords(ObjectUtils.defaultIfNull(documentList, new ArrayList<>()));
		return page;
	}

	@Override
	public IPage<CodeSnippetDoc> queryMyPage(PageQuery pageQuery) {
		if (!NumberUtil.isNumber(pageQuery.getUserId())) {
			return new Page<>();
		}

		// 被查看者ID
		Long reqUserId = Long.valueOf(pageQuery.getUserId());

		if (!userInfoService.feedOpen(reqUserId)) {
			return new Page<>();
		}

		//个人主页查看privateGroup不等1的公开数据
		Criteria criteria = Criteria.where("userId").is(reqUserId)
				.and("privateGroup").ne(1);

		AggregationOptions aggregationOptions = AggregationOptions.builder().cursorBatchSize(pageQuery.getPageNum()).allowDiskUse(true).build();
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(criteria),
				Aggregation.sort(Sort.by(Sort.Direction.DESC, "createTime")),
				Aggregation.skip(Long.valueOf((pageQuery.getPageNum() - 1) * pageQuery.getPageSize())),
				Aggregation.limit(pageQuery.getPageSize())
		).withOptions(aggregationOptions);

		String collectionName = FeedsConsumer.COLLECTION_NAME;
		List<CodeSnippetDoc> documentList = mongoTemplate.aggregate(aggregation, collectionName, CodeSnippetDoc.class).getMappedResults();

		//查询我的收藏信息
		Long myId = TokenUtil.userId(request);
		if (null != myId) {
			QueryWrapper<UserFavorite> eq = new QueryWrapper<UserFavorite>()
					.eq("user_id", myId);
			List<UserFavorite> userFavorites = userFavoriteMapper.selectList(eq);
			if (CollectionUtil.isNotEmpty(userFavorites)) {
				Set<String> favoriteIds = userFavorites.stream().map(UserFavorite::getOtherId).collect(Collectors.toSet());
				documentList.forEach(doc -> {
					doc.setFavorite(favoriteIds.contains(doc.getId()));
				});
			}
		}

		Query query = new Query(criteria);
		long count = mongoTemplate.count(query, collectionName);
		IPage<CodeSnippetDoc> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), count, false);
		page.setRecords(ObjectUtils.defaultIfNull(documentList, new ArrayList<>()));
		return page;
	}

	@Override
	public List<CodeSnippetDoc> queryByCondition(ConditionQuery conditionQuery) {
		String sort = "createTime";
		int limit = 15;

		String collectionName = FeedsConsumer.COLLECTION_NAME;
		// 创建条件对象
		Criteria criteria = new Criteria();
		// 创建查询对象，然后将条件对象添加到其中，然后根据指定字段进行排序
		Query query = new Query(criteria)
				.with(Sort.by(sort).descending())
				.limit(limit);
		// 执行查询
		List<CodeSnippetDoc> documentList = mongoTemplate.find(query, CodeSnippetDoc.class, collectionName);

		Set<Long> userId = documentList.stream().map(CodeSnippetDoc::getUserId).collect(Collectors.toSet());
		List<UserInfo> userInfos = userInfoMapper.selectBatchIds(userId);
		Map<Long, UserInfo> infoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUserId, i -> i));

		documentList.forEach(doc -> {
			UserInfo userInfo = infoMap.get(doc.getUserId());
			doc.setAuthor(userInfo.getNickname());
			doc.setSubscriberMark(ObjectUtils.defaultIfNull(userInfo.getSubscriber(), 0) > 0);
			doc.setAuthorImg(userInfo.getAvatar());
			doc.setAuthorDesc(userInfo.getOccupation());
		});
		return documentList;
	}

	@Override
	public void saveCodeSnippetInfo(CodeSnippetDTO entity) {
		Long userId = TokenUtil.userId(request);
		String key = GetKey.getFeedSaveKey(userId);
		if (null != userId) {
			if (redisTemplate.hasKey(key)) {
				throw new BisException("发布Feed太频繁，请5秒后再试");
			}
			if (StringUtils.isAnyBlank(entity.getContent(), entity.getPosition())) {
				return;
			}
			if (!userInfoService.hasPermission(userId, Permissions.PUBLISH_FEED.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}
			if (!userInfoService.checkEconomy(userId, starConfig.getPublishCode())) {
				throw new BisException("星星不足");
			}
			if (sensitiveFilter.exist(entity.getContent())) {
				throw new BisException("内容包含敏感词", ReqUtil.id(request));
			}
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("content", entity.getContent());
			hashMap.put("position", entity.getPosition());
			hashMap.put("teamId", entity.getTeamId());
			if (CollectionUtil.isNotEmpty(entity.getArticleIds())) {
				hashMap.put("articleIds", CollectionUtil.join(entity.getArticleIds(), ","));
			}

			hashMap.put("platform", ReqUtil.getPlatform(request));
			rabbitTemplate.convertAndSend(QueueConstant.Code_Snippet, JSON.toJSONString(hashMap));

			//记录使用时间在redis，接口冷却时间5秒
			redisTemplate.opsForValue().set(key, "1", 5, TimeUnit.SECONDS);
		}

	}

	@Override
	public void favorite(CodeSnippetDTO entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("otherId", entity.getId());
			rabbitTemplate.convertAndSend(QueueConstant.User_Favorite, JSON.toJSONString(hashMap));
		}

	}

	@Override
	public void report(CodeSnippetDTO entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("id", entity.getId());
			hashMap.put("type", "CodeSnippet");
			rabbitTemplate.convertAndSend(QueueConstant.Content_Report, JSON.toJSONString(hashMap));
		}

	}

	@Override
	public void del(CodeSnippetDTO entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			if (!userInfoService.hasPermission(userId, Permissions.DELETE_FEED.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("id", entity.getId());
			rabbitTemplate.convertAndSend(QueueConstant.Code_Snippet_Delete, JSON.toJSONString(hashMap));
		}

	}

	@Override
	public void recharge(CodeSnippetDTO entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			String starTransferKey = GetKey.getStarTransferKey(userId);
			if (redisTemplate.hasKey(starTransferKey)) {
				throw new BisException("存在未完成交易，请稍等");
			}

			if (!userInfoService.checkEconomy(userId)) {
				throw new BisException("星星不足");
			}

			//创建交易
			StarTransfer starTransfer = new StarTransfer();
			starTransfer.setSenderId(userId);
			starTransfer.setReceiverId(0L);
			starTransfer.setAmount(new BigDecimal("1"));
			starTransfer.setTxId(userId.toString());
			starTransfer.setFinished(false);
			starTransfer.setCreateTime(new Date());

			//保存交易到redis 1分钟过期
			redisTemplate.opsForValue().set(starTransferKey, JSON.toJSONString(starTransfer), 1, TimeUnit.MINUTES);

			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("id", entity.getId());
			rabbitTemplate.convertAndSend(QueueConstant.Code_Snippet_Recharge, JSON.toJSONString(hashMap));
		}

	}

	@Override
	public IPage<CodeSnippetDoc> feeds(PageQuery pageQuery) {
		if (!NumberUtil.isNumber(pageQuery.getUserId())) {
			return new Page<>();
		}

		Long reqUserId = Long.valueOf(pageQuery.getUserId());
		Criteria criteria = Criteria.where("userId").is(reqUserId);
		AggregationOptions aggregationOptions = AggregationOptions.builder().cursorBatchSize(pageQuery.getPageNum()).allowDiskUse(true).build();
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(criteria),
				Aggregation.sort(Sort.by(Sort.Direction.DESC, "createTime")),
				Aggregation.skip(Long.valueOf((pageQuery.getPageNum() - 1) * pageQuery.getPageSize())),
				Aggregation.limit(pageQuery.getPageSize())
		).withOptions(aggregationOptions);

		String collectionName = FeedsConsumer.COLLECTION_NAME;
		List<CodeSnippetDoc> documentList = mongoTemplate.aggregate(aggregation, collectionName, CodeSnippetDoc.class).getMappedResults();
		Query query = new Query(criteria);
		long count = mongoTemplate.count(query, collectionName);
		IPage<CodeSnippetDoc> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), count, false);
		page.setRecords(ObjectUtils.defaultIfNull(documentList, new ArrayList<>()));
		return page;
	}

	@Override
	public void move(FeedMove entity) {
		Long userId = TokenUtil.userId(request);
		if (StringUtils.isAnyBlank(entity.getFeedId(), entity.getGroupId())) {
			throw new BisException("参数错误");
		}

		if (null != userId) {
			if (!userInfoService.hasPermission(userId, Permissions.MOVE_FEED.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}

			//更新id=entity.getFeedId()的 CodeSnippetDoc中的teamId=entity.getGroupId
			Query query = new Query(Criteria.where("_id").is(entity.getFeedId()));
			Update update = new Update();
			update.set("teamId", entity.getGroupId());
			update.set("position", "team");
			UpdateResult updateResult = mongoTemplate.updateFirst(query, update, CodeSnippetDoc.class, FeedsConsumer.COLLECTION_NAME);
			if (updateResult.getModifiedCount() == 0) {
				throw new BisException("移动失败");
			}

		}
	}

}
