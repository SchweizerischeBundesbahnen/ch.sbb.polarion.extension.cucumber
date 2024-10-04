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
public class After {

    @Schema(description = "The result of the executed Cucumber test scenario")
    private Result result;

    @Schema(description = "The match information of the test scenario")
    private Match match;
}
