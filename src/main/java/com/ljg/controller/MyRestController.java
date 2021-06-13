package com.ljg.controller;

import com.ljg.service.MyService;
import org.flowable.engine.RepositoryService;
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
    private RepositoryService repositoryService;

    @PostMapping("deploy/{procDefKey}")
    public String deploy(@PathVariable String procDefKey) {
        return repositoryService.createDeployment().key(procDefKey).deploy().getId();
    }

    @PostMapping(value="/startProc/{procDefKey}")
    public void startProcessInstance(@PathVariable String procDefKey, @RequestParam String businessKey) {
        myService.startProcess(procDefKey, businessKey);
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