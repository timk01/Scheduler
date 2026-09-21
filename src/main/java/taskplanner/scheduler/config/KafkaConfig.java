package taskplanner.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import taskplanner.scheduler.dto.report.UserReport;
import taskplanner.scheduler.dto.summarization.request.SummarizationRequest;
import taskplanner.scheduler.dto.summarization.response.SummarizationResponse;

@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentMessageListenerContainer<String, SummarizationResponse> replyContainer(
            ConcurrentKafkaListenerContainerFactory<String, SummarizationResponse> factory
    ) {
        ConcurrentMessageListenerContainer<String, SummarizationResponse> container
                = factory.createContainer(KafkaTopics.SUMMARIZATION_REPLIES);

        container.setAutoStartup(false);

        return container;
    }

    @Bean
    public ReplyingKafkaTemplate<String, SummarizationRequest, SummarizationResponse>
    replyingKafkaTemplate(
            ProducerFactory<String, SummarizationRequest> producerFactory,
            ConcurrentMessageListenerContainer<String, SummarizationResponse> replyContainer
    ) {
        return new ReplyingKafkaTemplate<>(
                producerFactory,
                replyContainer
        );
    }

    @Bean
    public KafkaTemplate<String, UserReport> kafkaReportTemplate(
            ProducerFactory<String, UserReport> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
