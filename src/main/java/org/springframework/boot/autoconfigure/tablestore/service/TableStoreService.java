package org.springframework.boot.autoconfigure.tablestore.service;

import com.alicloud.openservices.tablestore.model.BatchWriteRowResponse;
import com.alicloud.openservices.tablestore.model.Condition;
import com.alicloud.openservices.tablestore.model.CreateTableResponse;
import com.alicloud.openservices.tablestore.model.DeleteRowResponse;
import com.alicloud.openservices.tablestore.model.DeleteTableResponse;
import com.alicloud.openservices.tablestore.model.DescribeTableResponse;
import com.alicloud.openservices.tablestore.model.PutRowResponse;
import com.alicloud.openservices.tablestore.model.UpdateRowResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.model.BatchGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.BatchGetReply;
import org.springframework.boot.autoconfigure.tablestore.model.IndexSearchQuery;
import org.springframework.boot.autoconfigure.tablestore.model.IndexSearchReply;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetReply;

import java.util.List;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
public interface TableStoreService {
    /**
     * 在TableStore中创建表
     *
     * @param table 表名
     * @param clazz 泛型类型，表示数据类型，根据数据类型中的字段来获取主键构造信息
     * @param <T>   泛型
     * @return 返回CreateTable响应
     */
    <T> CreateTableResponse createTable(String table, Class<T> clazz);

    /**
     * 在TableStore中创建表
     *
     * @param table           表名
     * @param primaryKeyInfos 主键构造信息
     * @return 返回CreateTable响应
     */
    CreateTableResponse createTable(String table, List<Pair<String, Class<?>>> primaryKeyInfos);

    /**
     * 在TableStore中创建表
     *
     * @param table            表名
     * @param clazz            泛型类型，表示数据类型，根据数据类型中的字段来获取主键构造信息
     * @param timeToLive       数据生命周期，数据生命周期至少为86400秒（一天）或-1（数据永不过期）
     * @param maxVersion       最大版本数
     * @param maxTimeDeviation 有效版本偏差，即写入数据的时间戳与系统当前时间的偏差允许最大值
     * @param allowUpdate      表中的数据是否允许Update操作
     * @param <T>              泛型
     * @return 返回CreateTable响应
     */
    <T> CreateTableResponse createTable(String table, Class<T> clazz, int timeToLive, int maxVersion, long maxTimeDeviation, boolean allowUpdate);

    /**
     * @param table            表名
     * @param primaryKeyInfos  主键构造信息
     * @param timeToLive       数据生命周期，数据生命周期至少为86400秒（一天）或-1（数据永不过期）
     * @param maxVersion       最大版本数
     * @param maxTimeDeviation 有效版本偏差，即写入数据的时间戳与系统当前时间的偏差允许最大值
     * @param allowUpdate      表中的数据是否允许Update操作
     * @return 返回CreateTable响应
     */
    CreateTableResponse createTable(String table, List<Pair<String, Class<?>>> primaryKeyInfos, int timeToLive, int maxVersion, long maxTimeDeviation, boolean allowUpdate);

    /**
     * 从TableStore删除表
     *
     * @param table 表名
     * @return 返回DeleteTable响应
     */
    DeleteTableResponse deleteTable(String table);

    /**
     * 获取表信息描述
     *
     * @param table 表名
     * @return 返回DescribeTable响应
     */
    DescribeTableResponse describeTable(String table);

    /**
     * 向TableStore中插入数据
     *
     * @param data      数据
     * @param condition 条件
     * @param <T>       泛型
     * @return 返回Put响应
     */
    <T> PutRowResponse put(T data, Condition condition);

    /**
     * 向TableStore中更新数据
     *
     * @param data       数据
     * @param condition  条件
     * @param deleteNull 是否删除为Null字段
     * @param <T>        泛型
     * @return 返回Update响应
     */
    <T> UpdateRowResponse update(T data, Condition condition, boolean deleteNull);

    /**
     * 从TableStore中删除数据
     *
     * @param table     表名
     * @param key       主键
     * @param condition 条件
     * @param <T>       泛型
     * @return 返回Delete响应
     */
    <T> DeleteRowResponse delete(String table, T key, Condition condition);

    /**
     * 从TableStore获取数据
     *
     * @param key         主键
     * @param columnNames 需要返回的列集合
     * @param clazz       泛型类型
     * @param <T>         主键泛型
     * @param <U>         返回值泛型
     * @return 返回Get响应
     */
    <T, U> T get(U key, List<String> columnNames, Class<T> clazz);

    /**
     * 向TableStore中批量插入数据
     *
     * @param dataPairs 数据集合
     * @param <T>       泛型
     * @return 返回BatchWrite响应
     */
    <T> BatchWriteRowResponse batchPut(List<Pair<T, Condition>> dataPairs);

    /**
     * 向TableStore中批量更新数据
     *
     * @param dataPairs  数据集合
     * @param deleteNull 是否删除为Null字段
     * @param <T>        泛型
     * @return 返回BatchWrite响应
     */
    <T> BatchWriteRowResponse batchUpdate(List<Pair<T, Condition>> dataPairs, boolean deleteNull);

    /**
     * 从TableStore范围读取数据
     *
     * @param query range请求
     * @param clazz 泛型类型
     * @param <T>   泛型
     * @return 返回range响应
     */
    <T> RangeGetReply<T> rangeGet(RangeGetQuery query, Class<T> clazz);

    /**
     * 从TableStore批量读取数据
     *
     * @param query batch请求
     * @param clazz 泛型类型
     * @param <T>   泛型
     * @return 返回batch响应
     */
    <T> BatchGetReply<T> batchGet(BatchGetQuery query, Class<T> clazz);

    /**
     * 从TableStore查询数据
     *
     * @param query 查询请求
     * @param clazz 泛型类型
     * @param <T>   泛型
     * @return 返回查询响应
     */
    <T> IndexSearchReply<T> search(IndexSearchQuery query, Class<T> clazz);
}
