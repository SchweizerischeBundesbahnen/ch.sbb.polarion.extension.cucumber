package ch.sbb.polarion.extension.cucumber.rest.model.execution;

import ch.sbb.polarion.extension.cucumber.rest.model.fields.FieldDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionInfoTest {

    @Test
    void deserializesFields() throws Exception {
        String json = "{\"fields\": {\"project\": {\"key\": \"PRJ\"}, \"summary\": \"My title\", \"customField\": \"value\"}}";

        ExecutionInfo info = new ObjectMapper().readValue(json, ExecutionInfo.class);

        assertThat(info.getFields()).containsKeys("project", "summary", "customField");
        assertThat(info.getProject().getKey()).isEqualTo("PRJ");
        assertThat(info.getCustomFieldIds()).contains("customField").doesNotContain("project");
    }

    @Test
    void getFieldsReturnsUnmodifiableMap() {
        ExecutionInfo info = new ExecutionInfo();
        info.putField(FieldDefinition.TITLE.getId(), "title");
        Map<String, Object> fields = info.getFields();

        assertThrows(UnsupportedOperationException.class, () -> fields.put("key", "value"));
        assertThat(info.getTitle()).isEqualTo("title");
    }

    @Test
    void putFieldResetsCachedProject() {
        ExecutionInfo info = new ExecutionInfo();
        info.putField(ExecutionInfo.PROJECT_KEY, Collections.singletonMap("key", "OLD"));
        assertThat(info.getProject().getKey()).isEqualTo("OLD");

        info.putField(ExecutionInfo.PROJECT_KEY, Collections.singletonMap("key", "NEW"));

        assertThat(info.getProject().getKey()).isEqualTo("NEW");
    }

    @Test
    void setFieldsReplacesContent() {
        ExecutionInfo info = new ExecutionInfo();
        info.putField("old", "value");

        info.setFields(Map.of("new", "value"));

        assertThat(info.getFields()).containsOnlyKeys("new");
    }
}
