package ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents the result of a Cucumber step execution")
public class Result {

    @Schema(description = "The status of the result")
    private String status;

    @Schema(description = "The duration of the step execution")
    private long duration;

    @JsonProperty("error_message")
    @Schema(description = "The error message if the step failed")
    private String errorMessage;
}
