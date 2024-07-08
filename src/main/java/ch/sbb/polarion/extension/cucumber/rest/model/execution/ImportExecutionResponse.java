package ch.sbb.polarion.extension.cucumber.rest.model.execution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportExecutionResponse {
    private TestExecIssue testExecIssue;
}
