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

package mygroup.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import mygroup.common.annotation.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Eddie.BaoZeBing
 * @date 2024/2/7 16:00
 */
@Data
public class SigninSuccessDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("生日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;

    @ApiModelProperty("头像")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty("主页背景图")
    private String homeBackgroundImage;

    @ApiModelProperty("简介")
    private String introduction;

    @ApiModelProperty("手机号")
    private String phoneNumber;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerTime;

    @ApiModelProperty("所在城市")
    private String city;

    @ApiModelProperty("职业")
    private String occupation;

    @ApiModelProperty("情感状态")
    private String emotionalStatus;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("发帖数量")
    private Integer postCount;

    @ApiModelProperty("追随者数量")
    private Integer followerCount;

    @ApiModelProperty("账号状态")
    private Integer accountStatus;

    @ApiModelProperty("关注数量")
    private Integer followingCount;

    @ApiModelProperty("Token")
    private String token;

    @ApiModelProperty("经济")
    private BigDecimal economy = new BigDecimal(0);

    @ApiModelProperty("是否领取了每日奖励")
    private boolean dailyRewards = false;

    @ApiModelProperty("会员编号")
    private Integer memberNumber;

    @ApiModelProperty("是否关注")
    private boolean followed;

    @ApiModelProperty("订阅用户")
    private boolean subscriberMark;

    @ApiModelProperty("在首页显示最近的Feeds：0关1开")
    private Integer publicFeeds;

    @ApiModelProperty("在首页显示最近的Topics：0关1开")
    private Integer publicTopics;

}
