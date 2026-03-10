package com.demo_system.service.impl;

import com.demo_system.entity.LearnRecord;
import com.demo_system.entity.QuestionBase;
import com.demo_system.entity.QuestionPackage;
import com.demo_system.mapper.DataMapper;
import com.demo_system.mapper.PracticeMapper;
import com.demo_system.mapper.QuestionBankMapper;
import com.demo_system.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PracticeServiceImpl implements PracticeService {

    private final PracticeMapper practiceMapper;
    private final QuestionBankMapper questionBankMapper;
    private final DataMapper dataMapper;

    @Override
    public List<QuestionPackage> getPracticePackages(Long userId) {
        return questionBankMapper.selectPackageListByUser(userId);
    }

    @Override
    public List<QuestionBase> getQuestions(Long packageId, String order, String count) {

        // 获取全部题目
        List<QuestionBase> list = questionBankMapper.selectQuestionsByPackageId(packageId);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        // 随机：打乱顺序
        if ("random".equalsIgnoreCase(order)) {
            Collections.shuffle(list);
        }
        // 限制数量
        if (!"all".equals(count)) {
            try {
                int limit = Integer.parseInt(count);
                if (list.size() > limit) {
                    list = list.subList(0, limit);
                }
            } catch (Exception ignored) {}
        }
        return list;
    }
    @Override
    public List<LearnRecord> getRecord(long userId){
        // 1. 先从 learn_record 表查出原始记录
        List<LearnRecord> list = practiceMapper.getRecord(userId);
        if (list == null || list.isEmpty()) {
            return list;
        }
        // 2. 先把所有用到的题库 id 收集起来，避免循环里反复查数据库
        Set<Long> pkgIdSet = new HashSet<>();
        for (LearnRecord lr : list) {
            if (lr.getBelongPackageId() != null) {
                pkgIdSet.add(lr.getBelongPackageId());
            }
        }
        // 3. 批量查询题库名（因为你的 QuestionBankMapper 只有按 id 查一个，
        //    我们就简单做一个缓存 map，避免对同一个 id 多次查）
        Map<Long, String> pkgNameMap = new HashMap<>();
        for (Long pkgId : pkgIdSet) {
            QuestionPackage pkg = questionBankMapper.selectPackageById(pkgId);
            pkgNameMap.put(pkgId, pkg == null ? "" : pkg.getPackageName());
        }
        // 4. 遍历记录列表，填充 packageName 和 quesNum
        for (LearnRecord lr : list) {
            // 题库名称
            Long pkgId = lr.getBelongPackageId();
            if (pkgId != null) {
                lr.setPackageName(pkgNameMap.getOrDefault(pkgId, ""));
            } else {
                lr.setPackageName("");
            }
            // 题目数量：解析 questionIdSeq 字符串
            String seq = lr.getQuestionIdSeq();
            if (seq == null || seq.isBlank()) {
                lr.setQuesNum(0);
            } else {
                String[] parts = seq.split("\\*");
                int count = 0;
                for (String p : parts) {
                    if (p != null && !p.isBlank()) {
                        count++;
                    }
                }
                lr.setQuesNum(count);
            }
        }
        return list;
    }
    @Override
    public List<QuestionBase> getRecordDetails(String questionIdSeq) {
        List<QuestionBase> result = new ArrayList<>();
        // 判空
        if (questionIdSeq == null || questionIdSeq.trim().isEmpty()) {
            return result;
        }
        // 去掉最后多余的 *（如果有的话）
        String seq = questionIdSeq.trim();
        if (seq.endsWith("*")) {
            seq = seq.substring(0, seq.length() - 1);
        }
        // 按 * 分割
        String[] parts = seq.split("\\*");
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) {
                continue;
            }
            try {
                Long qid = Long.valueOf(part.trim());
                QuestionBase qb = practiceMapper.selectQuestionById(qid);
                if (qb != null) {
                    // 保持和 questionIdSeq 一样的顺序
                    result.add(qb);
                }
            } catch (NumberFormatException e) {
                System.out.println("非法的题目ID: {}"+ part + e);
            }
        }
        return result;
    }
    @Override
    public void submitRecord(Long userId,LearnRecord record) {
        //根据Id和record去插入数据库learn_record表
        if (record == null) {
            throw new IllegalArgumentException("record 不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        // 补充用户 id
        record.setUserId(userId);
        // 设置创建时间
        record.setCreateTime(String.valueOf(LocalDateTime.now()));
        String quesSeq = record.getQuestionIdSeq();
        String ansSeq  = record.getAnswerSeq();
        if (quesSeq == null || quesSeq.isBlank()
                || ansSeq == null || ansSeq.isBlank()) {
            // 如果前端没传题目序列 / 答案序列，就直接插一条 0 正确率记录
            record.setAccuracy(0);
            practiceMapper.insertRecord(record);
            return;
        }
        /*解析record中的questionIdSeq和answerSeq，拿着questionIdSeq去question_base表查询题目
        拿到题目的正确答案后，与answerSeq进行比较，得到每个题目的status字段，从而更新question_base表
        */
        quesSeq = quesSeq.trim();
        ansSeq  = ansSeq.trim();
        if (quesSeq.endsWith("*")) {
            quesSeq = quesSeq.substring(0, quesSeq.length() - 1);
        }
        if (ansSeq.endsWith("*")) {
            ansSeq = ansSeq.substring(0, ansSeq.length() - 1);
        }
        String[] quesArr = quesSeq.split("\\*");
        String[] ansArr  = ansSeq.split("\\*");
        // 防御：两边长度不一致时，取较小值
        int n = Math.min(quesArr.length, ansArr.length);
        List<Long> questionIds = new ArrayList<>();
        List<Integer> userAnswers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String qStr = quesArr[i];
            String aStr = ansArr[i];
            if (qStr == null || qStr.isBlank()) {
                continue;
            }
            try {
                Long qid = Long.valueOf(qStr.trim());
                Integer ua = null;
                if (aStr != null && !aStr.isBlank()) {
                    ua = Integer.valueOf(aStr.trim());
                }
                questionIds.add(qid);
                userAnswers.add(ua);
            } catch (NumberFormatException ignore) {
                // 出现非法 ID 或答案就跳过该题
            }
        }
        int totalQues = questionIds.size();
        int correct = 0;
        int wrong = 0;
        // ===== 2. 遍历每道题，对比正确答案，更新 question_base.status =====
        for (int i = 0; i < totalQues; i++) {
            Long qid = questionIds.get(i);
            Integer ua = userAnswers.get(i);  // 用户选择的选项：0=未作答，1=A，2=B，3=C，4=D
            QuestionBase qb = practiceMapper.selectQuestionById(qid);
            if (qb == null || qb.getCorrectOption() == null) {
                // 查不到题目或正确答案，直接跳过
                continue;
            }
            // ===== 2.1 更新题目最后一次作答选项到 status =====
            // 这里直接把用户最后一次选项写入 status（包括 0 表示没选）
            // 根据你的表设计，status 就是 0/1/2/3/4
            practiceMapper.updateQuestionStatus(qid, ua == null ? 0 : ua);
            // ===== 2.2 统计正确 / 错误数（用于计算正确率、study_data 错题数）=====
            // 未作答（null 或 0）不计入正确/错误
            if (ua == null || ua == 0) {
                continue;
            }
            if (ua.equals(qb.getCorrectOption())) {
                correct++;
            } else {
                wrong++;
            }
        }
        // ===== 3. 计算正确率，并插入 learn_record =====
        int accuracy = 0;
        if (totalQues > 0) {
            accuracy = (int) Math.round(correct * 100.0 / totalQues);
        }
        record.setAccuracy(accuracy);
        practiceMapper.insertRecord(record);
        // ===== 4. 更新 question_package.study_time =====
        // study_time += 本次记录 total_time
        if (record.getBelongPackageId() != null && record.getTotalTime() != null) {
            if (record.getTotalTime() > 0) {
                practiceMapper.updatePackageStudyTime(
                        record.getBelongPackageId(),
                        record.getTotalTime()
                );
            }
        }
        /*
         *根据record中的belong_package_id去question_package表更新相应的study_time
         *
         * */

        /*
        *构造study_data数据中的date,
        * total_study_time += record.total_time，
        * total_ques_num 更新
        * wrong_ques_num 更新
        * 获取今日日期后更新或插入study_data表
        * */
        // ===== 5. 更新 / 插入 study_data =====
        // date = 今天（yyyy-MM-dd），
        // total_study_time += record.total_time，
        // total_ques_num   += 本次题目数 totalQues
        // wrong_ques_num   += wrong
        String today = java.time.LocalDate.now().toString();
        int deltaStudyTime = record.getTotalTime() == null ? 0 : record.getTotalTime();
        int deltaTotalQues = totalQues;
        int deltaWrongQues = wrong;
        com.demo_system.entity.StudyData exist =
                dataMapper.selectByUserAndDate(userId, today);
        com.demo_system.entity.StudyData delta =
                new com.demo_system.entity.StudyData(
                        userId,
                        today,
                        deltaStudyTime,
                        deltaTotalQues,
                        deltaWrongQues
                );
        if (exist == null) {
            // 今天还没有记录，插入一条新的
            dataMapper.insertStudyData(delta);
        } else {
            // 在原有基础上累加
            dataMapper.updateStudyData(delta);
        }

    }
}
