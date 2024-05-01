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
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
* @description:文章表数据表：article表的持久类
* @author Eddie
* @date 2024-02-07
*/
@Data
public class ArticleDoc implements Serializable {

   private static final long serialVersionUID = 1L;

   @MongoId
   private String articleId;

   @ApiModelProperty("创建者的用户ID")
   @JsonSerialize(using = ToStringSerializer.class)
   private Long creatorUserId;

   @ApiModelProperty("创建者的用户名")
   private String creatorUsername;

   @ApiModelProperty("创建时间")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
   private Date createTime;

   @ApiModelProperty("更新时间")
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
   private Date updateTime;

   @ApiModelProperty("标题")
   private String title;

   @ApiModelProperty("文章内容")
   private String content;

   @ApiModelProperty("文章摘要")
   private String summary;

   @ApiModelProperty("分类")
   private String category;

   @ApiModelProperty("分类标题")
   private String categoryTitle;

   @ApiModelProperty("文章主图URL")
   private String mainImageUrl;

   @ApiModelProperty("浏览量")
   private Integer view;

   @ApiModelProperty("发电量")
   private Integer batteryLevel;

   @ApiModelProperty("附言数")
   private Integer appendCount = 0;

   @ApiModelProperty("内容类型 0:markdown 1:富文本")
   private Integer type;

   @ApiModelProperty("平台")
   private String platform;

   private boolean favorite;
   private boolean update;
   private boolean append;
   private String avatar;
   private String nickname;
   private List<ArticleAppendDoc> appendList;

}
