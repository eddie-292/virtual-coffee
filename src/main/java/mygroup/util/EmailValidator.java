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

package mygroup.util;

import java.util.regex.Pattern;

/**
 * @author Eddie.BaoZeBing
 * @date 2024/4/26 22:05
 */
public class EmailValidator {
    private static final String[] COMMON_EMAIL_PROVIDERS = {
            "gmail.com", "qq.com", "163.com", "outlook.com", "sina.com",
            "sohu.com", "hotmail.com", "qq.com", "126.com", "aliyun.com"
    };

    public static boolean isValidEmail(String email) {
        // 简单的email格式检查
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$");
        if (!emailPattern.matcher(email).matches()) {
            return false;
        }

        // 检查邮箱是否来自于常用的服务商
        for (String provider : COMMON_EMAIL_PROVIDERS) {
            if (email.endsWith(provider)) {
                return true; // 常用服务商，有效
            }
        }

        return false; // 不是常用服务商或无效的email格式
    }

    public static void main(String[] args) {
        String email = "exaasdmple@xxx.cc"; // 将这里的email替换为你想要检查的邮箱

        if (isValidEmail(email)) {
            System.out.println("有效的邮箱来自于常用服务商！");
        } else {
            System.out.println("无效的邮箱或不是来自于常用服务商");
        }
    }

}
