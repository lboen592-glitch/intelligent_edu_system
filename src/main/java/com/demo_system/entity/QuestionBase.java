package com.demo_system.entity;

import lombok.Data;

/**
 * 题目实体，对应 question_base 表
 */
@Data
public class QuestionBase {
    private Long id;
    private Long userId;
    private Long packageId;
    private String quesContent;
    private String aOption;
    private String bOption;
    private String cOption;
    private String dOption;
    /** 1=A, 2=B, 3=C, 4=D */
    private Integer correctOption;
    /** 最近一次做题状态（null 未做，1A2B3C4D） */
    private Integer status;
}
