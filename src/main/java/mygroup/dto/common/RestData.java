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

package mygroup.dto.common;

import mygroup.common.annotation.ApiModel;
import mygroup.common.annotation.ApiModelProperty;
import lombok.Data;
import mygroup.common.enums.ResultCode;

/**
 * @Description:统一返回数据类型
 */
@Data
@ApiModel(value = "返回数据")
public class RestData<T> {
	
	 @ApiModelProperty("响应编码")
	 private int code;
	 
	 @ApiModelProperty("响应信息")
	 private String msg;
	 
	 @ApiModelProperty("响应数据")
	 private T data;

	 /**
	  * @Description:仅返回成功标志
	  * @author:hutao
	  * @mail:hutao_2017@aliyun.com
	  * @date:2022年2月19日
	  */
	 public static <T> RestData<T> success() {
		 RestData<T> response = new RestData<T>();
		 response.setCode(200);
		 response.setMsg("");
		 return response;
	 }
	 
	 /**
	  * @Description:返回成功标志，并自定义成功信息
	  * @author:hutao
	  * @mail:hutao_2017@aliyun.com
	  * @date:2022年2月19日
	  */
	 public static <T> RestData<T> success(String msg) {
		 RestData<T> response = new RestData<T>();
		 response.setCode(200);
		 response.setMsg(msg);
		 return response;
	 }
	 
	 /**
	  * @Description:
	  * @author:hutao
	  * @mail:hutao_2017@aliyun.com
	  * @date:2022年2月19日
	  */
	 public static <T> RestData<T> success(int code,String msg) {
		 RestData<T> response = new RestData<T>();
		 response.setCode(code);
		 response.setMsg(msg);
		 return response;
	 }
	 
	 /**
	  * @Description:
	  * @author:hutao
	  * @mail:hutao_2017@aliyun.com
	  * @date:2022年2月19日
	  */
	 public static <T> RestData<T> success(T t) {
		 RestData<T> response = new RestData<T>();
		 response.setCode(200);
		 response.setMsg("");
		 response.setData(t);
		 return response;
	 }
	 
	 /**
	  * @Description:
	  * @author:hutao
	  * @mail:hutao_2017@aliyun.com
	  * @date:2022年2月19日
	  */
	 public static <T> RestData<T> error() {
		 RestData<T> response = new RestData<>();
		 response.setCode(500);
		 response.setMsg("系统异常");
		 return response;
	 }
	 
	 /**
	  * @Description:
	  * @author:hutao
	  * @mail:hutao_2017@aliyun.com
	  * @date:2022年2月19日
	  */
	 public static <T> RestData<T> error(String msg) {
		 RestData<T> response = new RestData<>();
		 response.setCode(500);
		 response.setMsg(msg);
		 return response;
	 }
	
	 /**
	  * @Description:
	  * @author:hutao
	  * @mail:hutao_2017@aliyun.com
	  * @date:2022年2月19日
	  */
	 public static <T> RestData<T> error(int code,String msg) {
		 RestData<T> response = new RestData<>();
		 response.setCode(code);
		 response.setMsg(msg);
		 return response;
	 }

	/**
	 * 操作超时
	 */
	public static <T> RestData<T> tokenFailed(String msg) {
		RestData<T> response = new RestData<>();
		response.setCode(ResultCode.TOKEN_FAILED.getCode());
		response.setMsg(msg);
		return response;
	}

}
