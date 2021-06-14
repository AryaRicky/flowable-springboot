package com.ljg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljg.config.ExtBpmnJsonConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class MyService {

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private RepositoryService repositoryService;

    private static final String RESOURCE_NAME_SUFFIX = ".bpmn20.xml";

    public String deploymentBpmn(String modelId) throws JsonProcessingException {
        //获取model实例
        Model model = repositoryService.getModel(modelId);
        //获取流程设计器中内容
        byte[] modelEditorSource = repositoryService.getModelEditorSource(modelId);

        if (modelEditorSource == null || modelEditorSource.length == 0) {
            throw new RuntimeException("流程为空,请刷新再试");
        }
        //设计器内容转String
        String str = new String(modelEditorSource, StandardCharsets.UTF_8);
        //bpmn和Json对象转换实例
        BpmnJsonConverter bpmnJsonConverter = new ExtBpmnJsonConverter();
        //JsonString转JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(str);
        //JsonNode转BpmnModel
        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(jsonNode);

        if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
            throw new RuntimeException("该模型没有找到主流程");
        }
        //部署
        Deployment deploy = repositoryService.createDeployment().key(model.getKey()).name(model.getName()).addBpmnModel(model.getName() + RESOURCE_NAME_SUFFIX, bpmnModel).deploy();
        // 记录部署id
        model.setDeploymentId(deploy.getId());
        repositoryService.saveModel(model);
        if (deploy.getDeploymentTime() == null) {
            throw new RuntimeException("流程部署失败!请联系开发人员");
        }
        return "部署成功，" + new ObjectMapper().writeValueAsString(deploy);
    }

    @Transactional
    public void startProcess(String procDefKey, String businessKey) {
        runtimeService.startProcessInstanceByKey(procDefKey, businessKey);
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