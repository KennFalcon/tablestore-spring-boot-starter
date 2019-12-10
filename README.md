# tablestore-spring-boot-starter
Aliyun TableStore Spring Boot Starter

基于阿里云表格存储Spring Boot自动集成(暂未完成)

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
     *
     * @return
     */
    boolean primaryKey() default false;

    /**
     * 是否是自增（只有主键可自增）
     *
     * @return
     */
    boolean autoIncrease() default false;

    /**
     * 表格存储存储的字段名称
     *
     * @return field name
     */
    String name() default "";

    /**
     * 是否可写
     *
     * @return
     */
    boolean writable() default true;

    /**
     * 是否可读
     *
     * @return
     */
    boolean readable() default true;

    /**
     * 字段类型（默认字段本身类型）
     *
     * @return field class type
     */
    Class<?> clazz() default void.class;

    /**
     * 字段子类型（一般当为List时设置）
     *
     * @return element class type
     */
    Class<?> elementClazz() default void.class;

    /**
     * 字符集（默认utf-8），一般是byte[]转String或String转byte[]时设置
     *
     * @return element charset
     */
    String charset() default "utf-8";

    /**
     * 压缩（默认不压缩），一般存放byte[]时设置
     *
     * @return
     */
    Class<?> compress() default NoCompress.class;
}
```

### 2. 接口说明

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



