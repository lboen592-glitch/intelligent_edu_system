package com.demo_system.entity;

import lombok.Data;

/**
 * 表 learn_record
 */
@Data
public class LearnRecord {
    private Long id;
    private Long userId;
    private Long belongPackageId;
    private String questionIdSeq;
    private String answerSeq;
    //正确率（0-100 的整数百分比
    private Integer accuracy;
    // 用时，秒
    private Integer totalTime;
    private String createTime;

    // 下面两个字段不在表里，只是返回给前端展示用
    //
    private String packageName;
    //本次练习题目数量
    private Integer quesNum;

}
