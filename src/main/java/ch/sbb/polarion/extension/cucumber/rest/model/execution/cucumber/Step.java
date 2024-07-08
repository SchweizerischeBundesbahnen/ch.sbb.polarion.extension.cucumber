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
public class Step {
    private List<Embedding> embeddings;
    private String keyword;
    private String name;
    private int line;
    private Match match;
    private Result result;
    private List<Row> rows;
}
