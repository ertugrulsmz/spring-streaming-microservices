package api;

import com.microservices.demo.kafka.streams.service.runner.StreamsRunner;

import model.KafkaStreamsResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping(value = "/", produces = "application/vnd.api.v1+json")
public class KafkaStreamsController {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsController.class);

    private final StreamsRunner<String, Long> kafkaStreamsRunner;

    public KafkaStreamsController(StreamsRunner<String, Long> streamsRunner) {
        this.kafkaStreamsRunner = streamsRunner;
    }


    public @ResponseBody
    ResponseEntity<KafkaStreamsResponseModel> getWordCountByWord(
            @PathVariable @NotEmpty String word) {
        Long wordCount = kafkaStreamsRunner.getValueByKey(word);
        LOG.info("Word count {} returned for word {}", wordCount, word);
        return ResponseEntity.ok(KafkaStreamsResponseModel.builder()
                .word(word)
                .wordCount(wordCount)
                .build());
    }
}
