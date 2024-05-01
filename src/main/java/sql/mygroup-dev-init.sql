SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `activity_id` int(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '描述',
  `date` date NULL DEFAULT NULL COMMENT '日期',
  `time` time(0) NULL DEFAULT NULL COMMENT '时间',
  `duration` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '期间',
  `location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地点',
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '附件URL',
  `creator_user_id` bigint(0) NOT NULL COMMENT '创建者的用户ID',
  `activity_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '活动分类',
  `activity_main_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '活动主图URL',
  `is_finished` tinyint(1) NOT NULL COMMENT '是否结束',
  `visitor_count` int(0) NULL DEFAULT 0 COMMENT '访客数量',
  `participant_count` int(0) NULL DEFAULT 0 COMMENT '参数人数',
  PRIMARY KEY (`activity_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `article_id` bigint(0) NOT NULL,
  `creator_user_id` bigint(0) NOT NULL COMMENT '创建者的用户ID',
  `creator_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建者的用户名',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL COMMENT '更新时间',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文章内容ID',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类',
  `main_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文章主图URL',
  PRIMARY KEY (`article_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article
-- ----------------------------

-- ----------------------------
-- Table structure for code_snippet
-- ----------------------------
DROP TABLE IF EXISTS `code_snippet`;
CREATE TABLE `code_snippet`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL COMMENT '更新时间',
  `battery_level` int(0) NULL DEFAULT 0 COMMENT '发电量',
  `code_content_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代码快内容ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码快' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of code_snippet
-- ----------------------------

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `comment_id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `publish_time` datetime(0) NOT NULL COMMENT '评论时间',
  `like_count` int(0) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `biz_id` bigint(0) NULL DEFAULT NULL COMMENT '业务ID',
  `reply_user_id` bigint(0) NULL DEFAULT NULL COMMENT '回复用户ID',
  `reply_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '回复用户',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `biz_id_idx`(`biz_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 513 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comments
-- ----------------------------

-- ----------------------------
-- Table structure for invitation_codes
-- ----------------------------
DROP TABLE IF EXISTS `invitation_codes`;
CREATE TABLE `invitation_codes`  (
  `invitation_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `is_used` int(0) NOT NULL DEFAULT 0 COMMENT '0未使用',
  `used_count` bigint(0) NOT NULL DEFAULT 0 COMMENT '使用次数',
  `producer_user_id` bigint(0) NULL DEFAULT NULL COMMENT '生产者用户ID',
  `expiration` timestamp(0) NULL DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`invitation_code`) USING BTREE,
  UNIQUE INDEX `invitation_code`(`invitation_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of invitation_codes
-- ----------------------------
INSERT INTO `invitation_codes` VALUES ('eddie', '2024-04-17 13:27:44', 1, 11, NULL, '2024-12-18 13:27:56');

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `notification_id` bigint(0) NOT NULL,
  `sender_id` bigint(0) NULL DEFAULT NULL,
  `receiver_id` bigint(0) NULL DEFAULT NULL COMMENT '0表示所有人',
  `title` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `is_read` int(0) NULL DEFAULT 0 COMMENT '0未读',
  PRIMARY KEY (`notification_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `permission_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `permission_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`permission_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------
INSERT INTO `permissions` VALUES ('ALL_PERMISSIONS', '全部权限');
INSERT INTO `permissions` VALUES ('CREATE_GROUP', '创建Group');
INSERT INTO `permissions` VALUES ('DELETE_FEED', '删除Feed');
INSERT INTO `permissions` VALUES ('DELETE_TOPIC', '删除主题');
INSERT INTO `permissions` VALUES ('MOVE_FEED', '移动FEED');
INSERT INTO `permissions` VALUES ('POST_NOTICE', '发布通知');
INSERT INTO `permissions` VALUES ('PUBLISH_FEED', '发布Feed');
INSERT INTO `permissions` VALUES ('PUBLISH_TOPIC', '发布主题');
INSERT INTO `permissions` VALUES ('REPLY', '回复');

-- ----------------------------
-- Table structure for resource_upload_record
-- ----------------------------
DROP TABLE IF EXISTS `resource_upload_record`;
CREATE TABLE `resource_upload_record`  (
  `id` bigint(0) NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'URL',
  `upload_time` datetime(0) NOT NULL COMMENT '上传时间',
  `uploader_user_id` bigint(0) NOT NULL COMMENT '上传者用户ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '资源上传记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource_upload_record
-- ----------------------------

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `role_id` int(0) NOT NULL,
  `permission_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`role_id`, `permission_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (1, 'ALL_PERMISSIONS');
INSERT INTO `role_permissions` VALUES (2, 'CREATE_GROUP');
INSERT INTO `role_permissions` VALUES (2, 'DELETE_FEED');
INSERT INTO `role_permissions` VALUES (2, 'DELETE_TOPIC');
INSERT INTO `role_permissions` VALUES (2, 'PUBLISH_FEED');
INSERT INTO `role_permissions` VALUES (2, 'PUBLISH_TOPIC');
INSERT INTO `role_permissions` VALUES (2, 'REPLY');
INSERT INTO `role_permissions` VALUES (3, 'CREATE_GROUP');
INSERT INTO `role_permissions` VALUES (3, 'DELETE_FEED');
INSERT INTO `role_permissions` VALUES (3, 'DELETE_TOPIC');
INSERT INTO `role_permissions` VALUES (3, 'PUBLISH_FEED');
INSERT INTO `role_permissions` VALUES (3, 'PUBLISH_TOPIC');
INSERT INTO `role_permissions` VALUES (3, 'REPLY');

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags`  (
  `tag_id` int(0) NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `tag_name`(`tag_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1157 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tags
-- ----------------------------
INSERT INTO `tags` VALUES (16, 'qna', '问与答');
INSERT INTO `tags` VALUES (18, 'programmer', '程序员');

-- ----------------------------
-- Table structure for team
-- ----------------------------
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team`  (
  `team_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `creator_user_id` bigint(0) NOT NULL COMMENT '创建者的用户ID',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL COMMENT '更新时间',
  `team_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '团队名称',
  `team_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '团队图片url',
  `home_background_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主页背景图',
  `is_public` tinyint(1) NOT NULL COMMENT '公开还是私有',
  `team_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '团队描述',
  `team_member_count` int(0) NULL DEFAULT 0 COMMENT '团队人数',
  `daily_post_count` int(0) NULL DEFAULT 0 COMMENT '每日发帖数',
  PRIMARY KEY (`team_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1784951545246392321 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '团队' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of team
-- ----------------------------

-- ----------------------------
-- Table structure for team_join_apply
-- ----------------------------
DROP TABLE IF EXISTS `team_join_apply`;
CREATE TABLE `team_join_apply`  (
  `id` bigint(0) NOT NULL,
  `team_id` bigint(0) NOT NULL,
  `apply_user_id` bigint(0) NOT NULL COMMENT '申请者的用户ID',
  `apply_time` datetime(0) NOT NULL COMMENT '申请时间',
  `process_result` int(0) NULL DEFAULT 0 COMMENT '2拒绝1同意',
  `process_time` datetime(0) NULL DEFAULT NULL COMMENT '处理时间',
  `team_creator_user_id` bigint(0) NOT NULL COMMENT '团队创建者的用户ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '团队加入申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of team_join_apply
-- ----------------------------

-- ----------------------------
-- Table structure for team_link
-- ----------------------------
DROP TABLE IF EXISTS `team_link`;
CREATE TABLE `team_link`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链接名称',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `team_id` bigint(0) NOT NULL,
  `user_id` bigint(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of team_link
-- ----------------------------

-- ----------------------------
-- Table structure for user_badge
-- ----------------------------
DROP TABLE IF EXISTS `user_badge`;
CREATE TABLE `user_badge`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NULL DEFAULT NULL,
  `badge_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `get_time` datetime(0) NULL DEFAULT NULL,
  `badge_name_cn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_badge
-- ----------------------------

-- ----------------------------
-- Table structure for user_experience
-- ----------------------------
DROP TABLE IF EXISTS `user_experience`;
CREATE TABLE `user_experience`  (
  `experience_id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `company_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单位名称',
  `start_work_time` date NULL DEFAULT NULL COMMENT '开始工作时间',
  `end_work_time` date NULL DEFAULT NULL COMMENT '结束工作时间',
  `update_time` datetime(0) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`experience_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 67 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户经历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_experience
-- ----------------------------

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `other_id` varchar(52) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其他业务ID',
  `favorite_time` datetime(0) NULL DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_favorite
-- ----------------------------

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NULL DEFAULT NULL,
  `followed_user_id` bigint(0) NULL DEFAULT NULL,
  `follow_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 79 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_follow
-- ----------------------------

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `home_background_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主页背景图',
  `introduction` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '简介',
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `register_time` datetime(0) NOT NULL COMMENT '注册时间',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所在城市',
  `occupation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职业',
  `emotional_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '情感状态',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `post_count` int(0) NULL DEFAULT 0 COMMENT '发帖数量',
  `follower_count` int(0) NULL DEFAULT 0 COMMENT '追随者数量',
  `following_count` int(0) NULL DEFAULT 0 COMMENT '关注数量',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码',
  `account_status` int(0) NULL DEFAULT NULL COMMENT '账号状态',
  `economy` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '经济',
  `member_number` int(0) NULL DEFAULT NULL COMMENT '会员编号',
  `subscriber` int(0) NULL DEFAULT 0 COMMENT '订阅用户',
  `user_role` int(0) NULL DEFAULT NULL COMMENT '用户角色',
  `invitation_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用的邀请码',
  `public_feeds` int(0) NOT NULL DEFAULT 1 COMMENT '0关1开',
  `public_topics` int(0) NOT NULL DEFAULT 1 COMMENT '0关1开',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username_idx`(`nickname`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1785315158108909569 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_info
-- ----------------------------

-- ----------------------------
-- Table structure for user_join_activity_relation
-- ----------------------------
DROP TABLE IF EXISTS `user_join_activity_relation`;
CREATE TABLE `user_join_activity_relation`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `activity_id` int(0) NOT NULL COMMENT '活动ID',
  `join_time` datetime(0) NOT NULL COMMENT '加入时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户与活动关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_join_activity_relation
-- ----------------------------

-- ----------------------------
-- Table structure for user_join_team_relation
-- ----------------------------
DROP TABLE IF EXISTS `user_join_team_relation`;
CREATE TABLE `user_join_team_relation`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `team_id` bigint(0) NOT NULL COMMENT '团队ID',
  `join_time` datetime(0) NOT NULL COMMENT '加入时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 98 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户与团队关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_join_team_relation
-- ----------------------------

-- ----------------------------
-- Table structure for user_message
-- ----------------------------
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `consumer_user_id` bigint(0) NOT NULL COMMENT '消费用户ID',
  `producer_user_id` bigint(0) NOT NULL COMMENT '生产用户ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `read_status` tinyint(1) NOT NULL COMMENT '已读状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_message
-- ----------------------------

-- ----------------------------
-- Table structure for user_photo
-- ----------------------------
DROP TABLE IF EXISTS `user_photo`;
CREATE TABLE `user_photo`  (
  `photo_id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `photo_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '相片名称',
  `upload_time` datetime(0) NOT NULL COMMENT '上传时间',
  `photo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '相片url',
  PRIMARY KEY (`photo_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户照片' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_photo
-- ----------------------------

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `role_id` int(0) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, '管理员');
INSERT INTO `user_roles` VALUES (2, '普通会员');
INSERT INTO `user_roles` VALUES (3, '订阅者');

-- ----------------------------
-- Table structure for user_star_records
-- ----------------------------
DROP TABLE IF EXISTS `user_star_records`;
CREATE TABLE `user_star_records`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `time` datetime(0) NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `amount` decimal(10, 2) NOT NULL,
  `balance` decimal(10, 2) NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `msg_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 640 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户星星记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_star_records
-- ----------------------------

-- ----------------------------
-- Table structure for website_config
-- ----------------------------
DROP TABLE IF EXISTS `website_config`;
CREATE TABLE `website_config`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自动递增的主键，用于唯一标识每个配置项',
  `config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项的名称，例如网站标题、网站地址等',
  `config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项的值，例如My Website、https://example.com等',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配置项的描述，用于说明配置项的用途',
  `created_at` datetime(0) NULL DEFAULT NULL COMMENT '配置项的创建时间',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '配置项的最后更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '网站配置信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of website_config
-- ----------------------------
INSERT INTO `website_config` VALUES (2, 'star.register', '200', '注册完成奖励', NULL, NULL);
INSERT INTO `website_config` VALUES (3, 'star.dailyRewards', '10', '每日登录奖励', NULL, NULL);
INSERT INTO `website_config` VALUES (4, 'star.publishArticle', '3', '发布主题花费', NULL, NULL);
INSERT INTO `website_config` VALUES (5, 'star.publishCode', '1', '发布Feed花费', NULL, NULL);
INSERT INTO `website_config` VALUES (6, 'star.createGroup', '5', '创建组花费', NULL, NULL);
INSERT INTO `website_config` VALUES (7, 'star.rewardArticle', '1', '打赏给文章', NULL, NULL);
INSERT INTO `website_config` VALUES (8, 'star.rewardCode', '1', '打赏给Feed块', NULL, NULL);
INSERT INTO `website_config` VALUES (9, 'upyun.bucketName', 'xxxx', '云存储桶名称', NULL, NULL);
INSERT INTO `website_config` VALUES (10, 'upyun.userName', 'xxxx', '云存储用户名', NULL, NULL);
INSERT INTO `website_config` VALUES (11, 'upyun.password', 'xxxx', '云存储密码', NULL, NULL);
INSERT INTO `website_config` VALUES (12, 'upyun.domain', 'https://i.xxxx.com', '云存储域名', NULL, NULL);
INSERT INTO `website_config` VALUES (13, 'upyun.path', '/mygroup/2024', '云存储路径', NULL, NULL);
INSERT INTO `website_config` VALUES (14, 'github.client_id', 'xxxxx', 'GitHub客户端ID', NULL, NULL);
INSERT INTO `website_config` VALUES (15, 'github.client_secret', 'xxxxxx', 'GitHub客户端密钥', NULL, NULL);
INSERT INTO `website_config` VALUES (16, 'github.redirect_uri', 'http://localhost:1024/oauth/github-callback', 'GitHub回调地址', NULL, NULL);
INSERT INTO `website_config` VALUES (17, 'upyun.interval', '360', '使用间隔时间分钟', NULL, NULL);
INSERT INTO `website_config` VALUES (18, 'website.invitation', '1', '邀请制(1开启,0关闭)', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
