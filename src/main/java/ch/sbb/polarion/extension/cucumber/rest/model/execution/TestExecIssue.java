package ch.sbb.polarion.extension.cucumber.rest.model.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a test execution issue")
public class TestExecIssue {

    @Schema(description = "The unique identifier of the test execution issue")
    private String id;

    @Schema(description = "The key of the test execution issue")
    private String key;

    @Schema(description = "The self URL of the test execution issue")
    private String self;
}
