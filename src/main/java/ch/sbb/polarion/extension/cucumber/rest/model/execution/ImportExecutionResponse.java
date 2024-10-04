package ch.sbb.polarion.extension.cucumber.rest.model.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents the response after importing a test execution, containing the test execution issue details")
public class ImportExecutionResponse {
    @Schema(description = "Details of the test execution issue associated with the import")
    private TestExecIssue testExecIssue;
}
