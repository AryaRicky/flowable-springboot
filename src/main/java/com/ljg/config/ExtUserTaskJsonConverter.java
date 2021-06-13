package com.ljg.config;

import org.flowable.bpmn.model.BaseElement;
import org.flowable.editor.language.json.converter.BaseBpmnJsonConverter;
import org.flowable.editor.language.json.converter.UserTaskJsonConverter;

import java.util.Map;

/**
 * 用户任务 Json转换扩展类
 */
public class ExtUserTaskJsonConverter extends UserTaskJsonConverter {

    /**
     * 固定写法，直接拷贝，注意更改节点类型对应的常量即可
     *
     * @param convertersToBpmnMap
     * @param convertersToJsonMap
     */
    public static void customFillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

}