package com.microservices.demo.elastic.query.util.service.impl;


import com.microservices.demo.common.util.CollectionsUtil;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.util.exception.ElasticQueryClientException;
import com.microservices.demo.elastic.query.util.repository.TwitterElasticSearchQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.microservices.demo.elastic.query.util.service.ElasticQueryClientService;

import java.util.List;
import java.util.Optional;


/**
 * ElasticSearch Query service using spring elastic repository interface.
 * This service is capable of providing limited operations in elastic search.
 * Alternative is {@link TwitterElasticQueryClientService}
 */
@Service
public class TwitterElasticRepositoryClientService implements ElasticQueryClientService<TwitterIndexModel> {

    private final static Logger logger = LoggerFactory.getLogger(TwitterElasticRepositoryClientService.class);

    private final TwitterElasticSearchQueryRepository repository;

    public TwitterElasticRepositoryClientService(TwitterElasticSearchQueryRepository repository) {
        this.repository = repository;
    }

    @Override
    public TwitterIndexModel getIndexModelById(String id) {
        Optional<TwitterIndexModel> indexModel = repository.findById(id);
        logger.info("Document with id {} is retrieved successfully ",
                indexModel.orElseThrow(
                        ()-> new ElasticQueryClientException("No document find with id "+id))
                        .getId());

        return indexModel.get();
    }

    @Override
    public List<TwitterIndexModel> getIndexModelByText(String text) {
        List<TwitterIndexModel> indexModels = repository.findByText(text);
        logger.info("{} of documents with text {} retrieved successfully",indexModels.size(), text);
        return indexModels;
    }

    @Override
    public List<TwitterIndexModel> getAllIndexModel() {
        List<TwitterIndexModel> indexModels = CollectionsUtil.getInstance().
                listFromIterable(repository.findAll());
        logger.info("{} of documents is retrieved successfully",indexModels.size());
        return indexModels;
    }
}
