package com.microservices.demo.elastic.query.util;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 *  Query generator of elastic search that can generate model type independent queries.
 *
 */
@Component
public class ElasticQueryUtil {
    public Query getSearchQueryById(String id) {
        return new NativeSearchQueryBuilder()
                .withIds(Collections.singletonList(id))
                .build();
    }

    public Query getSearchQueryByFieldText(String field, String text) {
        return new NativeSearchQueryBuilder()
                .withQuery(
                        new BoolQueryBuilder()
                                .must(QueryBuilders.matchQuery(field, text)) //--> new MatchQuery()
                )
                .build();
    }

    public Query getSearchQueryForAll(){
        return new NativeSearchQueryBuilder()
                .withQuery(
                        new BoolQueryBuilder()
                        .must(QueryBuilders.matchAllQuery())
                )
                .build();
    }


}
