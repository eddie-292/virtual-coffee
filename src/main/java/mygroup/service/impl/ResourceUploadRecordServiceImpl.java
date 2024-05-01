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

package mygroup.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.upyun.RestManager;
import com.upyun.UpException;
import com.upyun.UpYunUtils;
import lombok.extern.slf4j.Slf4j;
import mygroup.common.exception.BisException;
import mygroup.dto.common.ConditionQuery;
import mygroup.dto.common.PageQuery;
import mygroup.entity.ResourceUploadRecord;
import mygroup.mapper.ResourceUploadRecordMapper;
import mygroup.service.IResourceUploadRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mygroup.util.EdBeanUtils;
import mygroup.util.TokenUtil;
import mygroup.util.WebsiteConfigUtil;
import okhttp3.Response;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:资源上传记录 服务接口实现类
 * @author Eddie
 * @date 2024-02-07
 */
@Slf4j
@Service
public class ResourceUploadRecordServiceImpl extends ServiceImpl<ResourceUploadRecordMapper, ResourceUploadRecord> implements IResourceUploadRecordService {

	@Autowired
	private ResourceUploadRecordMapper resourceUploadRecordMapper;
	@Autowired
	private EdBeanUtils edBeanUtils;
	@Autowired
	private HttpServletRequest request;

	@Override
	public IPage<ResourceUploadRecord> queryByPage(PageQuery pageQuery) {
		return null;
	}

	@Override
	public List<ResourceUploadRecord> queryByCondition(ConditionQuery conditionQuery) {
		return null;
	}

	@Override
	public ResourceUploadRecord upload(MultipartFile reqFile) throws IOException, UpException {
		if (reqFile.isEmpty()) {
			throw new BisException("文件不能为空");
		}

		Long userId = TokenUtil.userId(request);

		Tika tika = new Tika();
		String mimeType = tika.detect(reqFile.getInputStream());
		if (mimeType.startsWith("image/")) {
			//判斷如果是图片大小不能超过10M
			if (reqFile.getSize() > 10 * 1024 * 1024) {
				throw new BisException("图片大小不能超过10M");
			}
		} else if (mimeType.startsWith("video/")) {
			//判斷如果是视频大小不能超过100M
			if (reqFile.getSize() > 100 * 1024 * 1024) {
				throw new BisException("视频大小不能超过100M");
			}
		} else {
			throw new BisException("文件格式不支持");
		}

		String md5 = UpYunUtils.md5(reqFile.getBytes());
		Map<String, String> params = new HashMap<String, String>();
		//设置待上传文件的 Content-MD5 值
		//如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
		params.put(RestManager.PARAMS.CONTENT_MD5.getValue(), md5);

		String path = WebsiteConfigUtil.getWebsiteConfig("upyun.path").toString();
		String domain = WebsiteConfigUtil.getWebsiteConfig("upyun.domain").toString();

		String extension = FilenameUtils.getExtension(reqFile.getOriginalFilename());
		String idName = IdUtil.fastSimpleUUID();
		String originalFilename = idName + "." + extension.toLowerCase();
		String uploadPath = path + "/" + userId + "/" + originalFilename;

		Response response = edBeanUtils.getRestManager().writeFile(uploadPath, reqFile.getInputStream(), params);
		String url = domain + uploadPath;
		log.info("图片上传URL {}", url);

		if (response.isSuccessful()) {
			ResourceUploadRecord resourceUploadRecord = new ResourceUploadRecord();
			resourceUploadRecord.setId(IdUtil.getSnowflakeNextId());
			resourceUploadRecord.setType(mimeType);
			resourceUploadRecord.setUrl(url);
			resourceUploadRecord.setUploadTime(new Date());
			resourceUploadRecord.setUploaderUserId(userId);
			this.baseMapper.insert(resourceUploadRecord);
			return resourceUploadRecord;
		} else {
			log.error("图片上传失败 {} MD5 {} ", response.message(), md5);
			throw new BisException("上传失败");
		}
	}

}
