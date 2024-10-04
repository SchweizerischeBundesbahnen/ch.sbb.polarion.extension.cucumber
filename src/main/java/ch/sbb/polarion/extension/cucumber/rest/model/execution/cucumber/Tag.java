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
@Schema(description = "Represents a tag associated with a Cucumber scenario or feature")
public class Tag {
    @Schema(description = "The name of the tag")
    private String name;

    @Schema(description = "The line number")
    private int line;

    @Schema(description = "The type of the tag")
    private String type;

    @Schema(description = "The location of the tag")
    private Location location;
}
