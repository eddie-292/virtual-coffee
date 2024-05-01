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

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import mygroup.common.annotation.ApiModel;
import mygroup.common.annotation.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
* @description:Feed数据表：code_snippet表的持久类
* @author Eddie
* @date 2024-02-07
*/
@Data
public class CodeSnippetDoc implements Serializable {

   private static final long serialVersionUID = 1L;

   @MongoId
   private String id;

   @ApiModelProperty("用户ID")
   @JsonSerialize(using = ToStringSerializer.class)
   private Long userId;

   @ApiModelProperty("创建时间")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
   private Date createTime;

   @ApiModelProperty("更新时间")
   @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
   private Date updateTime;

   @ApiModelProperty("发电量")
   private Integer batteryLevel;

   @ApiModelProperty("Feed内容")
   private String content;

   @ApiModelProperty("位置")
   private String position;

   @ApiModelProperty("teamId")
   private String teamId;

   @ApiModelProperty("私有组=1，非私有组=0")
   private Integer privateGroup = 0;

   @ApiModelProperty("垃圾信息=1，非垃圾信息=0")
   private Integer spam = 0;

   @ApiModelProperty("平台")
   private String platform;


   // 关联帖子
   private String articles;
   private String author;
   private boolean subscriberMark;
   private String authorDesc;
   private String authorImg;
   private boolean favorite;
   private boolean anonymize;

}
