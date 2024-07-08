package ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("java:S107")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CucumberExecutionResult {
    private String keyword;
    private String name;
    private int line;
    private String description;
    private List<Tag> tags;
    private String id;
    private String uri;
    private List<Element> elements;
}
