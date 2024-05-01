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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Eddie·ZeBingBao
 * @date: 2024/4/18 13:41
 */
@Slf4j
public class NicknameUtils {

    //特殊字符
    private static final String SPECIAL_CHARACTERS = "~!@#$%^&*()+`-=[]\\{}|;':\",./<>?";

    //敏感词
    private static final String SENSITIVE_WORDS = "system,admin,root,super,xjp,mzd,ccp,gcd";

    //系统保留词
    private static final String SYSTEM_RESERVED_WORDS = "system,admin,root,super,xjp,mzd,ccp,gcd";

    //最小长度
    private static final int MIN_LENGTH = 4;

    //最大长度
    private static final int MAX_LENGTH = 16;

    //检查用户吗是否包含特色字符、长度是否符合要求、是否已经存在、是否是敏感词、是否是系统保留词
    public static Set<String> checkNickname(String nickname) {
        nickname = nickname.toLowerCase();
        Set<String> returnMap = new HashSet<>();

        //检查长度
        if (nickname.length() < MIN_LENGTH || nickname.length() > MAX_LENGTH) {
            returnMap.add("用戶名长度不符合要求，长度在4-16之间");
        }

        //检查特殊字符
        for (int i = 0; i < nickname.length(); i++) {
            if (SPECIAL_CHARACTERS.contains(nickname.substring(i, i + 1))) {
                returnMap.add("用戶名不能包含特殊字符");
            }
        }

        //检查敏感词
        if (SENSITIVE_WORDS.contains(nickname)) {
            returnMap.add("用戶名不可用");
        }

        //检查系统保留词
        if (SYSTEM_RESERVED_WORDS.contains(nickname)) {
            returnMap.add("用戶名不可用");
        }

        //由英文或者數字組成
        if (!nickname.matches("^[a-zA-Z0-9]+$")) {
            returnMap.add("用户名只能由英文或者数字组成");
        }

        return returnMap;
    }

    public static void main(String[] args) {
        Set<String> strings = checkNickname("12312321_");
        log.info(strings.toString());
    }

}
