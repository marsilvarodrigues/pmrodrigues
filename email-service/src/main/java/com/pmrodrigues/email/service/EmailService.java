package com.pmrodrigues.email.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.dtos.Email;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@RequiredArgsConstructor
@Component
@Slf4j
public class EmailService {

    private final KafkaTemplate<String, Email> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Timed
    public void send(@NonNull final Email email) throws JsonProcessingException {

        log.info("send a email {}", email );

        ListenableFuture<SendResult<String, Email>> future =
                kafkaTemplate.send("email", email);

        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, Email> result) {
                log.info("Sent message=[{}] with offset=[{}]",
                        email,
                        result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Unable to send message=[{}] due to : {}",
                        email,
                        ex.getMessage(),
                        ex);
            }
        });

    }
}
