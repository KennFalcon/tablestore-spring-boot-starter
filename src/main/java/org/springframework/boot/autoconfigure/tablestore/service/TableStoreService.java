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
     * @throws Exception 异常
     */
    <T> PutRowResponse put(T data, Condition condition) throws Exception;

    /**
     * 向TableStore中更新数据
     *
     * @param data       数据
     * @param condition  条件
     * @param deleteNull 是否删除为Null字段
     * @param <T>        泛型
     * @return 返回Update响应
     * @throws Exception 异常
     */
    <T> UpdateRowResponse update(T data, Condition condition, boolean deleteNull) throws Exception;

    /**
     * 从TableStore中删除数据
     *
     * @param table     表名
     * @param keyPairs  主键键值对
     * @param condition 条件
     * @return 返回Delete响应
     */
    DeleteRowResponse delete(String table, List<Pair<String, Object>> keyPairs, Condition condition);

    /**
     * 从TableStore获取数据
     *
     * @param keyPairs    主键键值对
     * @param columnNames 需要返回的列集合
     * @param <T>         泛型
     * @return 返回Get响应
     * @throws Exception 异常
     */
    <T> T get(List<Pair<String, Object>> keyPairs, List<String> columnNames) throws Exception;

    /**
     * 向TableStore中批量插入数据
     *
     * @param dataPairs 数据集合
     * @param <T>       泛型
     * @return 返回BatchWrite响应
     * @throws Exception 异常
     */
    <T> BatchWriteRowResponse batchPut(List<Pair<T, Condition>> dataPairs) throws Exception;

    /**
     * 向TableStore中批量更新数据
     *
     * @param dataPairs  数据集合
     * @param deleteNull 是否删除为Null字段
     * @param <T>        泛型
     * @return 返回BatchWrite响应
     * @throws Exception 异常
     */
    <T> BatchWriteRowResponse batchUpdate(List<Pair<T, Condition>> dataPairs, boolean deleteNull) throws Exception;

    /**
     * 从TableStore范围读取数据
     *
     * @param query range请求
     * @param <T>   泛型
     * @return 返回range响应
     */
    <T> RangeGetReply<T> rangeGet(RangeGetQuery query);

    /**
     * 从TableStore批量读取数据
     *
     * @param query batch请求
     * @param <T>   泛型
     * @return 返回batch响应
     */
    <T> BatchGetReply<T> batchGet(BatchGetQuery query);
}
