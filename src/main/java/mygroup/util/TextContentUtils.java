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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Eddie·ZeBingBao
 * @date: 2024/4/25 15:01
 */
@Slf4j
public class TextContentUtils {

    public static String atLink = " <strong><a class='link-success link-underline link-underline-opacity-0 at-member' target='_blank' href='/member/%s'>@%s</a></strong> ";
    public static String topicLink = " <a class='link-success link-underline link-underline-opacity-0 at-member' target='_blank' href='/t/%s'>%s</a> ";
    public static String groupLink = " <a class='link-success link-underline link-underline-opacity-0 at-member' target='_blank' href='/team-details/%s'>%s</a> ";

    public static String getGroupLink(String id, String name) {
        return String.format(groupLink, id, name);
    }

    public static String getTopicLink(String id, String name) {
        return String.format(topicLink, id, name);
    }

    public static String getAtLink(String id, String name) {
        return String.format(atLink, id, name);
    }

    /**
     * @[\u4e00-\u9fa5\w-]+
     * 匹配中文、英文、数字以及连字符（-）的用户名。[\u4e00-\u9fa5] 匹配中文字符，\w 匹配任何字母、数字和下划线，- 匹配连字符。
     * 而+ 表示匹配前面的字符至少一次，这个正则表达式用来验证用户名是否符合特定格式。
     * 提取content中@的用户，以@开头，后面跟中文、英文、数字、下划线、横杠，以空格结尾
     * @param content
     * @return
     */
    public static Set<String> atUser(String content) {
        try {
            if (StringUtils.isBlank(content)) {
                return null;
            }
            Set<String> names = new HashSet<>();
            String atPattern = "@[\\u4e00-\\u9fa5\\w-]+";
            Matcher matcher = Pattern.compile(atPattern).matcher(content);
            while (matcher.find()) {
                //去除@符号
                names.add(matcher.group().trim().replaceAll("@", ""));
            }
            return names;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
