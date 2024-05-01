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

package mygroup.algorithm.tools;

import com.huaban.analysis.jieba.JiebaSegmenter;
import mygroup.common.constant.TextType;
import mygroup.util.RemoveHtmlTags;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Eddie.BaoZeBing
 * @date 2024/3/8 18:30
 */
@Component
public class NaiveBayesClassifierTool {

    public Map<String, Integer> classCounts; // 存储每个类别的样本数量 1
    public Map<String, Double> classProbabilities; // 存储每个类别的先验概率 1
    public Map<String, Map<String, Integer>> featureCounts; // 存储每个类别中每个特征的样本数量 1
    private JiebaSegmenter segmenter; // 分词器
    private Set<String> stopWords = new HashSet<>(); // 停用词

    public Set<String> loadStopWords(String name) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(name)))) {
            String line;
            Set<String> stopWords = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim());
            }
            return stopWords;
        }
    }

    // 分词，处理文本，包括分词和去除停用词
    public List<String> processText(String text) {
        text = RemoveHtmlTags.removeHtmlTags(text); // 去除html标签
        List<String> tokens = segmenter.sentenceProcess(text);
        List<String> processedTokens = new ArrayList<>();
        for (String token : tokens) {
            if (!stopWords.contains(token)
                    && isChineseOrEnglish(token)
                    && !isNumeric(token)
                    && !isEnglishAndLengthIn2(token)) {
                processedTokens.add(token);
            }
        }
        return processedTokens;
    }

    //判断是否是纯英文并且长度在2以内
    public static boolean isEnglishAndLengthIn2(String str) {
        // 判断字符串是否是纯英文
        for (char c : str.toCharArray()) {
            if (!Character.isUpperCase(c) && !Character.isLowerCase(c)) {
                return false;
            }
        }
        // 判断字符串长度是否在2以内
        return str.length() <= 2;
    }

    //判断是否是纯数字
    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    //判断是否是中文或英文
    public static boolean isChineseOrEnglish(String str) {
        String regex = "[\\u4E00-\\u9FA5a-zA-Z0-9]+";
        return Pattern.matches(regex, str);
    }

    public NaiveBayesClassifierTool() throws IOException {
        classCounts = new HashMap<>();
        featureCounts = new HashMap<>();
        classProbabilities = new HashMap<>();
        segmenter = new JiebaSegmenter();
    }

    // 训练模型
    public void train(String text, String label) {
        List<String> features = processText(text);

        // 计算特征频率
        Map<String, Integer> featureCountMap = featureCounts.getOrDefault(label, new HashMap<>());
        for (String feature : features) {
            featureCountMap.put(feature, featureCountMap.getOrDefault(feature, 0) + 1);
        }
        featureCounts.put(label, featureCountMap);

        // 更新类别样本数量
        classCounts.put(label, classCounts.getOrDefault(label, 0) + 1);
    }

    public void train(InputStream inputStream, String label) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> features = processText(text.toString());
        //System.out.println(JSON.toJSONString(features));

        // 计算特征频率
        Map<String, Integer> featureCountMap = featureCounts.getOrDefault(label, new HashMap<>());
        for (String feature : features) {
            featureCountMap.put(feature, featureCountMap.getOrDefault(feature, 0) + 1);
        }
        featureCounts.put(label, featureCountMap);

        // 更新类别样本数量
        classCounts.put(label, classCounts.getOrDefault(label, 0) + 1);
    }

    // 计算类别概率
    public void calculateClassProbabilities() {
        int totalCount = classCounts.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : classCounts.entrySet()) {
            String label = entry.getKey();
            int classCount = entry.getValue();
            classProbabilities.put(label, (double) classCount / totalCount);
        }

        long classCountsSize = 0;
        for (Map.Entry<String, Integer> entry : classCounts.entrySet()) {
            classCountsSize += entry.getKey().length() * 2 + 4 + 4;
        }

        long classProbabilitiesSize = 0;
        for (Map.Entry<String, Double> entry : classProbabilities.entrySet()) {
            classProbabilitiesSize += entry.getKey().length() * 2 + 8 + 8;
        }

        long featureCountsSize = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : featureCounts.entrySet()) {
            featureCountsSize += entry.getKey().length() * 2 + 4 + 4;
            Map<String, Integer> map = entry.getValue();
            for (Map.Entry<String, Integer> entry1 : map.entrySet()) {
                featureCountsSize += entry1.getKey().length() * 2 + 4 + 4;
            }
        }

        //转换为MB
        System.out.println("classCountsSize: " + classCountsSize / 1024 / 1024 + "MB");
        System.out.println("classProbabilitiesSize: " + classProbabilitiesSize / 1024 / 1024 + "MB");
        System.out.println("featureCountsSize: " + featureCountsSize / 1024 / 1024 + "MB");
    }

    // 预测
    public String predict(String text) {
        List<String> features = processText(text);

        // 计算每个类别的概率
        Map<String, Double> scores = new HashMap<>();
        for (String label : classCounts.keySet()) {
            double score = Math.log(classProbabilities.get(label));
            Map<String, Integer> featureCountMap = featureCounts.get(label);
            int totalFeatureCount = featureCountMap.values().stream().mapToInt(Integer::intValue).sum();
            Set<String> featureSet = featureCountMap.keySet();

            for (String feature : features) {
                int featureCount = featureCountMap.getOrDefault(feature, 0) + 1; // 拉普拉斯平滑
                double probability = (double) featureCount / (totalFeatureCount + featureSet.size());
                score += Math.log(probability);
            }

            scores.put(label, score);
        }

        // 返回概率最高的类别
        String predictedLabel = null;
        double maxScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            String label = entry.getKey();
            double score = entry.getValue();
            if (score > maxScore) {
                maxScore = score;
                predictedLabel = label;
            }
        }

        System.out.println(predictedLabel + " predicted=" + maxScore);
        return predictedLabel;
    }

    public boolean spam(String text) {
        //开启训练后再使用这行代码
        //return this.predict(text).equalsIgnoreCase(TextType.SPAM);
        return false;
    }

}



