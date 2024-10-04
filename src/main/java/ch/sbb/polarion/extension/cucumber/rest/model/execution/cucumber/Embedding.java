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
@Schema(description = "Represents an embedding")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Embedding {
    @Schema(description = "The MIME type of the embedded data")
    @JsonProperty("mime_type")
    private String mimeType;

    @Schema(description = "The base64 encoded data of the embedding")
    private String data;
}
