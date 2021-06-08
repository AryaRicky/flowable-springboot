1.目前整合了spring，可以在注入RuntimeService和TaskService等来进行启动流程、设置负责人、查询列表等操作。
启动项目时会自动读取processes下的bpmn20.xml结尾的流程订单文件，部署流程。2021-5-16
2.加了flowable-spring-boot-starter-rest依赖可以使用flowable自带rest api接口（我加了后有依赖冲突，把jackson-annotations从中移除就好了），
例如查询所有任务：http://localhost:8080/process-api/runtime/tasks，至于接口路径怎么来的得查一下，文档里现在没找到
3.时区问题是mysql的配置问题，在url加上时区配置即可
4.下载了flowable-6.6.0，里面有flowable-ui.war，可以直接java -jar flowable-ui.war去运营，之后到管理页面登陆admin/test即可以到流程编辑页面
去创建流程并保存。默认是h2数据库保存，我下载了tomcat8，直接丢到webapps上面，删除ROOT，修改8085端口，运行./start.sh;tail -f ../logs/catalina.out
会自动部署，修改解压后的文件夹里的数据库配置，修改喂我自己的mysql配置，即可使用自己的数据库了。
5.todo 集成流程设计器到此项目中来，不要每次都单独部署。思路：把相关累copy进来，看看缺什么jar引入即可
