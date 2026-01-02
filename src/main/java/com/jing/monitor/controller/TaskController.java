package com.jing.monitor.controller;

import com.jing.monitor.common.Result;

import com.jing.monitor.model.dto.TaskRespDto;
import com.jing.monitor.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // 1. Get All Tasks (List)
    @GetMapping
    public Result<List<TaskRespDto>> list() {
        return Result.success(taskService.getAllTasks());
    }

    // 2. Toggle Enable/Disable Status (Update)
    // We use PATCH or PUT for state changes.
    @PatchMapping("/{id}/toggle")
    public Result<TaskRespDto> toggleStatus(@PathVariable Long id) {
        return Result.success(taskService.toggleTaskStatus(id));
    }

    // 3. Add new course
    @PostMapping
    public Result<List<TaskRespDto>> searchAndAdd(@RequestParam String courseName){
        return Result.success(taskService.SearchAndAdd(courseName));
    }

    //4. Delete course
    @DeleteMapping
    public Result<Void> delete(@RequestParam String courseDisplayName){
        taskService.deleteTask(courseDisplayName);
        return Result.success();
    }
}