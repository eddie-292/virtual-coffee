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

import cn.hutool.core.util.NumberUtil;
import mygroup.common.annotation.Free;
import mygroup.common.annotation.RequestLimit;
import mygroup.dto.CodeSnippetDTO;
import mygroup.dto.TeamJoinApplyResult;
import mygroup.entity.TeamJoinApply;
import mygroup.entity.TeamLink;
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

import mygroup.entity.Team;
import mygroup.service.ITeamService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:团队控制器
 * @author Eddie
 * @date 2024-02-07
 */
@RestController
@RequestMapping("/team")
public class TeamController {

	@Autowired
	private ITeamService teamService;


	/**
	 * @description:收藏
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@PostMapping("/favorite")
	public RestData<String> favorite(@RequestBody Team entity) {
		teamService.favorite(entity);
		return RestData.success("已收藏");
	}

	@GetMapping("/favorite/list")
	public RestData<List<Map>> favoriteList(HttpServletRequest request) {
		List<Map> list = teamService.favoriteList(TokenUtil.userId(request));
		return RestData.success(list);
	}

	/**
	 * @description:最新的20个团队
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@Free
	@GetMapping("/new/list")
	public RestData<List<Team>> newListLimit() {
		int limit = 20;
		List<Team> list = teamService.newListLimit(limit);
		return RestData.success(list);
	}
	
	/**
 	* @description:新增团队
    * @author Eddie
    * @date 2024-02-07
    */
	@RequestLimit
	@PostMapping("/info/save")
	public RestData<String> saveTeamInfo(@RequestBody Team entity) {
		teamService.saveTeamInfo(entity);
		return RestData.success();
	}

	/**
	 * @description:加入/退出团队
	 * @author Eddie
	 * @date 2024-02-07
	 */
	@RequestLimit
	@PostMapping("/join")
	public RestData<String> join(@RequestBody Team entity) {
		teamService.joinTeam(entity);
		return RestData.success();
	}
	
	/**
 	* @description:按照主键修改团队
    * @author Eddie
    * @date 2024-02-07
    */
	@PostMapping("/info/update")
	public RestData<String> updateTeamInfoByKey(@RequestBody Team entity) {
		teamService.updateTeamInfoByKey(entity);
		return RestData.success();
	}
	
	/**
 	* @description:按照主键查询团队
    * @author Eddie
    * @date 2024-02-07
    */
	@GetMapping("/info/{id}")
	public RestData<Team> getTeamInfoByKey(@PathVariable String id) {
		if (StringUtils.isBlank(id) || !NumberUtil.isNumber(id)) {
			return RestData.success();
		}

		Team entity = teamService.getTeam(id);
		return RestData.success(entity);
	}
	
	
	/**
 	* @description:按照条件进行分页查询团队
    * @author Eddie
    * @date 2024-02-07
    */
	@Free
	@PostMapping("/info/page")
	public RestData<IPage<Team>> pageTeamInfo(@RequestBody PageQuery pageQuery) {
		pageQuery.setPageSize(9);
		IPage<Team> page = teamService.queryByPage(pageQuery);
		return RestData.success(page);
	}
	
	/**
 	* @description:可移动的GROUP列表
    * @author Eddie
    * @date 2024-02-07
    */
	@PostMapping("/move/list")
	public RestData<List<Team>> moveList() {
		List<Team> list = teamService.moveList();
		return RestData.success(list);
	}

	/**
	 * 申请加入
	 * @param entity
	 * @return
	 */
	@RequestLimit
	@PostMapping("/join-apply")
	public RestData<String> joinApply(@RequestBody Team entity) {
		teamService.joinApply(entity);
		return RestData.success();
	}

	/**
	 * 申请审核
	 * @param teamJoinApply
	 * @return
	 */
	@PostMapping("/join-apply-review")
	public RestData<String> joinApplyReview(@RequestBody TeamJoinApply teamJoinApply) {
		teamService.joinApplyReview(teamJoinApply);
		return RestData.success();
	}

	/**
	 * 申请列表
	 * @param request
	 * @return
	 */
	@GetMapping("/join-apply-list")
	public RestData<List<TeamJoinApplyResult>> joinApplyList(HttpServletRequest request) {
		List<TeamJoinApplyResult> teamJoinApplyResults = teamService.joinApplyList(TokenUtil.userId(request));
		return RestData.success(teamJoinApplyResults);
	}

	/**
	 * 新增相关链接
	 * @param teamLink
	 * @return
	 */
	@PostMapping("/link")
	public RestData<String> saveLink(@RequestBody TeamLink teamLink) {
		teamService.saveLink(teamLink);
		return RestData.success();
	}

	/**
	 * 删除相关链接
	 * @param teamLink
	 * @return
	 */
	@PostMapping("/link/del")
	public RestData<String> delLink(@RequestBody TeamLink teamLink) {
		teamService.delLink(teamLink);
		return RestData.success();
	}

}
