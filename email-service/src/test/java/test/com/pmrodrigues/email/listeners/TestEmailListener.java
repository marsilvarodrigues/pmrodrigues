package test.com.pmrodrigues.email.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrodrigues.commons.dtos.Email;
import com.pmrodrigues.email.listeners.EmailListener;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestEmailListener {

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailListener emailListener;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        val message = mock(MimeMessage.class);
        given(emailSender.createMimeMessage()).willReturn(message);
    }


    @SneakyThrows
    @Test
    public void shouldSend() {

        val email = new Email()
                .to("teste@teste.com")
                .from("teste@teste.com")
                .subject("teste")
                .message("teste")
                .type(MediaType.TEXT_HTML_VALUE);

        emailListener.send(email);

        verify(emailSender).send(any(MimeMessage.class));

    }
}