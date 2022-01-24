package com.microservices.demo.elastic.query.util.service;

import com.microservices.demo.elastic.model.index.IndexModel;

import java.util.List;

/**
 * Elastic query client service which performs query operations in elastic search
 * @param <T> type of index model to be queried
 */
public interface ElasticQueryClientService<T extends IndexModel> {
    T getIndexModelById(String id);
    List<T> getIndexModelByText(String text);
    List<T> getAllIndexModel();
}
