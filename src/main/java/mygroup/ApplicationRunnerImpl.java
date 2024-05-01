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

package mygroup;

import cn.hutool.core.text.csv.CsvRow;
import com.alibaba.fastjson2.JSON;
import com.upyun.RestManager;
import com.upyun.UpException;
import lombok.extern.slf4j.Slf4j;
import mygroup.algorithm.tools.CSVReader;
import mygroup.algorithm.tools.DataReader;
import mygroup.algorithm.tools.NaiveBayesClassifierTool;
import mygroup.common.constant.TextType;
import mygroup.config.UpyunConfig;
import mygroup.service.IWebsiteConfigService;
import mygroup.util.EdBeanUtils;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Eddie·ZeBingBao
 * @date: 2024/3/13 18:48
 */
@Slf4j
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Autowired
    private EdBeanUtils edBeanUtils;
    @Autowired
    private UpyunConfig config;
    @Autowired
    private IWebsiteConfigService iWebsiteConfigService;
    @Autowired
    private NaiveBayesClassifierTool tool;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.info("加载网站配置信息缓存");
        iWebsiteConfigService.setCache();
        log.info("加载网站配置信息缓存完成");

        log.info("初始化又拍云");
        edBeanUtils.setRestManager(this.restManager());
        log.info("初始化又拍云完成");

        log.info("训练贝叶斯分类器");
        //this.trainBayes();
        log.info("训练贝叶斯分类器完成");

    }

    public void trainBayes() throws Exception {
        // 查看是否已经训练过
        if (redisTemplate.hasKey("trainBayes-classCounts")) {

            Map<String, Double> classProbabilities = new HashMap<>();
            Map map = JSON.parseObject((String) redisTemplate.opsForValue().get("trainBayes-classProbabilities"), Map.class);
            for (Object key : map.keySet()) {
                classProbabilities.put((String) key, Double.parseDouble(map.get(key).toString()));
            }
            tool.classProbabilities = classProbabilities;

            Map<String, Integer> classCounts = new HashMap<>();
            Map map1 = JSON.parseObject((String) redisTemplate.opsForValue().get("trainBayes-classCounts"), Map.class);
            for (Object key : map1.keySet()) {
                classCounts.put((String) key, Integer.parseInt(map1.get(key).toString()));
            }
            tool.classCounts = classCounts;

            Map<String, Map<String, Integer>> featureCounts = new HashMap<>();
            Map map2 = JSON.parseObject((String) redisTemplate.opsForValue().get("trainBayes-featureCounts"), Map.class);
            for (Object key : map2.keySet()) {
                Map<String, Integer> value = new HashMap<>();
                Map map3 = (Map) map2.get(key);
                for (Object key1 : map3.keySet()) {
                    value.put((String) key1, Integer.parseInt(map3.get(key1).toString()));
                }
                featureCounts.put((String) key, value);
            }
            tool.featureCounts = featureCounts;

            log.info("已经训练过，无需再次训练");
            return;
        }

        // 加载停用词
        tool.loadStopWords("stopwords2.txt");

        // 短信 0正常1垃圾
        List<String[]> readData = DataReader.readData("train_data/data.txt");
        for (String[] readDatum : readData) {
            tool.train(readDatum[1], Integer.parseInt(readDatum[0]) == 1 ? TextType.SPAM : TextType.HAM);
        }

        // 垃圾邮件训练0正常1垃圾
        List<CsvRow> rows = CSVReader.readCSV("train_data/df_train.csv");
        for (CsvRow row : rows) {
            String text = row.get(1);
            int label = Integer.parseInt(row.get(2));
            tool.train(text, label == 1 ? TextType.SPAM : TextType.HAM);
        }

        // 违禁、敏感、色情词训练
        List<String> spamTextList = DataReader.readDataWords("train_data/Illegal_sensitive_words.txt");
        spamTextList.addAll(DataReader.readDataWords("train_data/political_sensitive_words.txt"));
        spamTextList.addAll(DataReader.readDataWords("train_data/pornographic_sensitive_words.txt"));
        for (String words : spamTextList) {
            tool.train(words, TextType.SPAM);
        }

        /*// 垃圾数据训练
        List<String> spamTextList = DataReader.readDataWords("train_data/spam_data.txt");
        for (String words : spamTextList) {
            tool.train(words, TextType.SPAM);
        }

        // 正常数据训练
        List<String> hamTextList = DataReader.readDataWords("train_data/ham_data.txt");
        for (String words : hamTextList) {
            tool.train(words, TextType.HAM);
        }*/

        // 计算概率
        tool.calculateClassProbabilities();

        // 保存训练结果到redis
        Map<String, Integer> classCounts = tool.classCounts;
        Map<String, Double> classProbabilities = tool.classProbabilities;
        Map<String, Map<String, Integer>> featureCounts = tool.featureCounts;

        redisTemplate.opsForValue().set("trainBayes-classCounts", JSON.toJSONString(classCounts));
        redisTemplate.opsForValue().set("trainBayes-classProbabilities", JSON.toJSONString(classProbabilities));
        redisTemplate.opsForValue().set("trainBayes-featureCounts", JSON.toJSONString(featureCounts));
        //log.info("训练完成，训练数据 {} 行", readData.size() + spamTextList.size() + hamTextList.size());
        log.info("训练完成，训练数据 {} 行", readData.size() + rows.size());
    }

    public RestManager restManager() throws UpException, IOException {
        String path = config.getPath();
        String bucketName = config.getBucketName();
        String userName = config.getUserName();
        String password = config.getPassword();

        RestManager manager = new RestManager(bucketName, userName, password);
        manager.setApiDomain(RestManager.ED_AUTO);

        // 创建目录，自动创建父级目录
        Response result = manager.mkDir(path);
        log.info("创建 {} {}", path, result.isSuccessful());
        return manager;
    }

}
