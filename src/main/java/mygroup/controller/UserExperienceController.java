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

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import mygroup.common.annotation.Free;
import mygroup.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;




import mygroup.dto.common.ConditionQuery;
import mygroup.dto.common.PageQuery;
import mygroup.dto.common.RestData;

import mygroup.entity.UserExperience;
import mygroup.service.IUserExperienceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:用户经历表控制器
 * @author Eddie
 * @date 2024-02-07
 */
@RestController
@RequestMapping("/experience")
public class UserExperienceController {
	@Autowired
	private IUserExperienceService userExperienceService;
	@Autowired
	private HttpServletRequest request;
	
	/**
 	* @description:新增用户经历表
    * @author Eddie
    * @date 2024-02-07
    */
	@PostMapping("/save")
	public RestData<String> saveUserExperienceInfo(@RequestBody UserExperience entity) {
		String companyName = entity.getCompanyName();
		Date startWorkTime = entity.getStartWorkTime();
		Date endWorkTime = entity.getEndWorkTime();

		if (companyName == null || startWorkTime == null || endWorkTime == null) {
			return RestData.error("请填写完整信息");
		}
		if (companyName.length() > 30) {
			return RestData.error("公司名称不能超过30个字符");
		}

		entity.setUserId(TokenUtil.userId(request));
		entity.setUpdateTime(new Date());
		userExperienceService.save(entity);
		return RestData.success();
	}
	
	/**
 	* @description:按照主键删除用户经历表
    * @author Eddie
    * @date 2024-02-07
    */
	@PostMapping("/remove")
	public RestData<String> removeUserExperienceInfoByKey(@RequestBody List<String> ids) {
		userExperienceService.remove(new LambdaQueryWrapper<UserExperience>()
				.in(UserExperience::getExperienceId, ids)
				.eq(UserExperience::getUserId, TokenUtil.userId(request)));
		return RestData.success();
	}

	/**
 	* @description:按照条件进行查询用户经历表
    * @author Eddie
    * @date 2024-02-07
    */
	@Free
	@PostMapping("/list")
	public RestData<List<UserExperience>> listUserExperienceInfoByCondition(@RequestBody ConditionQuery conditionQuery) {
		List<UserExperience> list = userExperienceService.queryByCondition(conditionQuery);
		return RestData.success(list);
	}

}
