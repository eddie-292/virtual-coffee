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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import mygroup.algorithm.tools.NaiveBayesClassifierTool;
import mygroup.common.constant.QueueConstant;
import mygroup.common.constant.SystemConstant;
import mygroup.common.enums.Permissions;
import mygroup.common.exception.BisException;
import mygroup.common.exception.PermissionDeniedException;
import mygroup.config.StarConfig;
import mygroup.consumer.*;
import mygroup.dto.*;
import mygroup.dto.common.ConditionQuery;
import mygroup.dto.common.PageQuery;
import mygroup.entity.Article;
import mygroup.entity.Tags;
import mygroup.entity.UserFavorite;
import mygroup.entity.UserInfo;
import mygroup.mapper.*;
import mygroup.service.IArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description:文章表 服务接口实现类
 * @author Eddie
 * @date 2024-02-07
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
	@Autowired
	private TagsMapper tagsMapper;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private AmqpTemplate rabbitTemplate;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private UserInfoMapper userInfoMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private NaiveBayesClassifierTool tool;
	@Autowired
	private SensitiveFilter sensitiveFilter;
	@Autowired
	private UserFavoriteMapper userFavoriteMapper;
	@Autowired
	private CommentsMapper commentsMapper;
	@Autowired
	private StarConfig starConfig;
	@Autowired
	private IUserInfoService userInfoService;

	@Override
	public void saveArticleInfo(ArticleDoc entity) {
		Long userId = TokenUtil.userId(request);
		String key = GetKey.getArticleAddKey(userId);
		if (null != userId) {
			if (redisTemplate.hasKey(key)) {
				throw new BisException("操作频繁，请60秒后再试");
			}
			if (!userInfoService.hasPermission(userId, Permissions.PUBLISH_TOPIC.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}
			if (StringUtils.isBlank(entity.getTitle())) {
				throw new BisException("请输入标题");
			}
			if (StringUtils.isBlank(entity.getContent())) {
				throw new BisException("请输入内容");
			}
			if (StringUtils.isAnyBlank(entity.getCategory(), entity.getCategoryTitle())) {
				throw new BisException("请输入分类");
			}
			if (!userInfoService.checkEconomy(userId, starConfig.getPublishArticle())) {
				throw new BisException("星星不足");
			}
			if (sensitiveFilter.exist(entity.getTitle())) {
				throw new BisException("标题包含敏感词", ReqUtil.id(request));
			}
			if (sensitiveFilter.exist(entity.getContent())) {
				throw new BisException("内容包含敏感词", ReqUtil.id(request));
			}
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("articleId", entity.getArticleId());
			hashMap.put("title", entity.getTitle());
			hashMap.put("content", entity.getContent());
			hashMap.put("category", entity.getCategory());
			hashMap.put("categoryTitle", entity.getCategoryTitle());
			hashMap.put("mainImageUrl", entity.getMainImageUrl());
			hashMap.put("type", entity.getType());
			hashMap.put("platform", ReqUtil.getPlatform(request));
			rabbitTemplate.convertAndSend(QueueConstant.Article_Add, JSON.toJSONString(hashMap));

			//记录使用时间在redis，接口冷却时间60秒
			redisTemplate.opsForValue().set(key, "1", 60, TimeUnit.SECONDS);
		}
	}

	@Override
	public void updateArticleInfoByKey(ArticleDoc entity) {
		Long userId = TokenUtil.userId(request);
		String key = GetKey.getArticleUpdateKey(userId);
		if (null != userId) {
			if (redisTemplate.hasKey(key)) {
				throw new BisException("操作频繁，请60秒后再试");
			}
			if (!userInfoService.hasPermission(userId, Permissions.PUBLISH_TOPIC.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}
			if (StringUtils.isBlank(entity.getTitle())) {
				throw new BisException("请输入标题");
			}
			if (StringUtils.isBlank(entity.getContent())) {
				throw new BisException("请输入内容");
			}
			if (StringUtils.isAnyBlank(entity.getCategory(), entity.getCategoryTitle())) {
				throw new BisException("请选择分类");
			}
			if (!userInfoService.checkEconomy(userId, starConfig.getPublishArticle())) {
				throw new BisException("星星不足");
			}
			if (sensitiveFilter.exist(entity.getTitle())) {
				throw new BisException("标题包含敏感词", ReqUtil.id(request));
			}
			if (sensitiveFilter.exist(entity.getContent())) {
				throw new BisException("内容包含敏感词", ReqUtil.id(request));
			}
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("articleId", entity.getArticleId());
			hashMap.put("title", entity.getTitle());
			hashMap.put("content", entity.getContent());
			hashMap.put("category", entity.getCategory());
			hashMap.put("categoryTitle", entity.getCategoryTitle());
			hashMap.put("platform", ReqUtil.getPlatform(request));
			rabbitTemplate.convertAndSend(QueueConstant.Article_Update, JSON.toJSONString(hashMap));

			//记录使用时间在redis，接口冷却时间60秒
			redisTemplate.opsForValue().set(key, "1", 60, TimeUnit.SECONDS);
		}
	}

	@Override
	public void appendArticle(ArticleDoc entity) {
		Long userId = TokenUtil.userId(request);
		String key = GetKey.getArticleAppendKey(userId);
		if (null != userId) {
			if (redisTemplate.hasKey(key)) {
				throw new BisException("操作频繁，请60秒后再试");
			}
			if (!userInfoService.hasPermission(userId, Permissions.PUBLISH_TOPIC.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}
			if (StringUtils.isBlank(entity.getContent())) {
				throw new BisException("请输入内容");
			}
			if (sensitiveFilter.exist(entity.getContent())) {
				throw new BisException("内容包含敏感词", ReqUtil.id(request));
			}
			if (!userInfoService.checkEconomy(userId, starConfig.getPublishArticle())) {
				throw new BisException("星星不足");
			}
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("articleId", entity.getArticleId());
			hashMap.put("content", entity.getContent());
			hashMap.put("platform", ReqUtil.getPlatform(request));

			rabbitTemplate.convertAndSend(QueueConstant.Article_Append, JSON.toJSONString(hashMap));

			//记录使用时间在redis，接口冷却时间60秒
			redisTemplate.opsForValue().set(key, "1", 60, TimeUnit.SECONDS);
		}
	}

	@Override
	public void removeArticleInfoByKey(ArticleDoc entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			if (!userInfoService.hasPermission(userId, Permissions.DELETE_TOPIC.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("articleId", entity.getArticleId());
			rabbitTemplate.convertAndSend(QueueConstant.Article_Del, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public List<Tags> tags() {
		List<Tags> tags = tagsMapper.selectList(new LambdaQueryWrapper<Tags>()
				.orderByDesc(Tags::getTagId));
		if (CollectionUtil.isEmpty(tags)) {
			return new ArrayList<>();
		}

		return tags;
	}

	@Override
	public void favorite(ArticleDoc entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("otherId", entity.getArticleId());
			rabbitTemplate.convertAndSend(QueueConstant.User_Favorite, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public void report(ArticleDoc entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("userId", userId);
			hashMap.put("id", entity.getArticleId());
			hashMap.put("type", "Article");
			rabbitTemplate.convertAndSend(QueueConstant.Content_Report, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public void recharge(ArticleDoc entity) {
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
			hashMap.put("id", entity.getArticleId());
			rabbitTemplate.convertAndSend(QueueConstant.Article_Recharge, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public void comment(Comment entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			if (!userInfoService.hasPermission(userId, Permissions.REPLY.getCode())) {
				throw new PermissionDeniedException("没有权限", ReqUtil.id(request));
			}
			String id = entity.getId();
			String content = entity.getContent();
			if (StringUtils.isBlank(content)) {
				throw new BisException("请输入评论内容");
			}
			String title = entity.getTitle();
			if (StringUtils.isBlank(title)) {
				throw new BisException("未能提交回复");
			}
			if (StringUtils.isBlank(id)) {
				throw new BisException("未能提交回复");
			}
			if (sensitiveFilter.exist(content)) {
				throw new BisException("内容包含敏感词", ReqUtil.id(request));
			}

			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("content", content);
			hashMap.put("type", CommentConsumer.TYPE_Article);
			hashMap.put("userId", userId);
			hashMap.put("bizId", id);
			hashMap.put("title", title);

			UserAgent ua = UserAgentUtil.parse((request).getHeader(Header.USER_AGENT.toString()));
			String platform = ua.getPlatform().toString();
			hashMap.put("platform", platform);
			if (StringUtils.isNotBlank(entity.getReplyId())) {
				hashMap.put("replyId", entity.getReplyId());
			}
			rabbitTemplate.convertAndSend(QueueConstant.Comment, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public IPage<QueryCommentDTO> commentList(CommentQuery pageQuery) {
		Long bizId = Long.valueOf(pageQuery.getBizId());
		Integer pageNum = pageQuery.getPageNum();
		Integer pageSize = pageQuery.getPageSize();
		int offset = (pageNum - 1) * pageSize;

		long count = commentsMapper.commentListCount(bizId);
		List<QueryCommentDTO> commentList = commentsMapper.commentList(bizId, offset, pageSize);

		IPage<QueryCommentDTO> page = new Page<>(pageNum, pageSize, count, false);
		page.setRecords(ObjectUtils.defaultIfNull(commentList, new ArrayList<>()));
		return page;
	}

	@Override
	public void commentThank(Comment entity) {
		Long userId = TokenUtil.userId(request);

		if (null != userId) {
			String id = entity.getId();
			if (StringUtils.isBlank(id)) {
				throw new BisException("未能提交感谢");
			}

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
			hashMap.put("commentId", id);
			hashMap.put("userId", userId);
			rabbitTemplate.convertAndSend(QueueConstant.Comment_Like, JSON.toJSONString(hashMap));
		}
	}

	@Override
	public IPage<ArticleDoc> memberPageArticleInfo(ArticleQuery pageQuery) {
		Criteria criteria = new Criteria();

		String tag = pageQuery.getTag();
		if (StringUtils.isNotBlank(tag)) {
			criteria.and("category").is(tag);
		}

		String userId = pageQuery.getUserId();
		if (StringUtils.isNotBlank(userId)) {
			criteria.and("creatorUserId").is(Long.valueOf(userId));
		}

		if (!userInfoService.topicOpen(Long.valueOf(userId))) {
			return new Page<>();
		}

		AggregationOptions aggregationOptions = AggregationOptions.builder().cursorBatchSize(pageQuery.getPageNum()).allowDiskUse(true).build();
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.project("creatorUserId","articleId", "title","createTime","category", "summary", "view", "batteryLevel"),
				Aggregation.match(criteria),
				Aggregation.sort(Sort.by(Sort.Direction.DESC, "createTime")),
				Aggregation.skip(Long.valueOf((pageQuery.getPageNum() - 1) * pageQuery.getPageSize())),
				Aggregation.limit(pageQuery.getPageSize())
		).withOptions(aggregationOptions);

		String collectionName = ArticleConsumer.COLLECTION_NAME;
		List<ArticleDoc> documentList = mongoTemplate.aggregate(aggregation, collectionName, ArticleDoc.class).getMappedResults();

		Query query = new Query(criteria);
		long count = mongoTemplate.count(query, collectionName);

		IPage<ArticleDoc> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), count, false);
		page.setRecords(ObjectUtils.defaultIfNull(documentList, new ArrayList<>()));
		return page;
	}

	@Override
	public ArticleDoc getArticleInfoByKey(String id) {
		ArticleDoc articleDoc = mongoTemplate.findById(id, ArticleDoc.class, ArticleConsumer.COLLECTION_NAME);
		if (null == articleDoc) {
			return null;
		}

		// 五分钟内可以编辑
		Date createTime = articleDoc.getCreateTime();
		long differenceInMilliseconds = DateUtil.between(createTime, new Date(), DateUnit.MS);
		long differenceInMinutes = differenceInMilliseconds / (1000 * 60);
		articleDoc.setUpdate(differenceInMinutes < 5);

		// 附言可以新增3次但必须在5分钟以后
		articleDoc.setAppend(differenceInMinutes > 5 && articleDoc.getAppendCount() < 3);

		if (null != articleDoc.getAppendCount() && articleDoc.getAppendCount() > 0) {
			List<ArticleAppendDoc> appendList = mongoTemplate.find(
					new Query(Criteria.where("articleId").is(id)), ArticleAppendDoc.class, ArticleAppendConsumer.COLLECTION_NAME);
			if (CollectionUtil.isNotEmpty(appendList)) {
				appendList.sort(Comparator.comparing(ArticleAppendDoc::getCreateTime));
				articleDoc.setAppendList(appendList);
			}
		}

		//查询我的收藏信息
		Long myId = TokenUtil.userId(request);
		if (myId != null) {
			QueryWrapper<UserFavorite> eq = new QueryWrapper<UserFavorite>()
					.eq("user_id", myId);
			List<UserFavorite> userFavorites = userFavoriteMapper.selectList(eq);
			if (CollectionUtil.isNotEmpty(userFavorites)) {
				Set<String> favoriteIds = userFavorites.stream().map(UserFavorite::getOtherId).collect(Collectors.toSet());
				articleDoc.setFavorite(favoriteIds.contains(articleDoc.getArticleId()));
			}
		}

		Map<String, Object> hashMap = new HashMap<>();
		hashMap.put("id", id);
		hashMap.put("type", ViewConsumer.COLLECTION_NAME_A);
		rabbitTemplate.convertAndSend(QueueConstant.View, JSON.toJSONString(hashMap));
		return articleDoc;
	}

	@Override
	public IPage<ArticleDoc> pageArticleInfo(ArticleQuery pageQuery) {
		Criteria criteria = new Criteria();
		if (StringUtils.isNotBlank(pageQuery.getTag())) {
			criteria.and("category").is(pageQuery.getTag());
		}
		if (StringUtils.isNotBlank(pageQuery.getUserId())) {
			criteria.and("creatorUserId").is(Long.valueOf(pageQuery.getUserId()));
		}
		AggregationOptions aggregationOptions = AggregationOptions.builder().cursorBatchSize(pageQuery.getPageNum()).allowDiskUse(true).build();
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.project("creatorUserId","articleId", "title","createTime","category", "summary", "view", "batteryLevel"),
				Aggregation.match(criteria),
				Aggregation.sort(Sort.by(Sort.Direction.DESC, "createTime")),
				Aggregation.skip(Long.valueOf((pageQuery.getPageNum() - 1) * pageQuery.getPageSize())),
				Aggregation.limit(pageQuery.getPageSize())
		).withOptions(aggregationOptions);

		String collectionName = ArticleConsumer.COLLECTION_NAME;
		List<ArticleDoc> documentList = mongoTemplate.aggregate(aggregation, collectionName, ArticleDoc.class).getMappedResults();

		//设置用户头像
		if (CollectionUtil.isNotEmpty(documentList)) {
			Set<Long> userIds = documentList.stream().map(ArticleDoc::getCreatorUserId).collect(Collectors.toSet());
			List<UserInfo> userInfos = userInfoMapper.selectBatchIds(userIds);
			Map<Long, UserInfo> userInfoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUserId, user -> user));
			documentList.forEach(articleDoc -> {
				UserInfo userInfo = userInfoMap.get(articleDoc.getCreatorUserId());
				if (null == userInfo) {

					articleDoc.setAvatar(SystemConstant.DEFAULT_AVATAR);
					articleDoc.setNickname(SystemConstant.DEFAULT_NICKNAME);
				} else {
					articleDoc.setAvatar(userInfo.getAvatar());
					articleDoc.setNickname(userInfo.getNickname());
				}
			});
		}

		Query query = new Query(criteria);
		long count = mongoTemplate.count(query, collectionName);

		IPage<ArticleDoc> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), count, false);
		page.setRecords(ObjectUtils.defaultIfNull(documentList, new ArrayList<>()));
		return page;
	}

	@Override
	public IPage<ArticleDoc> topics(ArticleQuery pageQuery) {
		Criteria criteria = new Criteria();
		if (StringUtils.isNotBlank(pageQuery.getTag())) {
			criteria.and("category").is(pageQuery.getTag());
		}
		if (StringUtils.isNotBlank(pageQuery.getUserId())) {
			criteria.and("creatorUserId").is(Long.valueOf(pageQuery.getUserId()));
		}
		AggregationOptions aggregationOptions = AggregationOptions.builder().cursorBatchSize(pageQuery.getPageNum()).allowDiskUse(true).build();
		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.project("creatorUserId","articleId", "title","createTime","category", "summary", "view", "batteryLevel"),
				Aggregation.match(criteria),
				Aggregation.sort(Sort.by(Sort.Direction.DESC, "createTime")),
				Aggregation.skip(Long.valueOf((pageQuery.getPageNum() - 1) * pageQuery.getPageSize())),
				Aggregation.limit(pageQuery.getPageSize())
		).withOptions(aggregationOptions);

		String collectionName = ArticleConsumer.COLLECTION_NAME;
		List<ArticleDoc> documentList = mongoTemplate.aggregate(aggregation, collectionName, ArticleDoc.class).getMappedResults();
		Query query = new Query(criteria);
		long count = mongoTemplate.count(query, collectionName);
		IPage<ArticleDoc> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize(), count, false);
		page.setRecords(ObjectUtils.defaultIfNull(documentList, new ArrayList<>()));
		return page;
	}

}
