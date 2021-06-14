package com.ljg.config;

import org.flowable.editor.language.json.converter.BpmnJsonConverter;

public class ExtBpmnJsonConverter extends BpmnJsonConverter {
    static {
        convertersToBpmnMap.put(STENCIL_TASK_USER, ExtUserTaskJsonConverter.class);
        ExtUserTaskJsonConverter.customFillTypes(convertersToBpmnMap, convertersToJsonMap);
    }
}