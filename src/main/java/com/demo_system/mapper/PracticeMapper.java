package com.demo_system.mapper;

import com.demo_system.entity.LearnRecord;
import com.demo_system.entity.QuestionBase;
import com.demo_system.entity.QuestionPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PracticeMapper {

    /**
     * 读取当前用户的所有题库列表（刷题用）
     */
    List<QuestionPackage> selectPracticePackages(@Param("userId") Long userId);

    List<LearnRecord> getRecord(@Param("userId") long userId);

    //根据id查询题目
    QuestionBase selectQuestionById(@Param("questionId") Long questionId);

    //插入学习记录
    void insertRecord(LearnRecord record);

    //更新题目状态
    void updateQuestionStatus(@Param("questionId") Long questionId,
                              @Param("status") Integer status);
    //更新题库信息
    void updatePackageStudyTime(@Param("packageId") Long packageId,
                                @Param("deltaTime") Integer deltaTime);
}
