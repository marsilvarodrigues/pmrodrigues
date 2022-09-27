package com.pmrodrigues.commons.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.http.MediaType.TEXT_HTML;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(exclude = "message")
@JsonIgnoreProperties({"html","formatMessage"})
@Slf4j
@JsonSerialize
public class Email {
    @NonNull
    private String from;
    private List<String> to;
    private List<String> cc;
    @NonNull
    private String subject;
    private String message;
    @NonNull
    private String type;

    private String template;

    private HashMap<String, Object> parameters = new HashMap<>();

    public boolean isHTML() {
        return TEXT_HTML.toString().equalsIgnoreCase(type);
    }

    public Email to(@NonNull String to) {
        if( this.to == null ) this.to = new ArrayList<>();
        this.to.add(to);
        return this;
    }

    public Email cc(@NonNull String cc) {
        if( this.cc == null ) this.cc = new ArrayList<>();
        this.cc.add(cc);
        return this;
    }

    public Email from(@NonNull String from) {
        this.from = from;
        return this;
    }

    public Email subject(@NonNull String subject) {
        this.subject = subject;
        return this;
    }

    public Email message(@NonNull String message) {
        this.message = message;
        return this;
    }

    public Email type(@NonNull String contentType) {
        this.type = contentType;
        return this;
    }

    @Override
    public Object clone() {
        return new Email(this.from, this.to, this.cc, this.subject, this.type, this.message, this.template, this.parameters);
    }

    public Email set(@NonNull String parameter, @NonNull Object value){
        this.parameters.put(parameter, value);
        return this;
    }

    public String getFormatMessage() {

        if( !StringUtils.isBlank(template) ) {

            val context = new VelocityContext();
            this.parameters.forEach((k,v) -> context.put(k,v));

            val engine = new VelocityEngine();
            engine.setProperty(Velocity.RUNTIME_LOG_INSTANCE, log);
            engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            engine.init();

            val template = engine.getTemplate(this.template);
            val writer = new StringWriter();
            template.merge(context, writer);
            return writer.toString();
        } else {
            return message;
        }
    }

    public Email template(@NonNull String template) {
        this.template = template;
        return this;
    }
}
