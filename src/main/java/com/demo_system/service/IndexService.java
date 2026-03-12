package com.demo_system.service;

import com.demo_system.entity.StudyData;


public interface IndexService {
    public StudyData getDataToShow(Long userId,String date);
}
