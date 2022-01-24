package com.microservices.demo.elastic.query.service.service.impl;

import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.service.assembler.ElasticQueryResponseModelAssembler;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.service.ElasticQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.microservices.demo.elastic.query.util.service.ElasticQueryClientService;

import java.util.List;

/**
 * Delegates to elastic query service and do type conversion.
 */
@Service
public class TwitterElasticQueryService implements ElasticQueryService {

    private static final Logger logger = LoggerFactory.getLogger(TwitterElasticQueryService.class);
    private final ElasticQueryClientService<TwitterIndexModel> elasticQueryService;
    private final ElasticQueryResponseModelAssembler assembler;


    public TwitterElasticQueryService(ElasticQueryClientService<TwitterIndexModel> elasticQueryService,
                                      ElasticQueryResponseModelAssembler assembler) {
        this.elasticQueryService = elasticQueryService;
        this.assembler = assembler;
    }

    @Override
    public ElasticQueryServiceResponseModel getDocumentById(String id) {
        TwitterIndexModel documentById = elasticQueryService.getIndexModelById(id);
        return assembler.toModel(documentById);
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getDocumentByText(String text) {
        List<TwitterIndexModel> indexModelByText = elasticQueryService.getIndexModelByText(text);
        return assembler.toModels(indexModelByText);
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getAllDocuments() {
        List<TwitterIndexModel> allIndexModel = elasticQueryService.getAllIndexModel();
        return assembler.toModels(allIndexModel);
    }
}
