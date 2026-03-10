package com.demo_system.service.impl;

import com.demo_system.entity.QuestionBankShow;
import com.demo_system.entity.QuestionBase;
import com.demo_system.entity.QuestionPackage;
import com.demo_system.entity.Response;
import com.demo_system.mapper.QuestionBankMapper;
import com.demo_system.service.QuestionBankService;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionBankMapper questionBankMapper;
    @Override
    public QuestionBankShow getDataToShow(Long userId) {
        QuestionBankShow show = new QuestionBankShow();
        // 1. 查询题库数量
        Long packageCount = questionBankMapper.countPackageByUser(userId);
        if (packageCount == null) packageCount = 0L;
        // 2. 查询题目总数
        Long questionCount = questionBankMapper.sumQuestionNumByUser(userId);
        if (questionCount == null) questionCount = 0L;
        // 3. 查询错题数量
        Long wrongCount = questionBankMapper.countWrongByUser(userId);
        if (wrongCount == null) wrongCount = 0L;
        // 4. 查询题库列表
        List<QuestionPackage> packageList = questionBankMapper.selectPackageListByUser(userId);
        // 赋值
        show.setPackageCount(packageCount);
        show.setQuestionCount(questionCount);
        show.setWrongCount(wrongCount);
        show.setPackageList(packageList);
        return show;
    }
    // 删除题库：先删题，再删题库（加事务）
    @Override
    @Transactional
    public void deletePackage(Long packageId) {
        //删除该题库下所有题目
        questionBankMapper.deleteQuestionsByPackageId(packageId);
        //删除题库本身
        questionBankMapper.deletePackage(packageId);
    }
    @Override
    public List<QuestionBase> getQuestionsByPackage(Long packageId) {
        return questionBankMapper.selectQuestionsByPackageId(packageId);
    }
    @Override
    public void updateQuestion(QuestionBase q) {
        questionBankMapper.updateQuestion(q);
    }
    @Override
    public void deleteQuestion(Long questionId) {
        questionBankMapper.deleteQuestion(questionId);
    }
    @Override
    public void updatePackageName(Long id, String newName) {
        questionBankMapper.updatePackageName(id, newName);
    }
    @Override
    public void insertQuestion(QuestionBase q) {
        questionBankMapper.insertQuestion(q);
        questionBankMapper.increasePackageQuesNum(q.getPackageId());
    }
    @Override
    public Long createPackage(Long userId) {
        QuestionPackage pkg = new QuestionPackage();
        pkg.setUserId(userId);
        pkg.setPackageName("未命名题库");
        pkg.setQuesNum(0L);
        pkg.setCreateTime(String.valueOf(LocalDateTime.now()));
        pkg.setStudyTime(null);
        questionBankMapper.insertPackage(pkg);
        return pkg.getId();
    }
    // 获取错题
    @Override
    public List<QuestionBase> getWrongQuestions(Long userId) {
        return questionBankMapper.selectWrongQuestions(userId);
    }

    // 上传csv文件解析为题库
    private String detectEncoding(MultipartFile file) {
        try {
            byte[] header = new byte[3];
            file.getInputStream().read(header);

            // UTF-8 BOM
            if (header[0] == (byte)0xEF && header[1] == (byte)0xBB && header[2] == (byte)0xBF) {
                return "UTF-8";
            }

            // 没 BOM，再试 GBK
            // 简单判断：GBK 中文一般第一个字节大于 0x7F
            for (byte b : header) {
                if (b < 0) return "GBK";   // 负数表示中文字节
            }

            // 默认 UTF-8
            return "UTF-8";

        } catch (Exception e) {
            return "UTF-8";
        }
    }

    @Override
    @Transactional
    public Response importCsv(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            return Response.fail("上传文件为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".csv")) {
            return Response.fail("只支持 CSV 文件");
        }
        // 检测编码
        String encoding = detectEncoding(file);
        String packageName = fileName.replace(".csv", "");
        try {
            // 1. 创建题库
            QuestionPackage pkg = new QuestionPackage();
            pkg.setUserId(userId);
            pkg.setPackageName(packageName);
            pkg.setQuesNum(0L);
            pkg.setCreateTime(String.valueOf(LocalDateTime.now()));
            questionBankMapper.insertPackage(pkg);
            Long packageId = pkg.getId();
            // 2. 检测出的编码来读文件
            CSVReader reader = new CSVReader(
                    new InputStreamReader(file.getInputStream(), encoding)
            );
            String[] arr;
            int count = 0;
            while ((arr = reader.readNext()) != null) {
                if (arr.length < 6) continue;
                String answer = arr[5].trim().toUpperCase();

                int correct = switch (answer) {
                    case "A" -> 1;
                    case "B" -> 2;
                    case "C" -> 3;
                    case "D" -> 4;
                    default -> 0;
                };
                if (correct == 0) continue;

                QuestionBase q = new QuestionBase();
                q.setUserId(userId);
                q.setPackageId(packageId);
                q.setQuesContent(arr[0]);
                q.setAOption(arr[1]);
                q.setBOption(arr[2]);
                q.setCOption(arr[3]);
                q.setDOption(arr[4]);
                q.setCorrectOption(correct);
                q.setStatus(0);

                questionBankMapper.insertQuestion(q);
                count++;
            }

            // 3. 增加题库数量
            for (int i = 0; i < count; i++) {
                questionBankMapper.increasePackageQuesNum(packageId);
            }

            return Response.ok("导入成功，共导入 " + count + " 道题", null);

        } catch (Exception e) {
            return Response.fail("解析失败：" + e.getMessage());
        }
    }
    @Override
    public void exportCsv(Long packageId, HttpServletResponse response) {
        try {
            // 1. 查询题库信息
            List<QuestionBase> questions = questionBankMapper.selectQuestionsByPackageId(packageId);
            QuestionPackage pkg = questionBankMapper.selectPackageById(packageId);
            // 2. 设置文件名（中文要处理编码）
            String fileName = pkg.getPackageName() + ".csv";
            fileName = new String(fileName.getBytes("GBK"), "ISO-8859-1");
            response.setContentType("text/csv;charset=GBK");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            // 3. 写入 CSV
            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "GBK");
            CSVWriter csvWriter = new CSVWriter(writer);

            // 写数据
            for (QuestionBase q : questions) {
                String correct = switch (q.getCorrectOption()) {
                    case 1 -> "A";
                    case 2 -> "B";
                    case 3 -> "C";
                    case 4 -> "D";
                    default -> "";
                };

                csvWriter.writeNext(new String[]{
                        q.getQuesContent(),
                        q.getAOption(),
                        q.getBOption(),
                        q.getCOption(),
                        q.getDOption(),
                        correct
                });
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (Exception e) {
            throw new RuntimeException("导出题库失败: " + e.getMessage());
        }
    }


}
