package taskplanner.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;
import taskplanner.scheduler.config.KafkaTopics;
import taskplanner.scheduler.dto.report.UserReport;
import taskplanner.scheduler.dto.summarization.request.SummarizationRequest;
import taskplanner.scheduler.dto.summarization.response.SummarizationResponse;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static taskplanner.scheduler.config.KafkaTopics.SUMMARY_SENDING;

@RequiredArgsConstructor
@Service
public class KafkaService {

    private final ReplyingKafkaTemplate<String, SummarizationRequest, SummarizationResponse> kafkaSummarizationTemplate;

    private final KafkaTemplate<String, UserReport> kafkaReportTemplate;

    public SummarizationResponse processSummarization(SummarizationRequest request) {
        ProducerRecord<String, SummarizationRequest> record
                = new ProducerRecord<>(KafkaTopics.SUMMARIZATION_REQUESTS, request);

        RequestReplyFuture<String, SummarizationRequest, SummarizationResponse> summarization
                = kafkaSummarizationTemplate.sendAndReceive(
                record,
                Duration.ofSeconds(15)
        );

        try {
            ConsumerRecord<String, SummarizationResponse> consumerRecord
                    = summarization.get(15, TimeUnit.SECONDS);
            return consumerRecord.value();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Waiting for summarization was interrupted", e);

        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("Failed to get summarization response", e);
        }
    }

    public void sendMessage(UserReport dto) {
        kafkaReportTemplate.send(SUMMARY_SENDING, dto);
    }
}
