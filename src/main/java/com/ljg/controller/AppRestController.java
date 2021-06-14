package com.ljg.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ljg.config.ExtBpmnJsonConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.editor.constants.ModelDataJsonConstants;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/app/rest/")
public class AppRestController {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    IdentityService identityService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final String IMAGE_TYPE = "png";

    private final String activityFontName = "微软雅黑";

    private final String labelFontName = "微软雅黑";

    private final String annotationFontName = "微软雅黑";

    @RequestMapping("authenticate")
    public Map<String, Object> getAuthenticate() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("login", "admin");
        return map;
    }

    @RequestMapping("account")
    public Map<String, Object> getAccount() {
        Map<String, Object> map = new HashMap<>();
        map.put("email", "admin");
        map.put("firstName", "My");
        map.put("fullName", "Administrator");
        map.put("id", "admin");
        map.put("lastName", "Administrator");

        Map<String, Object> groupMap = new HashMap<>();
        map.put("id", "ROLE_ADMIN");
        map.put("name", "Superusers");
        map.put("type", "security-role");

        List<Map<String, Object>> groups = new ArrayList<>();
        groups.add(groupMap);

        map.put("groups", groups);

        return map;
    }

    /**
     * 初始化
     */
    @RequestMapping("models")
    public Map<String, Object> getModels(@RequestParam(value = "filter", required = false) String filter) {
        if (null != filter) {
            return this.getAllModels();
        }
        Map<String, Object> resultMap = new HashMap<>();
        Model model = repositoryService.newModel();
        resultMap.put("modelId", model.getId());
        resultMap.put("name", model.getName());
        resultMap.put("key", model.getKey());
        resultMap.put("description", "");
        resultMap.put("lastUpdated", model.getLastUpdateTime());
        Map<String, Object> editorJsonMap = new HashMap<>();
        editorJsonMap.put("id", "canvas");
        editorJsonMap.put("resourceId", "canvas");
        Map<String, Object> stencilSetMap = new HashMap<>();
        stencilSetMap.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorJsonMap.put("stencilset", stencilSetMap);
        editorJsonMap.put("modelType", "model");
        resultMap.put("model", editorJsonMap);
        return resultMap;
    }

    @GetMapping("/{modelId}/diagram")
    public void getModelImage(@PathVariable("modelId") String modelId, HttpServletResponse response) throws IOException {
        OutputStream os = response.getOutputStream();
        Model model = repositoryService.getModel(modelId);
        //获取流程设计器中内容
        byte[] modelEditorSource = repositoryService.getModelEditorSource(modelId);

        if (modelEditorSource == null || modelEditorSource.length == 0) {
            throw new RuntimeException("流程为空,请刷新再试");
        }
        //设计器内容转String
        String str = new String(modelEditorSource);
        //bpmn和Json对象转换实例
        BpmnJsonConverter bpmnJsonConverter = new ExtBpmnJsonConverter();
        //JsonString转JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(str);
        //JsonNode转BpmnModel
        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(jsonNode);
        InputStream is = generateDiagram(bpmnModel, new ArrayList<>(), new ArrayList<>());

        IOUtils.copy(is, os);
        os.flush();
        os.close();

    }

    private Map<String, Object> getAllModels() {
        Map<String, Object> resultMap = new HashMap<>(4);
        List<Model> list = repositoryService.createModelQuery().list();
        resultMap.put("size", list.size());
        resultMap.put("total", list.size());
        resultMap.put("start", 0);
        resultMap.put("data", list);
        return resultMap;
    }

    /**
     * 获得
     */
    @GetMapping(value = "/models/{modelId}/editor/json", produces = "application/json")
    public Map<String, Object> getModelJson(@PathVariable String modelId) throws JsonProcessingException {
        Model model = repositoryService.getModel(modelId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("modelId", model.getId());
        resultMap.put("name", model.getName());
        resultMap.put("key", model.getKey());
        try {
            resultMap.put("description", new ObjectMapper().readTree(model.getMetaInfo()).get("description").asText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultMap.put("lastUpdated", model.getLastUpdateTime());
        byte[] modelEditorSource = repositoryService.getModelEditorSource(modelId);
        if (null != modelEditorSource && modelEditorSource.length > 0) {
            try {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(modelEditorSource);
                editorJsonNode.put("modelType", "model");
                Map<String, Object> result = objectMapper.convertValue(editorJsonNode, new TypeReference<Map<String, Object>>() {
                });
                resultMap.put("model", result);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Map<String, Object> editorJsonMap = new HashMap<>();
            editorJsonMap.put("id", "canvas");
            editorJsonMap.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorJsonMap.put("stencilset", stencilSetNode);
            editorJsonMap.put("modelType", "model");
            resultMap.put("model", editorJsonMap);
        }
        return resultMap;
    }

    /**
     * 保存
     *
     * @param modelId
     * @param values
     */
    @PostMapping(value = "models/{modelId}/editor/json")
    public Model saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {

        String json = values.getFirst("json_xml");
        String name = values.getFirst("name");
        String description = values.getFirst("description");
        String key = values.getFirst("key");

        Model modelData = repositoryService.getModel(modelId);
        JsonNode modelNode = null;
        try {
            modelNode = new ObjectMapper().readTree(json);
            if (null == modelData) {
                // 新增
                // 验证是否已有模型
                modelExistCheck(key, name);
                // 验证是否已有流程
                processExistCheck(modelNode);
                modelData = repositoryService.newModel();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, StringUtils.defaultString(description));
        modelData.setMetaInfo(modelObjectNode.toString());
        modelData.setName(name);
        modelData.setKey(StringUtils.defaultString(key));

        repositoryService.saveModel(modelData);
        try {
            repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return modelData;
    }

    private void modelExistCheck(String key, String name) {
        List<Model> models = repositoryService.createModelQuery().latestVersion().list();
        Set<String> modelKeys = new HashSet<>();
        Set<String> modelNames = new HashSet<>();
        for (Model model : models) {
            modelKeys.add(model.getKey());
            modelNames.add(model.getName());
        }
        if (modelKeys.contains(key) || modelNames.contains(name)) {
            throw new RuntimeException("模型已存在!请检查模型名称和模型编码是否和已有模型冲突");
        }

    }

    private void processExistCheck(JsonNode modelNode) {
        //JsonNode转BpmnModel
        BpmnJsonConverter bpmnJsonConverter = new ExtBpmnJsonConverter();
        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(modelNode);
        if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
            throw new RuntimeException("该模型没有找到主流程");
        }
        Process mainProcess = bpmnModel.getMainProcess();
        String processDefName = mainProcess.getName();
        String processDefKey = mainProcess.getId();

        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().latestVersion().list();
        Set<String> procDefKeys = new HashSet<>();
        Set<String> procDefNames = new HashSet<>();
        for (ProcessDefinition processDefinition : processDefinitions) {
            procDefKeys.add(processDefinition.getKey());
            procDefNames.add(processDefinition.getName());
        }

        if (procDefKeys.contains(processDefKey) || procDefNames.contains(processDefName)) {
            throw new RuntimeException("流程已存在!请检查流程名称和流程编码是否和已有流程冲突");
        }

    }


    @RequestMapping(value = "export/{modelId}/{type}")
    public void export(@PathVariable("modelId") String modelId,
                       @PathVariable("type") String type,
                       HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new ExtBpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("no main process, can't export for type: " + type);
                response.flushBuffer();
                return;
            }

            String filename = "";
            byte[] exportBytes = null;

            String mainProcessId = bpmnModel.getMainProcess().getId();

            if ("bpmn".equals(type)) {
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                exportBytes = xmlConverter.convertToXML(bpmnModel);
                filename = mainProcessId + ".bpmn20.xml";
            } else if ("json".equals(type)) {
                exportBytes = modelEditorSource;
                filename = mainProcessId + ".json";
            }

            ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
            IOUtils.copy(in, response.getOutputStream());

            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("import-process-model")
    public Map<String, Object> processImport(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml"))) {
            try {
                XMLInputFactory xif = createSafeXmlInputFactory();
                InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), "UTF-8");
                XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                BpmnModel bpmnModel = xmlConverter.convertToBpmnModel(xtr);
                if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                    throw new RuntimeException("No process found in definit1ion " + fileName);
                }

                BpmnJsonConverter bpmnJsonConverter = new ExtBpmnJsonConverter();
                ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);

                Process process = bpmnModel.getMainProcess();
                String name = process.getId();
                if (StringUtils.isNotEmpty(process.getName())) {
                    name = process.getName();
                }
                String description = process.getDocumentation();

                Model newModel = repositoryService.newModel();
                newModel.setVersion(1);
                newModel.setName(name);
                newModel.setKey(process.getId());

                ObjectNode modelObjectNode = objectMapper.createObjectNode();
                modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
                modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
                description = StringUtils.defaultString(description);
                modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
                newModel.setMetaInfo(modelObjectNode.toString());

                repositoryService.saveModel(newModel);
                try {
                    repositoryService.addModelEditorSource(newModel.getId(), modelNode.toString().getBytes("utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Map<String, Object> resultMap = new HashMap<>(1);
                resultMap.put("id", newModel.getId());
                return resultMap;
            } catch (Exception e) {
                log.error("Import failed for {}", fileName, e);
                throw new RuntimeException("Import failed for " + fileName + ", error message " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Invalid file name, only .bpmn and .bpmn20.xml files are supported not " + fileName);
        }
    }

    private static XMLInputFactory createSafeXmlInputFactory() {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        if (xif.isPropertySupported(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)) {
            xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        }

        if (xif.isPropertySupported(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES)) {
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        }

        if (xif.isPropertySupported(XMLInputFactory.SUPPORT_DTD)) {
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        }
        return xif;
    }

    public InputStream generateDiagram(BpmnModel bpmnModel, List<String> highLightedActivities, List<String> highLightedFlows) {
        return new DefaultProcessDiagramGenerator().generateDiagram(bpmnModel, IMAGE_TYPE, highLightedActivities,
                highLightedFlows, activityFontName, labelFontName, annotationFontName,
                null, 1.0, true);
    }

}
