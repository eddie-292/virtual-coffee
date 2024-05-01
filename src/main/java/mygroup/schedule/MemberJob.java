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

package mygroup.schedule;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import mygroup.entity.UserInfo;
import mygroup.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Eddie·ZeBingBao
 * @date: 2024/4/14 11:59
 */
@Slf4j
@Component
public class MemberJob {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 每小时
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void memberSchedule() {

        //查询账号状态为0的用户并且注册时间在1天前的用户
        List<UserInfo> userInfos = userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getAccountStatus, 0)
                .lt(UserInfo::getRegisterTime, System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        );

        //删除用户
        if (CollectionUtil.isNotEmpty(userInfos)) {
            userInfoMapper.deleteBatchIds(userInfos.stream().map(UserInfo::getUserId).collect(Collectors.toList()));
        }

    }


}
