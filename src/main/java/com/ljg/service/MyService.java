package com.ljg.service;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MyService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Transactional
    public void startProcess() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess");
    }

    @Transactional
    public List<Task> getTasks(String assignee) {
        List<Task> list = taskService.createTaskQuery().list();
        System.out.println(list);
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }


    @Transactional
    public void setAssignee(String assignee) {
        List<Task> list = taskService.createTaskQuery().taskName("my task").list();
        System.out.println(list);
        list.forEach(task->{
            taskService.setAssignee(task.getId(), assignee);
        });
    }

}