package com.demo_system.entity;

import lombok.Data;

/**
 * 题库（题包）实体，对应 question_package 表
 */
@Data
public class QuestionPackage {
    private Long id;
    private Long userId;
    private String packageName;
    private Long quesNum;
    private Long studyTime;
    private String createTime;
}
