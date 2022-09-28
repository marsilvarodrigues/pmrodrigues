package test.com.pmrodrigues.email.service;

import com.pmrodrigues.email.exception.TemplateNotFoundException;
import com.pmrodrigues.email.service.EmailTemplateService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = EmailTemplateService.class)
@TestPropertySource(properties = { "spring.config.location=classpath:emails.yaml" })
public class TestTemplateService {

    @Autowired
    private EmailTemplateService templateService;

    @Test
    void shouldLoadTemplates() {
        assertNotNull(templateService.getEmails());
    }

    @Test
    void shouldFoundATemplate() {
        val email = templateService.getByEmailType("newUser");
        assertNotNull(email);
    }

    @Test
    void shouldNotFountATemplate() {

        assertThrows(TemplateNotFoundException.class,
                () -> templateService.getByEmailType("not_exist")
        );

    }
}
