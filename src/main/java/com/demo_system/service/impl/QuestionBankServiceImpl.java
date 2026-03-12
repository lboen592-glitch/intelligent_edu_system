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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
    @Transactional //@Transactional：声明式事务注解，要么全部成功，要么全部回滚）
    public Response importCsv(MultipartFile file, Long userId) {
        if (file.isEmpty()) return Response.fail("上传文件为空");
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".csv")) return Response.fail("只支持 CSV 文件");
        // 检测编码
        String encoding = detectEncoding(file);
        //提取题库名称
        String packageName = fileName.replace(".csv", "");
        try {
            // 创建题库knowledge_base
            QuestionPackage pkg = new QuestionPackage();
            pkg.setUserId(userId);
            pkg.setPackageName(packageName);
            pkg.setQuesNum(0L);
            pkg.setCreateTime(String.valueOf(LocalDateTime.now()));
            questionBankMapper.insertPackage(pkg);
            Long packageId = pkg.getId();
            // 检测出的编码来读文件
            // 创建CSVReader：按检测到的编码读取文件流
            // InputStreamReader将文件二进制流（前端Blob）转为字符流，指定编码encoding
            CSVReader reader = new CSVReader(
                    new InputStreamReader(file.getInputStream(), encoding)
            );
            // 存储CSV每一行解析后的列数据
            String[] arr;
            // 统计成功导入的题目数量
            int count = 0;
            // 循环读取CSV每一行（readNext()返回null表示读取完毕）
            while ((arr = reader.readNext()) != null)
            {
                if (arr.length < 6) continue;
                // 处理答案列：去空格 + 转大写（统一格式，如a→A，b→B）
                String answer = arr[5].trim().toUpperCase();
                // 答案转数字（匹配数据库存储的correct_option字段：1=A，2=B，3=C，4=D）
                int correct = switch (answer)
                {
                    case "A" -> 1;
                    case "B" -> 2;
                    case "C" -> 3;
                    case "D" -> 4;
                    default -> 0;
                };
                if (correct == 0) continue;//无效答案
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
            // 增加题库数量
            for (int i = 0; i < count; i++)
                questionBankMapper.increasePackageQuesNum(packageId);
            return Response.ok("导入成功，共导入 " + count + " 道题", null);
        } catch (Exception e) {
            return Response.fail("解析失败：" + e.getMessage());
        }
    }
    @Override
    public void exportCsv(Long packageId, HttpServletResponse response)
    //HttpServletResponse：用于向前端输出CSV文件流（替代return返回值）
    {
        try {
            // 查询题库信息
            List<QuestionBase> questions = questionBankMapper.selectQuestionsByPackageId(packageId);
            QuestionPackage pkg = questionBankMapper.selectPackageById(packageId);
            // 设置文件名
            String fileName = pkg.getPackageName() + ".csv";
            // HTTP响应头不支持直接传输中文，需转成ISO-8859-1编码浏览器可以正常识别
            fileName = new String(fileName.getBytes("GBK"), "ISO-8859-1");
            // 设置响应内容类型：告诉浏览器返回的是CSV文件，编码为GBK
            response.setContentType("text/csv;charset=GBK");
            // ontent-Disposition：核心头，指定“附件下载”+文件名
            // attachment：浏览器识别为“文件下载”而非页面展示；filename：下载的文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            // 写入 CSV。创建字符输出流：绑定响应的输出流，response.getOutputStream()是二进制流，以便
            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "GBK");
            // 创建CSVWriter简化CSV格式写入
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
                // 写入一行CSV数据：按“题目、选项A、选项B、选项C、选项D、答案”顺序排列
                csvWriter.writeNext(new String[]{
                        q.getQuesContent(),
                        q.getAOption(),
                        q.getBOption(),
                        q.getCOption(),
                        q.getDOption(),
                        correct
                });
            }
            // 刷新缓冲区确保所有数据写入响应流
            csvWriter.flush();
            // 关闭流释放资源
            csvWriter.close();
        } catch (Exception e) {
            throw new RuntimeException("导出题库失败: " + e.getMessage());
        }
    }


}
