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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 摘要提取器
 * @author Eddie.BaoZeBing
 * @date 2024/2/16 15:27
 */
public class SummaryExtractor {

    public static void main(String[] args) {
        String htmlContent = "";

        String summary = extractSummary(htmlContent);
        System.out.println(summary);
    }

    public static String extractSummary(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        int limit = 150;

        // 提取所有段落文本
        Elements paragraphs = document.getAllElements();
        StringBuilder summaryBuilder = new StringBuilder();
        String text = "";

        // 将前三个段落添加到摘要中
        for (int i = 0; i< Math.min(3, paragraphs.size()); i++) {
            Element paragraph = paragraphs.get(i);
            summaryBuilder.append(paragraph.text());
            if (i < 2) {
                summaryBuilder.append(" ");
            }

            if (summaryBuilder.toString().length() > limit) {
                text = summaryBuilder.substring(0, limit).toString() + "...";
                break;
            }
        }

        if (text.isEmpty()) {
            text = summaryBuilder.toString();
        }

        return removeNonChineseCharacters(text);
    }

    public static String removeNonChineseCharacters(String str) {
        // 创建正则表达式，匹配非中文或英文的字符
        Pattern pattern = Pattern.compile("^[^a-zA-Z\u4e00-\u9fa5]+");
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(str);
        // 替换所有匹配的子字符串
        return matcher.replaceFirst("");
    }

}
