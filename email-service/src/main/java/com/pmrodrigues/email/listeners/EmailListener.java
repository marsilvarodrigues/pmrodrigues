package com.pmrodrigues.email.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.dtos.Email;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
@Component
@Slf4j
public class EmailListener {
    private final JavaMailSender emailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Timed(histogram = true, value = "EmailListener.send")
    @KafkaListener(topics = "email", groupId = "send")
    @SneakyThrows
    public void send(@NonNull @Payload final Email email) {

        log.info("prepare message to be send {}" , email);

        this.sendEmail(email);

    }


    @SneakyThrows
    private void sendEmail(@NonNull final Email email) {

        log.info("sending a email from {} to {} with subject {}", email.getFrom(), email.getTo(), email.getSubject());

        val message = emailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message, email.isHTML(), UTF_8.toString());
        if(helper.isMultipart()) {
            helper.setText(email.getMessage(), email.getFormatMessage());
        }else{
            helper.setText(email.getFormatMessage());
        }
        helper.setFrom(email.getFrom());
        helper.setTo(email.getTo().toArray(new String[0]));
        helper.setCc(Optional.ofNullable(email.getCc())
                        .orElse(List.of())
                        .toArray(new String[0]));
        helper.setSubject(email.getSubject());

        emailSender.send(message);

        log.info("email sent");
    }
}
