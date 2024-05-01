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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mygroup.util.WebsiteConfigUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

/**
 * 星星配置
 * @author Eddie.BaoZeBing
 * @date 2023/10/21 17:48
 */
@Data
@Slf4j
@Component
public class StarConfig {

    // 注册完成奖励
    //@Value("${star.register}")
    private Double register;

    //每日登录奖励
    //@Value("${star.dailyRewards}")
    private Double dailyRewards;

    //发布文章花费
    //@Value("${star.publishArticle}")
    private Double publishArticle;

    //发布代码块花费
    //@Value("${star.publishCode}")
    private Double publishCode;

    //创建组花费
    //@Value("${star.createGroup}")
    private Double createGroup;

    //打赏给文章
    //@Value("${star.rewardArticle}")
    private Double rewardArticle;

    //打赏给代码块
    //@Value("${star.rewardCode}")
    private Double rewardCode;

    /*
    #  本站星星使用场景有：
        star:
          register: 200 #注册完成奖励
          dailyRewards: 10 #每日登录奖励
          publishArticle: 3 #发布文章花费
          publishCode: 1 #发布代码块花费
          createGroup: 5 #创建组花费
          rewardArticle: 1 #打赏给文章
          rewardCode: 1 #打赏给代码块
     */

    public Double getDailyRewards() {
        Object val = WebsiteConfigUtil.getWebsiteConfig("star.dailyRewards");
        return Double.valueOf(ObjectUtils.defaultIfNull(val, 0.0).toString());
    }

    public Double getRegister() {
        Object val = WebsiteConfigUtil.getWebsiteConfig("star.register");
        return Double.valueOf(ObjectUtils.defaultIfNull(val, 0.0).toString());
    }

    public Double getPublishArticle() {
        Object val = WebsiteConfigUtil.getWebsiteConfig("star.publishArticle");
        return Double.valueOf(ObjectUtils.defaultIfNull(val, 0.0).toString());
    }

    public Double getPublishCode() {
        Object val = WebsiteConfigUtil.getWebsiteConfig("star.publishCode");
        return Double.valueOf(ObjectUtils.defaultIfNull(val, 0.0).toString());
    }

    public Double getCreateGroup() {
        Object val = WebsiteConfigUtil.getWebsiteConfig("star.createGroup");
        return Double.valueOf(ObjectUtils.defaultIfNull(val, 0.0).toString());
    }

    public Double getRewardArticle() {
        Object val = WebsiteConfigUtil.getWebsiteConfig("star.rewardArticle");
        return Double.valueOf(ObjectUtils.defaultIfNull(val, 0.0).toString());
    }

    public Double getRewardCode() {
        Object val = WebsiteConfigUtil.getWebsiteConfig("star.rewardCode");
        return Double.valueOf(ObjectUtils.defaultIfNull(val, 0.0).toString());
    }

}
