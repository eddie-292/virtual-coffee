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

package mygroup.config;

import lombok.extern.slf4j.Slf4j;
import mygroup.common.enums.ResultCode;
import mygroup.common.exception.BisException;
import mygroup.common.exception.PermissionDeniedException;
import mygroup.dto.common.RestData;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public RestData<String> exceptionHandler(HttpServletRequest httpServletRequest, Exception e) {
        String erroMsg = String.format("来至 %s 的请求发生以下错误", httpServletRequest.getRequestURL());
        log.error(erroMsg, e);
        return RestData.error("系统未知问题，请稍后重试");
    }

    @ResponseBody
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public RestData<String> httpMessageNotReadableException(HttpServletRequest httpServletRequest, HttpMessageNotReadableException e) {
        log.error(e.getMessage(), e);
        return RestData.error("输入的信息格式不正确");
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public RestData<String> IllegalArgumentException(HttpServletRequest httpServletRequest, Exception e) {
        String erroMsg = String.format("来至 %s 的请求发生以下错误", httpServletRequest.getRequestURL());
        log.error(erroMsg, e);
        return RestData.error(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public RestData<String> HttpRequestMethodNotSupportedException(HttpServletRequest httpServletRequest, Exception e) {
        String erroMsg = String.format("来至 %s 的请求发生以下错误", httpServletRequest.getRequestURL());
        log.error(erroMsg, e);
        return RestData.error(String.format("此次请求%s不受支持", httpServletRequest.getMethod()));
    }

    @ResponseBody
    @ExceptionHandler(value = SQLIntegrityConstraintViolationException.class)
    public RestData<String> SQLIntegrityConstraintViolationException(HttpServletRequest httpServletRequest, Exception e) {
        String erroMsg = String.format("SQL 完整性约束违规异常 ", httpServletRequest.getRequestURL());
        log.error(erroMsg, e);
        return RestData.error("此空间中已存在这条数据");
    }

    @ResponseBody
    @ExceptionHandler(value = BisException.class)
    public RestData<String> businessExceptionHandler(HttpServletRequest httpServletRequest, BisException e) {
        String erroMsg = String.format("来至 %s 的请求发生以下错误", httpServletRequest.getRequestURL());
        log.error(erroMsg, e);

        if (e.getCode() == ResultCode.TOKEN_FAILED.getCode()) {
            return RestData.tokenFailed(e.getMsg());
        }

        return RestData.error(e.getMsg());
    }

    @ResponseBody
    @ExceptionHandler(value = PermissionDeniedException.class)
    public RestData<String> permissionDeniedExceptionHandler(HttpServletRequest httpServletRequest, PermissionDeniedException e) {
        String erroMsg = String.format("来至 %s 的请求发生以下错误", httpServletRequest.getRequestURL());
        log.error(erroMsg, e);
        return RestData.error(e.getMsg());
    }

}
