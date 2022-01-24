package com.microservices.demo.elastic.index.client.service.impl;

import com.microservices.demo.elastic.index.client.repository.TwitterElasticIndexRepository;
import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

//@Primary

@Service
@ConditionalOnProperty(name="elastic-config.is-repository", havingValue="true", matchIfMissing = true)
public class TwitterElasticRepositoryIndexClient implements ElasticIndexClient<TwitterIndexModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticRepositoryIndexClient.class);
    private final TwitterElasticIndexRepository repository;

    public TwitterElasticRepositoryIndexClient(TwitterElasticIndexRepository repository) {
        this.repository = repository;
    }


    @Override
    public List<String> save(List<TwitterIndexModel> documents) {
        List<TwitterIndexModel> twitterIndexModels = (List<TwitterIndexModel>) repository.saveAll(documents);
        List<String> ids = twitterIndexModels.stream().map(TwitterIndexModel::getId)
                .collect(Collectors.toList());

        LOGGER.info("Document indexed successfully with type {} and ids: {} ",
                TwitterIndexModel.class.getName(), ids);
        return ids;
    }
}
