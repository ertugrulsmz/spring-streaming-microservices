package com.microservices.demo.elastic.query.service.service;

import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;

import java.util.List;

/**
 * Manages api special actions over query service. It is expected to delegate pure elastic query service.
 */
public interface ElasticQueryService {
    ElasticQueryServiceResponseModel getDocumentById(String id);

    List<ElasticQueryServiceResponseModel> getDocumentByText(String text);

    List<ElasticQueryServiceResponseModel> getAllDocuments();

}
