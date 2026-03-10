package com.demo_system.mapper;

import com.demo_system.entity.StudyData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DataMapper {
    StudyData selectByUserAndDate(
            @Param("userId") long userId,
            @Param("date") String date);

    void insertStudyData(StudyData studyData);

    void updateStudyData(StudyData studyData);
}
