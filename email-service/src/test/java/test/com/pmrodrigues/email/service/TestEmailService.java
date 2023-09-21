package test.com.pmrodrigues.email.service;

import com.pmrodrigues.commons.dtos.Email;
import com.pmrodrigues.email.service.EmailService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestEmailService {

    @Mock
    private KafkaTemplate kafkaTemplate;
    @InjectMocks
    private EmailService emailService;

    @Test
    @SneakyThrows
    void shouldSend() {
        val listenable = mock(ListenableFuture.class);
        given(kafkaTemplate.send(any(String.class), any(Email.class))).willReturn(listenable);

        Email email = new Email().to("teste")
                .from("teste")
                .subject("teste")
                .message("teste");
        emailService.send(email);

        verify(kafkaTemplate, times(1)).send(anyString(), any(Email.class));

    }
}