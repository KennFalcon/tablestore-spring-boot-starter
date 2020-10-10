package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.search.SearchQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;
import com.alicloud.openservices.tablestore.model.search.sort.ScoreSort;
import com.alicloud.openservices.tablestore.model.search.sort.Sort;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created on 2020/10/09
 *
 * @author Kenn
 */
public class IndexSearchQuery {

    private Query query;

    private int offset;

    private int size;

    private boolean getTotalCount;

    private Sort sort = new Sort(Lists.newArrayList(new ScoreSort()));

    private List<String> columns;

    public SearchQuery searchQuery() {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(query);
        searchQuery.setOffset(offset);
        searchQuery.setLimit(size);
        searchQuery.setGetTotalCount(getTotalCount);
        searchQuery.setSort(sort);
        return searchQuery;
    }

    public Query query() {
        return query;
    }

    public void query(Query query) {
        this.query = query;
    }

    public int offset() {
        return offset;
    }

    public void offset(int offset) {
        this.offset = offset;
    }

    public int size() {
        return size;
    }

    public void size(int size) {
        this.size = size;
    }

    public boolean getTotalCount() {
        return getTotalCount;
    }

    public void getTotalCount(boolean getTotalCount) {
        this.getTotalCount = getTotalCount;
    }

    public Sort sort() {
        return sort;
    }

    public void sort(Sort sort) {
        this.sort = sort;
    }

    public List<String> columns() {
        return columns;
    }

    public void columns(List<String> columns) {
        this.columns = columns;
    }
}
