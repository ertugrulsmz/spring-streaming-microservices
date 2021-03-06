package com.microservices.demo.elastic.query.web.client.config;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;
import java.util.List;

@Configuration
@Primary
public class ElasticQueryServiceInstanceSupplierConfig implements ServiceInstanceListSupplier {

    private final ElasticQueryWebClientConfigData.WebClient webClientConfig;

    public ElasticQueryServiceInstanceSupplierConfig(ElasticQueryWebClientConfigData configData) {
        this.webClientConfig = configData.getWebClient();
    }

    @Override
    public String getServiceId() {
        return webClientConfig.getServiceId();
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return Flux.just(
                webClientConfig.getInstances()
                .stream().map(i -> new DefaultServiceInstance(
                        i.getId(),
                        i.getHost(),
                        i.getPort(),
                        false
                )).collect(Collectors.toList())
        );
    }
}
