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
@Schema(description = "Represents the result of a Cucumber test execution")
public class CucumberExecutionResult {

    @Schema(description = "The keyword of the test")
    private String keyword;

    @Schema(description = "The name of the test")
    private String name;

    @Schema(description = "The line number in the feature file")
    private int line;

    @Schema(description = "The description of the test")
    private String description;

    @ArraySchema(schema = @Schema(description = "List of tags associated with the test", implementation = Tag.class))
    private List<Tag> tags;

    @Schema(description = "The unique ID of the test")
    private String id;

    @Schema(description = "The URI of the feature file")
    private String uri;

    @ArraySchema(schema = @Schema(description = "List of elements (steps) associated with the test", implementation = Element.class))
    private List<Element> elements;
}
