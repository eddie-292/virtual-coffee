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

import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.annotation.Free;
import mygroup.common.annotation.RequestLimit;
import mygroup.common.constant.SystemConstant;
import mygroup.common.exception.BisException;
import mygroup.dto.SystemEventDTO;
import mygroup.service.impl.SystemEventService;
import mygroup.util.ReqUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RequestLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SystemEventService systemEventService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // springboot跨域请求  放行OPTIONS请求
        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            return true;
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            //使用此注解的接口自由访问
            Free free = handlerMethod.getMethodAnnotation(Free.class);
            if (free != null) {
                return true;
            }

            //请求者IP
            String ipAddr = ServletUtil.getClientIP(request);

            //查看请求者IP是否在黑名单中
            if (redisTemplate.hasKey(ReqUtil.getBlacklistKey(ipAddr, request.getRequestURI()))) {
                //获取黑名单的过期时间
                String time = (String) redisTemplate.opsForValue().get(ReqUtil.getBlacklistKey(ipAddr, request.getRequestURI()));
                //计算黑名单剩余时间
                long remainTime = Long.parseLong(time) - System.currentTimeMillis();
                //返回剩余时间
                throw new BisException("操作频繁，已被限制访问，请在" + TimeUnit.MILLISECONDS.toMinutes(remainTime) + "分钟后重试");
            }

            // 获取RequestLimit注解
            RequestLimit requestLimit = handlerMethod.getMethodAnnotation(RequestLimit.class);
            if (requestLimit == null) {
                return true;
            }

            //限制的时间范围
            int timePeriod = requestLimit.second();
            //时间内的 最大次数
            int maxRequests = requestLimit.maxCount();
            //提示信息
            String msg = requestLimit.msg();

            // 存储key
            String key = ipAddr + ":" + handlerMethod.getMethod().getName();
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(timePeriod));
                return true;
            } else {
                int currentRequests = Integer.parseInt(value.toString());
                if (currentRequests < maxRequests) {
                    redisTemplate.opsForValue().increment(key, 1L);
                    return true;
                } else {

                    //记录访问者IP，如果持续频繁访问则现在访问，每次处罚时间加倍
                    String punishKey = ReqUtil.getPunishKey(ipAddr);
                    Object punishValue = redisTemplate.opsForValue().get(punishKey);
                    if (punishValue == null) {
                        redisTemplate.opsForValue().set(punishKey, "1", Duration.ofSeconds(60));
                    } else {
                        int punishCount = Integer.parseInt(punishValue.toString());
                        if (punishCount < 10) {
                            redisTemplate.opsForValue().increment(punishKey, 1L);
                        } else {
                            //60秒内超过10次，记录访问者IP为黑名单，当前接口1小时内不允许访问
                            String blacklistKey = ReqUtil.getBlacklistKey(ipAddr, request.getRequestURI());
                            //当前时间+1小时 作为黑名单的过期时间
                            long time = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
                            redisTemplate.opsForValue().set(blacklistKey, String.valueOf(time), Duration.ofHours(1));
                            //记录IP处罚次数
                            SystemEventDTO systemEventDTO = new SystemEventDTO();
                            systemEventDTO.setType(SystemConstant.BLACKLIST);
                            systemEventDTO.setIp(ipAddr);
                            systemEventDTO.setCount(1);
                            systemEventDTO.setCreateTime(new Date());
                            systemEventDTO.setUri(request.getRequestURI());
                            systemEventDTO.setRemark("因为频繁访问限制超过10次，IP被记录为黑名单，当前接口1小时内不允许访问");
                            systemEventService.sendEvent(systemEventDTO);
                            throw new BisException("操作频繁，已被限制访问");
                        }
                    }

                    throw new BisException(msg);
                }
            }
        }
        return true;
    }

}
