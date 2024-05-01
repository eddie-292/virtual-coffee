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

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONObject;
import mygroup.common.exception.BisException;
import mygroup.config.UpyunConfig;
import mygroup.dto.common.RestData;
import mygroup.entity.Team;
import mygroup.util.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Eddie.BaoZeBing
 * @date 2024/3/16 19:39
 */
@RestController
@RequestMapping("/upyun")
public class UPYunController {

    @Autowired
    private UpyunConfig config;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 上传图到又拍云之前获取签名和其他信息
     * @return
     * @throws Exception
     */
    public RestData<Map<String, String>> getConfig() throws Exception {
        Long userId = TokenUtil.userId(request);
        JSONObject policyJson = new JSONObject();
        policyJson.put("bucket", config.getBucketName());
        policyJson.put("save-key", config.getPath() + "/" + userId + "/{filename}{.suffix}");
        policyJson.put("expiration", new Date().getTime() + 3600);

        String policy = Base64.getEncoder().encodeToString(policyJson.toString().getBytes(StandardCharsets.UTF_8));
        String md5Password = md5(config.getPassword());
        String signatureData = "POST&/" + config.getBucketName() + "&" + policy;
        String signatureStr = hmacSha1(signatureData, md5Password);
        String signature = "UPYUN " + config.getUserName() + ":" + signatureStr;

        Map<String, String> result = new HashMap<>();
        result.put("signature", signature);
        result.put("policy", policy);
        result.put("url", "https://v0.api.upyun.com/" + config.getBucketName());
        result.put("baseUrl", config.getDomain());
        return RestData.success(result);
    }

    /**
     * 获取头像上传配置
     * 存在使用冷却时间
     * @return
     * @throws Exception
     */
    @GetMapping("/config/avatar")
    public RestData<Map<String, String>> getAvatarConfig(String t) throws Exception {
        List<String> changeTypes = new ArrayList<>();
        changeTypes.add("a"); // 头像
        changeTypes.add("h"); // 背景图
        if (StringUtils.isBlank(t) || !changeTypes.contains(t)) {
            throw new BisException("参数错误");
        }

        Long userId = TokenUtil.userId(request);
        String key = "upyun:" +t+ ":" + userId;
        if (redisTemplate.hasKey(key)) {
            Long lastUseTime = Long.valueOf(redisTemplate.opsForValue().get(key).toString()) ;
            long coolingTimeRemaining = System.currentTimeMillis() - lastUseTime;

            //计算coolingTimeRemaining是多少分钟
            long minutes = TimeUnit.MILLISECONDS.toMinutes(coolingTimeRemaining);
            return RestData.error("请" + (config.getInterval() - minutes) + "分钟后再试");
        }
        
        RestData<Map<String, String>> result = getConfig();
        
        //记录使用时间，每次使用需要间隔一段时间
        redisTemplate.opsForValue().set(
                key,
                String.valueOf(System.currentTimeMillis()),
                config.getInterval(),
                TimeUnit.MINUTES);
        return result;
    }

    /**
     * 获取编辑器上传配置
     * @return
     * @throws Exception
     */
    @GetMapping("/config/editor")
    public RestData<Map<String, String>> getEditorConfig() throws Exception {
        RestData<Map<String, String>> result = getConfig();
        return result;
    }

    public static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static String hmacSha1(String input, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
        mac.init(secretKeySpec);
        byte[] hmac = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }

}
