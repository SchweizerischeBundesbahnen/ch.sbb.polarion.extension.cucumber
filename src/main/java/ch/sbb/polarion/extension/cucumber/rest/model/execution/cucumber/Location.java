package ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a location in a Cucumber feature with line and column")
public class Location {

    @Schema(description = "The line number in the feature file")
    private int line;

    @Schema(description = "The column number in the feature file")
    private int column;
}
