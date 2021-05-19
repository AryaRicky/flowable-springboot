1.目前整合了spring，可以在注入RuntimeService和TaskService等来进行启动流程、设置负责人、查询列表等操作。
启动项目时会自动读取processes下的bpmn20.xml结尾的流程订单文件，部署流程。2021-5-16
2.自带rest api接口，不需要其他配置，例如查询所有任务：http://localhost:8080/process-api/runtime/tasks，至于接口路径怎么来的得查一下，文档里现在没找到