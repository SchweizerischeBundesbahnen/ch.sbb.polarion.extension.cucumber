package ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("java:S107")
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a step in a Cucumber scenario, including embeddings, match, result, and rows")
public class Step {

    @ArraySchema(schema = @Schema(description = "List of embeddings associated with the step", implementation = Embedding.class))
    private List<Embedding> embeddings;

    @Schema(description = "The keyword of the step")
    private String keyword;

    @Schema(description = "The name or description of the step")
    private String name;

    @Schema(description = "The line number of the step in the feature file")
    private int line;

    @Schema(description = "The match information for the step, showing how the step matches code", implementation = Match.class)
    private Match match;

    @Schema(description = "The result of the step execution", implementation = Result.class)
    private Result result;

    @ArraySchema(schema = @Schema(description = "List of rows (for data tables) associated with the step", implementation = Row.class))
    private List<Row> rows;
}
