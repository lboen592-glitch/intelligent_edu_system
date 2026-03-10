package com.demo_system.service;

import com.demo_system.entity.QuestionBankShow;
import com.demo_system.entity.QuestionBase;
import com.demo_system.entity.Response;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionBankService {
    QuestionBankShow getDataToShow(Long userId);
    void deletePackage(Long packageId);
    // 查询某个题库下的所有题目
    List<QuestionBase> getQuestionsByPackage(Long packageId);
    // 修改题目
    void updateQuestion(QuestionBase question);
    // 删除题目
    void deleteQuestion(Long questionId);
    // 更新题库名称
    void updatePackageName(Long id, String newName);
    // 添加题目
    void insertQuestion(QuestionBase q);
    // 添加题库
    Long createPackage(Long userId);
    // 获取错题
    List<QuestionBase> getWrongQuestions(Long userId);
    //上传csv文件解析为题库
    Response importCsv(MultipartFile file, Long userId);
    //导出csv文件
    void exportCsv(Long packageId, HttpServletResponse response);

}
