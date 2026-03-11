package com.demo_system.controller;

import com.demo_system.entity.LearnRecord;
import com.demo_system.entity.QuestionBase;
import com.demo_system.entity.QuestionPackage;
import com.demo_system.entity.Response;
import com.demo_system.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;
    /**
     * 刷题页面加载题库列表
     * GET /api/practice/packages
     */
    @GetMapping("/packages")
    public Response getPracticePackages(@RequestHeader("X-User-Id") Long userId) {
        System.out.println("用户id " + userId + " 请求刷题题库列表");
        try {
            List<QuestionPackage> list = practiceService.getPracticePackages(userId);
            return Response.ok("成功获取", list);
        } catch (Exception e) {
            return Response.fail("获取题库失败: " + e.getMessage());
        }
    }

    /**
     * 获取题目列表
     * GET /api/practice/questions?packageId=xx&order=order|random&count=all|10|20|50
     */
    @GetMapping("/questions")
    public Response getQuestions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long packageId,
            @RequestParam(defaultValue = "order") String order,
            @RequestParam(defaultValue = "all") String count
    ) {
        System.out.println("用户id " + userId + " 请求题目列表 packageId=" + packageId + ", order=" + order + ", count=" + count);
        try {
            List<QuestionBase> list = practiceService.getQuestions(packageId, order, count);
            return Response.ok("获取成功", list);
        } catch (Exception e) {
            return Response.fail("获取题目失败: " + e.getMessage());
        }
    }

    /**
     * 获取刷题记录
     * GET /api/practice/getRecordVO
     */
    @GetMapping("/getRecordVO")
    public Response getRecordVO(@RequestHeader("X-User-Id") Long userId) {
        System.out.println("用户id " + userId + " 请求刷题记录");
        try {
            List<LearnRecord> list = practiceService.getRecord(userId);
            return Response.ok("获取成功", list);
        } catch (Exception e) {
            return Response.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取刷题记录详情（根据 questionIdSeq 查题目详情）
     * GET /api/practice/getRecordDetails?questionIdSeq=1*2*3*
     */
    @GetMapping("/getRecordDetails")
    public Response getRecordDetails(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String questionIdSeq
    ) {
        System.out.println("用户id " + userId + " 请求刷题详情 questionIdSeq=" + questionIdSeq);
        try {
            List<QuestionBase> list = practiceService.getRecordDetails(questionIdSeq);
            return Response.ok("获取成功", list);
        } catch (Exception e) {
            return Response.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 提交刷题记录
     * POST /api/practice/submitRecord
     */
    @PostMapping("/submitRecord")
    public Response submitRecord(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody LearnRecord record
    ) {
        System.out.println("用户id " + userId + " 请求提交刷题记录");
        try {
            practiceService.submitRecord(userId, record);
            return Response.ok("提交成功", null);
        } catch (Exception e) {
            return Response.fail("提交失败: " + e.getMessage());
        }
    }
}
