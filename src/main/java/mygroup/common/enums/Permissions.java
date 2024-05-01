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

package mygroup.common.enums;

/**
 * @author Eddie.BaoZeBing
 * @date 2024/4/12 20:50
 */
public enum Permissions {

    /*
    ALL_PERMISSIONS	全部权限
    CREATE_GROUP	创建Group
    DELETE_FEED	删除Feed
    DELETE_TOPIC	删除主题
    PUBLISH_FEED	发布Feed
    PUBLISH_TOPIC	发布主题
    REPLY	回复
    POST_NOTICE 发布通知
    move_feed    移动FEED
     */
    ALL_PERMISSIONS("全部权限", "ALL_PERMISSIONS"),
    CREATE_GROUP("创建Group", "CREATE_GROUP"),
    DELETE_FEED("删除Feed", "DELETE_FEED"),
    DELETE_TOPIC("删除主题", "DELETE_TOPIC"),
    PUBLISH_FEED("发布Feed", "PUBLISH_FEED"),
    PUBLISH_TOPIC("发布主题", "PUBLISH_TOPIC"),
    REPLY("回复", "REPLY"),
    MOVE_FEED("移动FEED", "MOVE_FEED"),
    POST_NOTICE("发布通知", "POST_NOTICE");



    private String name;

    private String code;

    Permissions(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public static Permissions getPermissionByCode(String code) {
        for (Permissions permission : Permissions.values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        return null;
    }

}
