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

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import mygroup.common.annotation.Free;
import mygroup.common.annotation.RequestLimit;
import mygroup.common.exception.BisException;
import mygroup.dto.*;
import mygroup.entity.Tags;
import mygroup.util.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import mygroup.dto.common.ConditionQuery;
import mygroup.dto.common.PageQuery;
import mygroup.dto.common.RestData;

import mygroup.entity.Article;
import mygroup.service.IArticleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:文章表控制器
 * @author Eddie
 * @date 2024-02-07
 */
@RestController
@RequestMapping("/article")
public class ArticleController {
	@Autowired
	private IArticleService articleService;
	@Autowired
	private HttpServletRequest request;

	/**
	 * @description:评论
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@RequestLimit(second = 5, maxCount = 1, msg = "操作频繁，请5秒后再试")
	@PostMapping("/comment")
	public RestData<String> comment(@RequestBody Comment entity) {
		articleService.comment(entity);
		return RestData.success("已提交");
	}

	@PostMapping("/comment/thank")
	public RestData<String> commentThank(@RequestBody Comment entity) {
		articleService.commentThank(entity);
		return RestData.success("已提交感谢");
	}

	/**
	 * @description:按照条件进行分页查询评论
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@Free
	@PostMapping("/comment/page")
	public RestData<IPage<QueryCommentDTO>> pageArticleCommentInfo(@RequestBody CommentQuery pageQuery) {
		if (StringUtils.isBlank(pageQuery.getBizId())) {
			return RestData.success(new Page<QueryCommentDTO>());
		}

		pageQuery.setPageSize(30);
		IPage<QueryCommentDTO> page = articleService.commentList(pageQuery);
		return RestData.success(page);
	}

	/**
	 * @description:收藏
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@PostMapping("/favorite")
	public RestData<String> favorite(@RequestBody ArticleDoc entity) {
		articleService.favorite(entity);
		return RestData.success("已收藏");
	}

	/**
	 * @description:报告
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@PostMapping("/report")
	public RestData<String> report(@RequestBody ArticleDoc entity) {
		articleService.report(entity);
		return RestData.success("已报告");
	}

	/**
	 * @description:充电
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@RequestLimit(second = 5, maxCount = 10)
	@PostMapping("/recharge")
	public RestData<String> recharge(@RequestBody ArticleDoc entity) {
		articleService.recharge(entity);
		return RestData.success("已提交充电");
	}

	@RequestLimit(msg = "点击太快了，请稍后再试")
	@PostMapping("/save")
	public RestData<String> saveArticle(@RequestBody ArticleDoc entity) {
		entity.setArticleId(IdUtil.getSnowflakeNextIdStr());
		articleService.saveArticleInfo(entity);

		RestData<String> success = RestData.success();
		success.setData(entity.getArticleId());
		return success;
	}

	@RequestLimit(msg = "点击太快了，请稍后再试")
	@PostMapping("/update")
	public RestData<String> updateArticle(@RequestBody ArticleDoc entity) {
		articleService.updateArticleInfoByKey(entity);

		RestData<String> success = RestData.success();
		success.setData(entity.getArticleId());
		return success;
	}

	@RequestLimit(msg = "点击太快了，请稍后再试")
	@PostMapping("/append")
	public RestData<String> appendArticle(@RequestBody ArticleDoc entity) {
		articleService.appendArticle(entity);

		RestData<String> success = RestData.success();
		success.setData(entity.getArticleId());
		return success;
	}
	
	@Free
	@GetMapping("/{id}")
	public RestData<ArticleDoc> getArticleInfoByKey(@PathVariable String id) {
		ArticleDoc entity = articleService.getArticleInfoByKey(id);
		return RestData.success(entity);
	}
	
	
	/**
 	* @description:按照条件进行分页查询文章表
    * @author Eddie
    * @date 2024-02-07
    */
	@Free
	@PostMapping("/page")
	public RestData<IPage<ArticleDoc>> pageArticleInfo(@RequestBody ArticleQuery pageQuery) {
		pageQuery.setPageSize(15);
		IPage<ArticleDoc> page = articleService.pageArticleInfo(pageQuery);
		return RestData.success(page);
	}

	/**
	 * @description:查询会员的帖子列表
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@Free
	@PostMapping("/member")
	public RestData<IPage<ArticleDoc>> memberPageArticleInfo(@RequestBody ArticleQuery pageQuery) {
		if (StringUtils.isBlank(pageQuery.getUserId()) || !NumberUtil.isNumber(pageQuery.getUserId())) {
			return RestData.success(new Page<>());
		}

		pageQuery.setPageSize(15);
		IPage<ArticleDoc> page = articleService.memberPageArticleInfo(pageQuery);
		return RestData.success(page);
	}

	/**
	 * @description:查询文章标签
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@Free
	@GetMapping("/tags")
	public RestData<List<Tags>> tags() {
		List<Tags> tags = articleService.tags();
		return RestData.success(tags);
	}

}
