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

package mygroup.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import mygroup.common.annotation.ApiModel;
import mygroup.common.annotation.ApiModelProperty;
import com.baomidou.mybatisplus.annotation.IdType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;

 /**
 * @description:用户信息数据表：user_info表的持久类
 * @author Eddie
 * @date 2024-02-07
 */
@Data
@TableName("user_info")
@ApiModel(value = "UserInfo对象", description = "用户信息")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @ApiModelProperty("昵称")
    @TableField("nickname")
    private String nickname;

    @ApiModelProperty("用户名")
    @TableField("username")
    private String username;

    @ApiModelProperty("使用的邀请码")
    @TableField("invitation_code")
    private String invitationCode;

    @ApiModelProperty("生日")
    @TableField("birthday")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;

    @ApiModelProperty("头像")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty("主页背景图")
    @TableField("home_background_image")
    private String homeBackgroundImage;

    @ApiModelProperty("简介")
    @TableField("introduction")
    private String introduction;

    @ApiModelProperty("手机号")
    @TableField("phone_number")
    private String phoneNumber;

    @ApiModelProperty("邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty("注册时间")
    @TableField("register_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerTime;

    @ApiModelProperty("所在城市")
    @TableField("city")
    private String city;

    @ApiModelProperty("职业")
    @TableField("occupation")
    private String occupation;

    @ApiModelProperty("情感状态")
    @TableField("emotional_status")
    private String emotionalStatus;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("发帖数量")
    @TableField("post_count")
    private Integer postCount;

    @ApiModelProperty("追随者数量")
    @TableField("follower_count")
    private Integer followerCount;

    @ApiModelProperty("账号状态")
    @TableField("account_status")
    private Integer accountStatus;

    @ApiModelProperty("关注数量")
    @TableField("following_count")
    private Integer followingCount;

    @ApiModelProperty("登录密码")
    @TableField("password")
    private String password;

     @ApiModelProperty("经济")
     @TableField("economy")
    private BigDecimal economy;

    @ApiModelProperty("是否关注")
    @TableField(exist = false)
    private boolean followed;

     @ApiModelProperty("会员编号")
     @TableField("member_number")
    private Integer memberNumber;

     @ApiModelProperty("订阅用户")
     @TableField("subscriber")
    private Integer subscriber;

     @ApiModelProperty("用户角色")
     @TableField("user_role")
     private Integer userRole;

     @ApiModelProperty("0关1开")
     @TableField("public_feeds")
     private Integer publicFeeds;

     @ApiModelProperty("在首页显示最近的Topics：0关1开")
     @TableField("public_topics")
     private Integer publicTopics;
}
