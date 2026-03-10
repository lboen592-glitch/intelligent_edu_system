package com.demo_system.mapper;

import com.demo_system.entity.QuestionBase;
import com.demo_system.entity.QuestionPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionBankMapper {
    /**
     * 查询当前用户的题库数量
     */
    Long countPackageByUser(@Param("userId") Long userId);
    /**
     * 查询当前用户所有题库的题目数量总和
     */
    Long sumQuestionNumByUser(@Param("userId") Long userId);


    /**
     * 查询当前用户错题数量（question_base.status = 2）
     */
    Long countWrongByUser(@Param("userId") Long userId);
    /**
     * 查询当前用户所有题库列表
     */
    List<QuestionPackage> selectPackageListByUser(@Param("userId") Long userId);
    //删除某个题库下的所有题目
    void deleteQuestionsByPackageId(@Param("packageId") Long packageId);
    //删除题库
    void deletePackage(@Param("id") Long id);
    // 根据题库ID查询该题库下的所有题目
    List<QuestionBase> selectQuestionsByPackageId(@Param("packageId") Long packageId);
    // 更新题目
    void updateQuestion(QuestionBase q);
    // 删除题目
    void deleteQuestion(Long questionId);
    // 更新题库名称
    void updatePackageName(@Param("id") Long id, @Param("name") String name);
    // 新增题目
    void insertQuestion(QuestionBase q);
    // 修改题库题目数量
    void increasePackageQuesNum(Long packageId);
    // 新增题库
    void insertPackage(QuestionPackage pkg);
    // 查询当前用户所有错题（status != correct_option AND status != 0）
    List<QuestionBase> selectWrongQuestions(@Param("userId") Long userId);
    //根据id查询某个题库
    QuestionPackage selectPackageById(@Param("id") Long id);
}
