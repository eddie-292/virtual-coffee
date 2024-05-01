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

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.exception.BisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Eddie·ZeBingBao
 * @date: 2024/4/6 22:38
 */
@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.profiles.active}")
    private String env;

    //邮箱验证码缓存前缀
    public static final String EMAIL_CODE_PREFIX = "email_code_";

    //发送系统事件通知邮件
    public void sendSystemEventEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            //com.sun.mail.smtp.SMTPAddressFailedException: 550 User not found: louyulv@163.com
            log.error(e.getMessage(), e);
            throw new BisException("发送失败，请检查邮箱是否正确");
        }
    }


    public void sendVerificationCode(String to) {
        String code = RandomUtil.randomNumbers(4);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Kafeih Email Verification");
        message.setText("Your Kafeih verification code is: " + code);

        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            //com.sun.mail.smtp.SMTPAddressFailedException: 550 User not found: louyulv@163.com
            log.error(e.getMessage(), e);
            throw new BisException("发送失败，请检查邮箱是否正确");
        }

        // 将验证码存入redis
        redisTemplate.opsForValue().set(EmailService.EMAIL_CODE_PREFIX + to, code, 5, TimeUnit.MINUTES);
    }

    public boolean verifyEmailCode(String email, String code) {
        if (env.equals("dev") && "1111".equals(code)) {
            return true;
        }

        Object o = redisTemplate.opsForValue().get(EMAIL_CODE_PREFIX + email);
        if (o == null) {
            return false;
        }

        String redisCode = (String) o;
        return code.equals(redisCode);
    }

    public void deleteEmailCode(String email) {
        redisTemplate.delete(EMAIL_CODE_PREFIX + email);
    }

}
