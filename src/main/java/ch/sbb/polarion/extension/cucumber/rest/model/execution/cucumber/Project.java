package ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionInfo.PROJECT_KEY;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private String id;
    private String key;

    public static Project fromMap(Map<String, Object> map) {

        @SuppressWarnings("unchecked")
        Map<String, String> projectInfoMap = (Map<String, String>) map.get(PROJECT_KEY);
        return new Project(projectInfoMap.get("id"), projectInfoMap.get("key"));
    }
}
