package ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionInfo.PROJECT_KEY;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents a project with its ID and key")
public class Project {

    @Schema(description = "The unique identifier of the project")
    private String id;

    @Schema(description = "The key associated with the project")
    private String key;

    public static Project fromMap(Map<String, Object> map) {

        @SuppressWarnings("unchecked")
        Map<String, String> projectInfoMap = (Map<String, String>) map.get(PROJECT_KEY);
        return new Project(projectInfoMap.get("id"), projectInfoMap.get("key"));
    }
}
