package com.microservices.demo.kafka.streams.service.runner;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.KafkaStreamsConfigData;
import com.microservices.demo.kafka.avro.model.TwitterAnalyticsAvroModel;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class KafkaStreamRunner implements StreamsRunner<String, Long> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamRunner.class);
    private static final String REGEX = "\\W+";

    private final KafkaStreamsConfigData kafkaStreamsConfigData;
    private final KafkaConfigData kafkaConfigData;
    private final Properties streamsConfiguration;

    private KafkaStreams kafkaStreams;
    private volatile ReadOnlyKeyValueStore<String,Long> keyValueStore;

    public KafkaStreamRunner(KafkaStreamsConfigData kafkaStreamsConfigData, KafkaConfigData kafkaConfigData,
                             @Qualifier("streamConfiguration") Properties streamsConfiguration) {
        this.kafkaStreamsConfigData = kafkaStreamsConfigData;
        this.kafkaConfigData = kafkaConfigData;
        this.streamsConfiguration = streamsConfiguration;
    }

    @Override
    public void start() {
        final StreamsBuilder streamsBuilder = new StreamsBuilder();

        final Map<String, String> serdeConfig = Collections.singletonMap(
                kafkaConfigData.getSchemaRegistryUrlKey(),
                kafkaConfigData.getSchemaRegistryUrl()
        );

        KStream<Long, TwitterAvroModel> twitterAvroModelKstream =
                getTwitterAvroModelKStream(streamsBuilder, serdeConfig);

        createTopology(twitterAvroModelKstream, serdeConfig);

        startStreaming(streamsBuilder);

    }

    private void startStreaming(StreamsBuilder streamsBuilder) {
        Topology topology = streamsBuilder.build();
        LOG.info("Defined Topology {}",topology.describe());

        kafkaStreams = new KafkaStreams(topology, streamsConfiguration);
        kafkaStreams.start();
        LOG.info("Kafka streaming started");
    }

    private void createTopology(KStream<Long, TwitterAvroModel> twitterAvroModelKstream, Map<String, String> serdeConfig) {
        Pattern pattern = Pattern.compile(REGEX, Pattern.UNICODE_CHARACTER_CLASS);
        Serde<TwitterAnalyticsAvroModel> serdeTwitterAnalyticsAvroModel
                = getSerdeAnalyticsModel(serdeConfig);

        twitterAvroModelKstream
                .flatMapValues(value -> Arrays.asList(pattern.split(value.getText().toLowerCase())))
                //returns Kstream
                .groupBy((key, word) -> word) //returns KGroupedStream
                .count(Materialized.as(kafkaStreamsConfigData.getWordCountStoreName())) //KTable
                .toStream() //Kstream<String,Long>
                .map(mapToAnalyticsModel())
                .to(kafkaStreamsConfigData.getOutputTopicName(),
                        Produced.with(Serdes.String(), serdeTwitterAnalyticsAvroModel));

    }

    private KeyValueMapper<String, Long, KeyValue<? extends String, ? extends TwitterAnalyticsAvroModel>> mapToAnalyticsModel() {
        return (word, count) -> {
            LOG.info("Sending topic {} , word {} - count {}",
                    kafkaStreamsConfigData.getOutputTopicName(), word, count);
            return new KeyValue<> (word, TwitterAnalyticsAvroModel.newBuilder()
                    .setWord(word)
                    .setWordCount(count)
                    .setCreatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                    .build());
        };
    }

    private Serde<TwitterAnalyticsAvroModel> getSerdeAnalyticsModel(Map<String, String> serdeConfig) {
        Serde<TwitterAnalyticsAvroModel> serdeAnalyticsModel = new SpecificAvroSerde<>();
        serdeAnalyticsModel.configure(serdeConfig, false);
        return serdeAnalyticsModel;
    }

    private KStream<Long, TwitterAvroModel> getTwitterAvroModelKStream(StreamsBuilder streamsBuilder,
                                                                       Map<String, String> serdeConfig) {
        final Serde<TwitterAvroModel> serdeTwitterAvroModel = new SpecificAvroSerde<>();

        serdeTwitterAvroModel.configure(serdeConfig, false);

        KStream<Long, TwitterAvroModel> twitterAvroModelKstream = streamsBuilder.stream(kafkaStreamsConfigData.getInputTopicName(),
                Consumed.with(Serdes.Long(), serdeTwitterAvroModel));
        return twitterAvroModelKstream;
    }

    @PreDestroy
    public void close(){
        if(kafkaStreams != null){
            kafkaStreams.close();
            LOG.info("Kafka streams is closing");
        }
    }

    @Override
    public Long getValueByKey(String key) {
        if(kafkaStreams!=null && kafkaStreams.state() == KafkaStreams.State.RUNNING){
            if(keyValueStore == null){
                synchronized(this){
                    if(keyValueStore == null){
                        keyValueStore = kafkaStreams.store(
                                StoreQueryParameters.fromNameAndType(
                                        kafkaStreamsConfigData.getWordCountStoreName(),
                                        QueryableStoreTypes.keyValueStore()
                                )
                        );
                    }
                }
            }
            return keyValueStore.get(key.toLowerCase());
        }
        return 0L;
    }
}
