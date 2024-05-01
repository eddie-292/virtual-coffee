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

package mygroup.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import mygroup.common.annotation.Free;
import mygroup.common.enums.ResultCode;
import mygroup.common.exception.BisException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Eddie.BaoZeBing
 * @date 2024/2/7 14:49
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // springboot跨域请求  放行OPTIONS请求
        if(request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())){
            return true;
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            Free free = handlerMethod.getMethodAnnotation(Free.class);
            if (null != free) {

                String token = request.getHeader("Token");
                if (StringUtils.isNotBlank(token)) {
                    Object userId = redisTemplate.opsForValue().get(token);
                    request.setAttribute("userId", userId);
                }

                return true;
            }
        }

        return this.signCheck(request, response);
    }

    private boolean signCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = request.getHeader("Token");
        //log.info("token: {}", token);

        if (StringUtils.isBlank(token)) {
            log.info("登录超时TOKEN IS NULL {}", request.getRequestURI());
            throw new BisException(ResultCode.TOKEN_FAILED);
        }

        if (!redisTemplate.hasKey(token)) {
            log.info("{} 登录超时 TOKEN ECPIRE", token);
            throw new BisException(ResultCode.TOKEN_FAILED);
        }

        Object userId = redisTemplate.opsForValue().get(token);
        request.setAttribute("userId", userId);
        return true;
    }

}
