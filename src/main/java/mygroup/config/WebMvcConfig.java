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
import mygroup.config.interceptor.AuthenticationInterceptor;
import mygroup.config.interceptor.RequestLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${server.allowedOriginPatterns}")
    private String allowedOriginPatterns;

    @Autowired
    private RequestLimitInterceptor requestLimitInterceptor;
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        if (allowedOriginPatterns == null || allowedOriginPatterns.isEmpty()) {
            log.info("允许的来源模式未配置, 已设置为 *");
            allowedOriginPatterns = "*";
        }

        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns)

                //限制允许的HTTP方法，防止恶意请求使用不支持的方法。
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")

                //禁用凭证支持：如果API不需要处理cookie或认证信息，可以将allowCredentials设置为false。这样可以防止恶意请求携带敏感信息。
                .allowCredentials(false)

                //设置适当的预检请求缓存时间，这样可以减少不必要的预检请求，提高性能。
                .maxAge(3600)

                //限制允许的请求头：防止恶意请求使用不支持的请求头。
                .allowedHeaders("*");

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLimitInterceptor)
                .excludePathPatterns("/socket/**")
                .excludePathPatterns("/oauth/**")
                .excludePathPatterns("/error");

        registry.addInterceptor(authenticationInterceptor)
                .excludePathPatterns("/socket/**")
                .excludePathPatterns("/oauth/**")
                .excludePathPatterns("/user/sign-up", "/user/sign-in", "/user/sign-out");
    }

}
