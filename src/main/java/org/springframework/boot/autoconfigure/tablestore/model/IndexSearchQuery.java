package org.springframework.boot.autoconfigure.tablestore.model;

import com.alicloud.openservices.tablestore.model.search.SearchQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;
import com.alicloud.openservices.tablestore.model.search.sort.ScoreSort;
import com.alicloud.openservices.tablestore.model.search.sort.Sort;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-09 13:54
 */
@Getter
@Setter
@Accessors(fluent = true)
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
}
