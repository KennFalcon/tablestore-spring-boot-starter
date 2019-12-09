# tablestore-spring-boot-starter
Aliyun TableStore Spring Boot Starter

基于阿里云表格存储Spring Boot自动集成

----------

使用方式
----------

1. pom.xml中引入
```xml
<dependency>
    <groupId>com.kennfalcon.data</groupId>
    <artifactId>tablestore-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

2. 在application.yml中增加配置
```yaml
tablestore:
  endpoint: https://xxx.xxx.ots.aliyuncs.com # 表格存储访问地址
  instance: xxx                              # 表格存储实例名
  ak: xxxxx                                  # 阿里云访问AccessKeyId
  sk: xxxxx                                  # 阿里云访问AccessKeySecret
```
