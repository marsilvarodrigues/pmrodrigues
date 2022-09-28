package com.pmrodrigues.email.service;

import com.pmrodrigues.commons.dtos.Email;
import com.pmrodrigues.email.exception.TemplateNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@ConfigurationProperties(prefix = "emails")
public class EmailTemplateService {

    @Setter
    private List<Map<String,Email>> templates;

    @Getter
    private final Map<String,Email> emails = new HashMap<>();

    public Email getByEmailType(String template){

        if( this.emails.isEmpty() ) this.postConstruct();

        return Optional.ofNullable(emails.get(template))
                            .orElseThrow(TemplateNotFoundException::new)
                            .copy();
    }

    @PostConstruct
    public void postConstruct() {

        if( templates == null ) return;
        templates.forEach(emails::putAll);
    }

}
