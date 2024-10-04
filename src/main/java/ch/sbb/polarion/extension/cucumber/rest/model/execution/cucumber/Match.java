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
@Schema(description = "Represents a match in a Cucumber scenario, including arguments and location")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {

    @ArraySchema(schema = @Schema(description = "A list of arguments associated with the match"))
    private List<Argument> arguments;

    @Schema(description = "The location of the match")
    private String location;
}
