# tablestore-spring-boot-starter
Aliyun TableStore Spring Boot Starter

基于阿里云TableStore(OTS) Spring Boot自动集成

----------

release:

🚩 更新日志：
1. 修改access-key-id和access-key-secret定义
2. 新增TunnelService，用于创建、删除、查询通道
3. 针对TableStoreService，增加建表、删表、获取表信息接口

使用方式
----------

1. pom.xml中引入
```xml
<dependency>
  <groupId>io.github.kennfalcon</groupId>
  <artifactId>tablestore-spring-boot-starter</artifactId>
  <version>0.0.3</version>
</dependency>
```

2. 在application.yml中增加配置
```yaml
tablestore:
  endpoint: https://xxx.xxx.ots.aliyuncs.com # 表格存储访问地址
  instance: xxx                              # 表格存储实例名
  access-key-id: xxxxx                       # 阿里云访问AccessKeyId
  access-key-secret: xxxxx                   # 阿里云访问AccessKeySecret
```

3. 使用时，会自动注入容器两个Bean

```java
import com.alicloud.openservices.tablestore.SyncClient;
import org.springframework.boot.autoconfigure.tablestore.service.TableStoreService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class A {
    
    @Resource
    private SyncClient syncClient;

    @Resource
    private TableStoreService tableStoreService;

    // ...
}
```

其中

- SyncClient为表格存储提供的操作句柄

- TableStoreService是基于表格存储的Java SDK衍生的一组API，可以方便进行CRUD操作，具体TableService使用方式请查看

TableStoreService API
----------

### 1. 注解说明

@Table
```java
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Table {

    /**
     * 标注该对象映射的表名
     *
     * @return 该对象对应映射的表名
     */
    String name();

    /**
     * 标准该对象映射的索引名
     *
     * @return 该对象映射的索引名
     */
    String index() default "";
}

```

@OtsColumn
```java
import org.springframework.boot.autoconfigure.tablestore.utils.compress.NoCompress;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OtsColumn {
    /**
     * 是否是主键
     */
    boolean primaryKey() default false;

    /**
     * 是否是自增（只有主键可自增）
     */
    boolean autoIncrease() default false;

    /**
     * 表格存储存储的字段名称
     */
    String name() default "";

    /**
     * 是否可写
     */
    boolean writable() default true;

    /**
     * 是否可读
     */
    boolean readable() default true;
    
    /**
     * 存储到OTS中的类型
     */
    OtsColumnType type() default OtsColumnType.NONE;

    /**
     * 压缩（默认不压缩），一般存放byte[]时设置
     */
    Class<?> compress() default NoCompress.class;
}
```

OtsColumnType与OTS类型对应表

| OtsColumnType  | PrimaryKeyType    | ColumnType  |
| :------------- | :---------------- | :---------- |
| STRING         | STRING            | STRING      |
| INTEGER        | INTEGER           | INTEGER     |
| BOOLEAN        | _**Not support**_ | BOOLEAN     |
| DOUBLE         | _**Not support**_ | DOUBLE      |
| BINARY         | BINARY            | BINARY      |

若OtsColumnType为空时，字段类型对应表

| Java Data Type      | PrimaryKeyType    | ColumnType  |
| :------------------ | :---------------- | :---------- |
| Short(short)        | INTEGER           | INTEGER     |
| Integer(int)        | INTEGER           | INTEGER     |
| Long(long)          | INTEGER           | INTEGER     |
| Float(float)        | _**Not support**_ | DOUBLE      |
| Double(double)      | _**Not support**_ | DOUBLE      |
| Boolean(boolean)    | _**Not support**_ | BOOLEAN     |
| String              | STRING            | STRING      |
| Byte\[\](byte\[\])  | BINARY            | BINARY      |

其他类型写入获取读取时，会先转为JSON，以STRING类型写入或读取OTS


### 2. 接口说明

#### 2.1 TableStoreService

```java 
<T> PutRowResponse put(T data, Condition condition)
```

data: 数据

condition: 插入条件

```java 
<T> UpdateRowResponse update(T data, Condition condition, boolean deleteNull)
```

data: 数据

condition: 更新条件

deleteNull: 是否删除空值字段

```java 
<T> DeleteRowResponse delete(String table, T key, Condition condition)
```

table: 表名

key: 主键，支持（PrimaryKey类型，和自定义类型Bean)

condition: 删除条件

#### 2.2 TunnelService

```java
CreateTunnelResponse createTunnel(String tableName, String tunnelName, TunnelType tunnelType)
```

tableName: 数表名

tunnelName: 通道名

tunnelType: 通道类型

```java
DescribeTunnelResponse describeTunnel(String tableName, String tunnelName)
```

tableName: 数表名

tunnelName: 通道名

```java
DeleteTunnelResponse deleteTunnel(String tableName, String tunnelName)
```

tableName: 数表名

tunnelName: 通道名