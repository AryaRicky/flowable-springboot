package com.ljg.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ljg.service.MyService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("task")
@RestController
public class MyRestController {

    @Resource
    private MyService myService;
    @Resource
    private TaskService taskService;

    @PostMapping("deploy/{modelId}")
    public String deploy(@PathVariable String modelId) {
        try {
            return myService.deploymentBpmn(modelId);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @PostMapping(value="/startProc/{procDefKey}")
    public void startProcessInstance(@PathVariable String procDefKey, @RequestParam String businessKey) {
        myService.startProcess(procDefKey, businessKey);
    }

    @PostMapping(value="/complete/{taskId}")
    public String complete(@PathVariable String taskId) {
        taskService.complete(taskId);
        return "ok";
    }

    @PostMapping(value="/setAssignee")
    public void setAssignee(@RequestParam String assignee) {
        myService.setAssignee(assignee);
    }

    @GetMapping(value="/myTasks")
    public List<TaskRepresentation> getTasks(@RequestParam String assignee) {
        List<Task> tasks = myService.getTasks(assignee);
        List<TaskRepresentation> dtos = new ArrayList<TaskRepresentation>();
        for (Task task : tasks) {
            dtos.add(new TaskRepresentation(task.getId(), task.getName()));
        }
        return dtos;
    }

    static class TaskRepresentation {

        private String id;
        private String name;

        public TaskRepresentation(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

    }

}