package com.todo.smarttodo.controller;


import com.todo.smarttodo.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task-action")
public class TaskActionController {

    private final TaskService taskService;

    public TaskActionController(TaskService taskService) {
        this.taskService = taskService;
    }
    @PostMapping("/snooze")
    public String snooze(@RequestParam Long taskId, @RequestParam(required = false) Integer days) {
        taskService.snooze(taskId, days != null ? days : 1); // default 1
        return "Task snoozed by " + (days != null ? days : 1) + " day(s)";
    }

    @PostMapping("/postpone")
    public String postpone(@RequestParam Long taskId, @RequestParam(required = false) Integer days) {
        taskService.postpone(taskId, days != null ? days : 3); // default 3
        return "Task postponed by " + (days != null ? days : 3) + " day(s)";
    }




}
