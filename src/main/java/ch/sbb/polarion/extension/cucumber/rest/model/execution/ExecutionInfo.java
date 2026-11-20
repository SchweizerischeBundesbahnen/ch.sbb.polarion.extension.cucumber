package ch.sbb.polarion.extension.cucumber.rest.model.execution;

import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.Project;
import ch.sbb.polarion.extension.cucumber.rest.model.fields.FieldDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Contains execution information including project details and additional fields")
public class ExecutionInfo {

    public static final String PROJECT_KEY = "project";

    @Schema(description = "A map containing additional fields related to execution")
    private final Map<String, Object> fields = new HashMap<>();

    @Schema(description = "The project associated with this execution")
    private Project project;

    @JsonProperty("fields")
    public Map<String, Object> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @JsonProperty("fields")
    public void setFields(Map<String, Object> fields) {
        this.fields.clear();
        if (fields != null) {
            this.fields.putAll(fields);
        }
        this.project = null;
    }

    public void putField(String key, Object value) {
        fields.put(key, value);
        this.project = null;
    }

    public String getTitle() {
        return getString(FieldDefinition.TITLE.getId());
    }

    public String getDescription() {
        return getString(FieldDefinition.DESCRIPTION.getId());
    }

    public String getTestRunTemplateId() {
        return getString(FieldDefinition.TEST_RUN_TEMPLATE_ID.getId());
    }

    public String getString(String key) {
        return (String) fields.get(key);
    }

    public Set<String> getCustomFieldIds() {
        Set<String> nonCustomFieldIds = new HashSet<>(Arrays.asList(PROJECT_KEY,
                FieldDefinition.TITLE.getId(), FieldDefinition.DESCRIPTION.getId(), FieldDefinition.TEST_RUN_TEMPLATE_ID.getId()));
        return fields.keySet().stream().filter(id -> !nonCustomFieldIds.contains(id)).collect(Collectors.toSet());
    }

    public Project getProject() {
        if (project == null) {
            project = Project.fromMap(fields);
        }
        return project;
    }

    @Override
    public String toString() {
        return "CucumberExecutionInfo{" +
                "fields=" + fields +
                '}';
    }
}
