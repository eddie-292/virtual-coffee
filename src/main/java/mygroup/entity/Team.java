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
import mygroup.dto.LastPostInGroupDTO;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Eddie
 * @description:团队数据表：team表的持久类
 * @date 2024-02-07
 */
@Data
@TableName("team")
@ApiModel(value = "Team对象", description = "团队")
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "team_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long teamId;

    @ApiModelProperty("创建者的用户ID")
    @TableField("creator_user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long creatorUserId;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("团队名称")
    @TableField("team_name")
    private String teamName;

    @ApiModelProperty("团队图片url")
    @TableField("team_image_url")
    private String teamImageUrl;

    @ApiModelProperty("主页背景图")
    @TableField("home_background_image")
    private String homeBackgroundImage;

    @ApiModelProperty("公开还是私有")
    @TableField("is_public")
    private Boolean isPublic;

    @ApiModelProperty("团队描述")
    @TableField("team_description")
    private String teamDescription;

    @ApiModelProperty("团队人数")
    @TableField("team_member_count")
    private Integer teamMemberCount;

    @ApiModelProperty("每日发帖数")
    @TableField("daily_post_count")
    private Integer dailyPostCount;

    @ApiModelProperty("是否加入了")
    @TableField(exist = false)
    private boolean join = false;

    @TableField(exist = false)
    private boolean favorite;

    @TableField(exist = false)
    private List<TeamLink> teamLinks;

    @TableField(exist = false)
    private LastPostInGroupDTO lastPost;
}
