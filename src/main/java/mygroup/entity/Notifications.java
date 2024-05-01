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
 * @description:通知表：notifications表的持久类
 * @author Eddie
 * @date 2024-02-07
 */
@Data
@TableName("notifications")
@ApiModel(value = "Notifications对象", description = "通知")
public class Notifications implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "notification_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long notificationId;

    @ApiModelProperty("发送者ID")
    @TableField("sender_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;

    @ApiModelProperty("接收者ID，0表示所有人")
    @TableField("receiver_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverId;

    @ApiModelProperty("标题")
    @TableField("title")
    private String title;

    @ApiModelProperty("内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("创建时间")
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    @ApiModelProperty("是否已读，0未读")
    @TableField("is_read")
    private Integer isRead;

}