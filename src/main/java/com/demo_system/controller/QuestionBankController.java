package com.demo_system.controller;

import com.demo_system.entity.QuestionBankShow;
import com.demo_system.entity.QuestionBase;
import com.demo_system.entity.QuestionPackage;
import com.demo_system.entity.Response;
import com.demo_system.service.QuestionBankService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question-bank")
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @GetMapping("/data_show")
    public Response getDataToShow(@RequestAttribute("userId") Long userId) {
        System.out.println("用户id " + userId + " 在题库管理页请求数据获取");
        try {
            QuestionBankShow overview = questionBankService.getDataToShow(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("packageCount", overview.getPackageCount());
            data.put("questionCount", overview.getQuestionCount());
            data.put("wrongCount", overview.getWrongCount());
            data.put("packageList", overview.getPackageList());

            return Response.ok("获取成功", data);
        } catch (RuntimeException e) {
            return Response.fail("数据请求失败: " + e.getMessage());
        } catch (Exception e) {
            return Response.fail("服务器内部错误: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{packageId}")
    public Response deletePackage(@RequestAttribute("userId") Long userId,
                                  @PathVariable Long packageId) {
        System.out.println("用户id " + userId + " 在题库管理页请求删除题库 packageId=" + packageId);
        try {
            questionBankService.deletePackage(packageId);
            return Response.ok("删除成功", null);
        } catch (RuntimeException e) {
            return Response.fail("删除失败: " + e.getMessage());
        } catch (Exception e) {
            return Response.fail("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 查询某个题库下的所有题目
     */
    @GetMapping("/questions/{packageId}")
    public Response getQuestionsByPackage(@RequestAttribute("userId") Long userId,
                                          @PathVariable Long packageId) {
        System.out.println("用户id " + userId + " 请求查看题库 " + packageId + " 的题目列表");
        try {
            List<QuestionBase> list = questionBankService.getQuestionsByPackage(packageId);
            return Response.ok("获取题目成功", list);
        } catch (RuntimeException e) {
            return Response.fail("获取题目失败: " + e.getMessage());
        } catch (Exception e) {
            return Response.fail("服务器内部错误: " + e.getMessage());
        }
    }

    @PutMapping("/question/update")
    public Response updateQuestion(@RequestAttribute("userId") Long userId,
                                   @RequestBody QuestionBase question) {
        System.out.println("用户id " + userId + " 请求更新题目 questionId=" + question.getId());
        try {
            questionBankService.updateQuestion(question);
            return Response.ok("更新题目成功", null);
        } catch (Exception e) {
            return Response.fail("保存失败: " + e.getMessage());
        }
    }

    @PostMapping("/question/create")
    public Response createQuestion(@RequestAttribute("userId") Long userId,
                                   @RequestBody QuestionBase q) {
        System.out.println("用户id " + userId + " 请求新增题目");
        try {
            q.setUserId(userId);
            questionBankService.insertQuestion(q);
            return Response.ok("新增题目成功", null);
        } catch (Exception e) {
            return Response.fail("新增失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/question/delete/{questionId}")
    public Response deleteQuestion(@RequestAttribute("userId") Long userId,
                                   @PathVariable Long questionId) {
        System.out.println("用户id " + userId + " 请求删除题目 questionId=" + questionId);
        try {
            questionBankService.deleteQuestion(questionId);
            return Response.ok("删除题目成功", null);
        } catch (Exception e) {
            return Response.fail("删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/package/update")
    public Response updatePackageName(@RequestAttribute("userId") Long userId,
                                      @RequestBody QuestionPackage pkg) {
        System.out.println("用户id " + userId + " 请求修改题库名 packageId=" + pkg.getId());
        try {
            questionBankService.updatePackageName(pkg.getId(), pkg.getPackageName());
            return Response.ok("修改成功", null);
        } catch (Exception e) {
            return Response.fail("修改失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public Response createPackage(@RequestAttribute("userId") Long userId) {
        System.out.println("用户id " + userId + " 请求创建题库");
        try {
            Long newId = questionBankService.createPackage(userId);
            Map<String, Object> data = new HashMap<>();
            data.put("packageId", newId);
            return Response.ok("创建成功", data);
        } catch (Exception e) {
            return Response.fail("创建失败: " + e.getMessage());
        }
    }

    @GetMapping("/wrong-list")
    public Response getWrongQuestions(@RequestAttribute("userId") Long userId) {
        System.out.println("用户id " + userId + " 请求错题本列表");
        try {
            List<QuestionBase> list = questionBankService.getWrongQuestions(userId);
            return Response.ok("获取错题成功", list);
        } catch (Exception e) {
            return Response.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 上传 csv 文件解析为题库
     */
    @PostMapping("/import")
    public Response importCsv(@RequestAttribute("userId") Long userId,
                              @RequestPart("file") MultipartFile file) {
        System.out.println("用户id " + userId + " 请求导入CSV题库 file=" + file.getOriginalFilename());
        try {
            return questionBankService.importCsv(file, userId);
        } catch (Exception e) {
            return Response.fail("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出 csv 文件
     */
    @GetMapping("/export/{packageId}")
    public void exportPackage(@RequestAttribute("userId") Long userId,
                              @PathVariable Long packageId,
                              HttpServletResponse response) {
        System.out.println("用户id " + userId + " 请求导出题库 packageId=" + packageId);
        questionBankService.exportCsv(packageId, response);
    }
}
