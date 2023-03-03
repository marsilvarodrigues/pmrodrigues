package test.com.pmrodrigues.users.helper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class HelperPage<T> extends PageImpl<T> {

    @JsonCreator
    public HelperPage(@JsonProperty("content") List<T> content,
                      @JsonProperty("number") int number,
                      @JsonProperty("size") int size,
                      @JsonProperty("totalElements") int totalElements,
                      @JsonProperty("pageable") JsonNode pageable,
                      @JsonProperty("last") boolean last,
                      @JsonProperty("totalPages") int totalPages,
                      @JsonProperty("sort") JsonNode sort,
                      @JsonProperty("first") boolean first,
                      @JsonProperty("numberOfElements") int numberOfElements) {
        super(content, PageRequest.of(number, size), totalElements);
    }
}