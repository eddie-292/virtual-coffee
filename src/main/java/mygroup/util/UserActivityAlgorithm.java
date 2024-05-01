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

/**
 * @author Eddie.BaoZeBing
 * @date 2024/4/12 19:55
 */
public class UserActivityAlgorithm {

    // 用户登录频率
    private int loginFrequency;

    // 用户发帖数量
    private int postCount;

    // 用户评论数量
    private int commentCount;

    public UserActivityAlgorithm(int loginFrequency, int postCount, int commentCount) {
        this.loginFrequency = loginFrequency;
        this.postCount = postCount;
        this.commentCount = commentCount;
    }

    // 计算用户活跃度
    public double calculateActivity() {
        // 这里可以根据实际需求设置权重
        double loginWeight = 0.4;
        double postWeight = 0.3;
        double commentWeight = 0.3;

        // 进行活跃度计算
        double activity = loginWeight * loginFrequency +
                postWeight * postCount +
                commentWeight * commentCount;

        return activity;
    }

    public static void main(String[] args) {
        // 示例：创建一个用户并设置登录频率、发帖数量和评论数量
        UserActivityAlgorithm user = new UserActivityAlgorithm(10, 5, 20);

        // 计算用户活跃度
        double activity = user.calculateActivity();
        System.out.println("用户活跃度为：" + activity);
    }
}

