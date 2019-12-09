package org.springframework.boot.autoconfigure.tablestore.service;

import com.alicloud.openservices.tablestore.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.tablestore.model.BatchGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.BatchGetReply;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetQuery;
import org.springframework.boot.autoconfigure.tablestore.model.RangeGetReply;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-02-28 10:58
 */
public interface TableStoreService {

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
     * @param keyPairs  主键键值对
     * @param condition 条件
     * @return 返回Delete响应
     */
    <T> DeleteRowResponse delete(String table, T keyPairs, Condition condition);

    /**
     * 从TableStore获取数据
     *
     * @param key         主键
     * @param columnNames 需要返回的列集合
     * @param clazz 泛型类型
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
}
