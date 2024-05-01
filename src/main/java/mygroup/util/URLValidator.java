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

import com.google.common.collect.Sets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eddie.BaoZeBing
 * @date 2024/4/27 13:34
 */
public class URLValidator {

    public static void main(String[] args) {
        String[] testUrls = {
                "http://www.example.com",
                "https://example.com",
                "https://hunyuan.tencent.com/bot/chat/af31f648-da5e-4254-aa5c-007947ff1f0d"
        };

        for (String url : testUrls) {
            System.out.println("URL: " + url + " is valid? " + isValidHttpOrHttpsUrl(url) + " is illegal? " + isIllegalWebsite(url));
        }
    }

    //检查是否是合法的http或https链接必须以http://或https://开头
    public static boolean isValidHttpOrHttpsUrl(String url) {
        String regex = "^https?:\\/\\/([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    // 黄色网站: 自行整理
   public static Set<String> yellowWebsites = new HashSet<>(Arrays.asList(
            "xxxx.com"
    ));

    // 盗版软件网站: 自行整理
    public static Set<String> crackWebsites = new HashSet<>(Arrays.asList(
            "xxxx.do"
    ));

    //检查非法，黄色，破解等网站关键词
    public static boolean isIllegalWebsite(String url) {
        Set<String> illegalWebsites = Sets.union(yellowWebsites, crackWebsites);

        try {
            String domain = new URL(url).getHost();
            String[] split = domain.split("\\.");
            if (split.length > 2) {
                domain = split[split.length - 2] + "." + split[split.length - 1];
            }

            return illegalWebsites.contains(domain);
        } catch (MalformedURLException e) {
            return false;
        }
    }

}
