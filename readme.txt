1.目前整合了spring，可以在注入RuntimeService和TaskService等来进行启动流程、设置负责人、查询列表等操作。
启动项目时会自动读取processes下的bpmn20.xml结尾的流程订单文件，部署流程。2021-5-16
2.加了flowable-spring-boot-starter-rest依赖可以使用flowable自带rest api接口（我加了后有依赖冲突，把jackson-annotations从中移除就好了），
例如查询所有任务：http://localhost:8080/process-api/runtime/tasks，至于接口路径怎么来的得查一下，文档里现在没找到
3.时区问题是mysql的配置问题，在url加上时区配置即可