package com.microservices.demo.elastic.query.service.assembler;

import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.service.api.ElasticDocumentController;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.common.transformer.ElasticToResponseModelTransformer;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adding HATEOS support to dto object.
 */
@Service
public class ElasticQueryResponseModelAssembler
        extends RepresentationModelAssemblerSupport<TwitterIndexModel, ElasticQueryServiceResponseModel> {

    private final ElasticToResponseModelTransformer transformer;

    public ElasticQueryResponseModelAssembler(ElasticToResponseModelTransformer transformer) {
        super(ElasticDocumentController.class, ElasticQueryServiceResponseModel.class);
        this.transformer = transformer;
    }


    @Override
    public ElasticQueryServiceResponseModel toModel(TwitterIndexModel indexModel) {
        ElasticQueryServiceResponseModel responseModel = transformer.getResponseModel(indexModel);

        responseModel.add(
                linkTo(methodOn(ElasticDocumentController.class).getDocumentById(indexModel.getId()))
                .withSelfRel()
        );

        responseModel.add(
          linkTo(ElasticDocumentController.class)
                .withRel("documents")
        );

        return responseModel;
    }

    public List<ElasticQueryServiceResponseModel> toModels(List<TwitterIndexModel> indexModels){
        return indexModels.stream().map(this::toModel).collect(Collectors.toList());
    }
}
