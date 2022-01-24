package com.microservices.demo.elastic.query.util.service.impl;

import com.microservices.demo.config.ElasticConfigData;
import com.microservices.demo.config.ElasticQueryConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.util.ElasticQueryUtil;
import com.microservices.demo.elastic.query.util.exception.ElasticQueryClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import com.microservices.demo.elastic.query.util.service.ElasticQueryClientService;

import java.util.List;
import java.util.stream.Collectors;

@Primary
@Service
public class TwitterElasticQueryClientService implements ElasticQueryClientService<TwitterIndexModel> {

    private static final Logger logger = LoggerFactory.getLogger(TwitterElasticQueryClientService.class.getName());

    /**
     * For reading entries in application.properties.
     * each one read one part of application.properties
     */
    private final ElasticConfigData elasticConfigData;
    private final ElasticQueryConfigData elasticQueryConfigData;

    private final ElasticQueryUtil elasticQueryUtil;
    private final ElasticsearchOperations elasticsearchOperations;



    public TwitterElasticQueryClientService(ElasticConfigData elasticConfigData,
                                            ElasticQueryConfigData elasticQueryConfigData,
                                            ElasticQueryUtil elasticQueryUtil,
                                            ElasticsearchOperations elasticsearchOperations) {
        this.elasticConfigData = elasticConfigData;
        this.elasticQueryConfigData = elasticQueryConfigData;
        this.elasticQueryUtil = elasticQueryUtil;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public TwitterIndexModel getIndexModelById(String id) {
        Query searchQueryById = elasticQueryUtil.getSearchQueryById(id);

        SearchHit<TwitterIndexModel> searchHit = elasticsearchOperations.
                searchOne(searchQueryById, TwitterIndexModel.class,
                        getIndexName());

        if(searchHit == null){
            throw new ElasticQueryClientException("No document found at elasticsearch with id "+id);
        }

        logger.info("Document with id {} is retrieved successfully",id);
        return searchHit.getContent();
    }

    @Override
    public List<TwitterIndexModel> getIndexModelByText(String text) {
        String textField = elasticQueryConfigData.getTextField();
        Query searchQueryByFieldText = elasticQueryUtil.getSearchQueryByFieldText(textField, text);

        return search(searchQueryByFieldText,"{} of Documents with text {} is retrieved successfully", text);
        }


    @Override
    public List<TwitterIndexModel> getAllIndexModel() {
        Query searchQueryForAll = elasticQueryUtil.getSearchQueryForAll();
        return search(searchQueryForAll, "{} of documents are retrieved successfully");

    }

    private List<TwitterIndexModel> search(Query query, String logMessage, Object... logParams){
        SearchHits<TwitterIndexModel> searchHits = elasticsearchOperations.search(query, TwitterIndexModel.class,
                getIndexName());

        logger.info(logMessage, searchHits.getTotalHits(),logParams);
        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    private IndexCoordinates getIndexName() {
        return IndexCoordinates.of(elasticConfigData.getIndexName());
    }
}
