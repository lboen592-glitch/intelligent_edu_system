package com.demo_system.entity;

import lombok.Data;

import java.util.List;

@Data
public class QuestionBankShow {
    private long packageCount;
    private long questionCount;
    private long wrongCount;
    // 题库列表
    private List<QuestionPackage> packageList;
}
