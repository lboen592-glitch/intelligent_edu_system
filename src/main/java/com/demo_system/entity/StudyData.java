package com.demo_system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyData {
    private long userId;
    private String date;
    private Integer totalStudyTime;
    private Integer totalQuesNum;
    private Integer wrongQuesNum;
}
