package com.microservices.demo.elastic.index.client.service;

import com.microservices.demo.elastic.model.index.IndexModel;
import java.util.List;

/**
 * Service performing elastic-search persistence operations.
 * @param <T> type of index model
 *
 */
public interface ElasticIndexClient<T extends IndexModel> {
    public List<String> save(List<T> documents);
}
