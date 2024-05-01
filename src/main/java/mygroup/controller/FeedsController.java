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

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import mygroup.common.annotation.Free;
import mygroup.common.annotation.RequestLimit;
import mygroup.dto.CodeSnippetDTO;
import mygroup.dto.CodeSnippetDoc;
import mygroup.dto.CodeSnippetQuery;
import mygroup.dto.FeedMove;
import mygroup.util.TokenUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




import mygroup.dto.common.PageQuery;
import mygroup.dto.common.RestData;

import mygroup.service.ICodeSnippetService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:Feed控制器
 * @author Eddie
 * @date 2024-02-07
 */
@RestController
@RequestMapping("/feed")
public class FeedsController {

	@Autowired
	private ICodeSnippetService codeSnippetService;
	@Autowired
	private HttpServletRequest request;
	
	/**
 	* @description:新增Feed
    * @author Eddie
    * @date 2024-02-07
    */
	@RequestLimit(msg = "点击太快了，请稍后再试")
	@PostMapping
	public RestData<String> saveCodeSnippetInfo(@RequestBody CodeSnippetDTO entity) {
		codeSnippetService.saveCodeSnippetInfo(entity);
		return RestData.success("已发送");
	}

	/**
	 * @description:分页查询Feed
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@Free
	@PostMapping("/home/page")
	public RestData<IPage<CodeSnippetDoc>> pageCodeSnippetInfo(@RequestBody CodeSnippetQuery pageQuery) {
		pageQuery.setPageNum(ObjectUtils.defaultIfNull(pageQuery.getPageNum(), 1));
		pageQuery.setPageSize(15);
		IPage<CodeSnippetDoc> page = codeSnippetService.queryByPage(pageQuery);
		return RestData.success(page);
	}

	/**
	 * @description:分页查询自己的Feed
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@PostMapping("/my/page")
	public RestData<IPage<CodeSnippetDoc>> myCodeSnippet(@RequestBody PageQuery pageQuery) {
		pageQuery.setPageNum(ObjectUtils.defaultIfNull(pageQuery.getPageNum(), 1));
		pageQuery.setPageSize(15);
		pageQuery.setUserId(TokenUtil.userId(request).toString());
		IPage<CodeSnippetDoc> page = codeSnippetService.queryMyPage(pageQuery);
		return RestData.success(page);
	}

	/**
	 * @description:分页查询会员的Feed
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@Free
	@PostMapping("/member")
	public RestData<IPage<CodeSnippetDoc>> memberCodeSnippet(@RequestBody PageQuery pageQuery) {
		if (StringUtils.isBlank(pageQuery.getUserId()) || !NumberUtil.isNumber(pageQuery.getUserId())) {
			return RestData.success(new Page<>());
		}

		pageQuery.setPageNum(ObjectUtils.defaultIfNull(pageQuery.getPageNum(), 1));
		pageQuery.setPageSize(15);
		pageQuery.setUserId(pageQuery.getUserId());
		IPage<CodeSnippetDoc> page = codeSnippetService.queryMyPage(pageQuery);
		return RestData.success(page);
	}

	/**
	 * @description:收藏Feed
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@PostMapping("/favorite")
	public RestData<String> favorite(@RequestBody CodeSnippetDTO entity) {
		codeSnippetService.favorite(entity);
		return RestData.success("已收藏");
	}

	/**
	 * @description:报告Feed
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@PostMapping("/report")
	public RestData<String> report(@RequestBody CodeSnippetDTO entity) {
		codeSnippetService.report(entity);
		return RestData.success("已报告");
	}

	/**
	 * @description:给Feed充电
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@RequestLimit(second = 5, maxCount = 10)
	@PostMapping("/recharge")
	public RestData<String> recharge(@RequestBody CodeSnippetDTO entity) {
		codeSnippetService.recharge(entity);
		return RestData.success("");
	}

	@PostMapping("/move")
	public RestData<String> move(@RequestBody FeedMove entity) {
		codeSnippetService.move(entity);
		return RestData.success("已移动");
	}


}
