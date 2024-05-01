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

package mygroup.config;


import mygroup.common.constant.QueueConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eddie.BaoZeBing
 * @date 2023/10/12 21:00
 */
@Configuration
public class RabbitConfig {

    @Bean
    public Queue codeSnippet() {
        return new Queue(QueueConstant.Code_Snippet);
    }

    @Bean
    public Queue userFavorite() {
        return new Queue(QueueConstant.User_Favorite);
    }

    @Bean
    public Queue contentReport() {
        return new Queue(QueueConstant.Content_Report);
    }

    @Bean
    public Queue codeSnippetDelete() {
        return new Queue(QueueConstant.Code_Snippet_Delete);
    }

    @Bean
    public Queue codeSnippetRecharge() {
        return new Queue(QueueConstant.Code_Snippet_Recharge);
    }

    @Bean
    public Queue articleAdd() {
        return new Queue(QueueConstant.Article_Add);
    }

    @Bean
    public Queue articleDel() {
        return new Queue(QueueConstant.Article_Del);
    }

    @Bean
    public Queue articleUpdate() {
        return new Queue(QueueConstant.Article_Update);
    }

    @Bean
    public Queue articleAppend() {
        return new Queue(QueueConstant.Article_Append);
    }

    @Bean
    public Queue userFollow() {
        return new Queue(QueueConstant.User_Follow);
    }

    @Bean
    public Queue joinTeam() {
        return new Queue(QueueConstant.Join_Team);
    }

    @Bean
    public Queue dailyRewards() {
        return new Queue(QueueConstant.Daily_Rewards);
    }

    @Bean
    public Queue economy() {
        return new Queue(QueueConstant.Economy);
    }

    @Bean
    public Queue teamJoinApply() {
        return new Queue(QueueConstant.Team_Join_Apply);
    }

    @Bean
    public Queue userStarMsg() {
        return new Queue(QueueConstant.User_Star_Msg);
    }

    @Bean
    public Queue view() {
        return new Queue(QueueConstant.View);
    }

    @Bean
    public Queue articleRecharge() {
        return new Queue(QueueConstant.Article_Recharge);
    }

    @Bean
    public Queue comment() {
        return new Queue(QueueConstant.Comment);
    }

    @Bean
    public Queue commentLike() {
        return new Queue(QueueConstant.Comment_Like);
    }

    @Bean
    public Queue systemEvent() {
        return new Queue(QueueConstant.System_Event);
    }

}
