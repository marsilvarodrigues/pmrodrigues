package com.pmrodrigues.email.listeners;

import com.pmrodrigues.commons.dtos.Email;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class EmailConsumerRecordRecovered implements ConsumerRecordRecoverer {

    private KafkaTemplate<String, Email> kafkaTemplate;

    @Override
    public void accept(ConsumerRecord<?, ?> consumerRecord, Exception e) {
        final String key = (String) consumerRecord.key();
        final Email email = (Email) consumerRecord.value();

        log.error("error trying to consume a message {}, {}", key, e);
    }
}
