package ch.sbb.polarion.extension.cucumber.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a feature, including project and work item details, as well as feature content")
public class Feature {

    @Schema(description = "The ID of the project associated with the feature")
    private String projectId;

    @Schema(description = "The ID of the work item linked to the feature")
    private String workItemId;

    @Schema(description = "The title of the feature")
    private String title;

    @Schema(description = "The name of the file containing the feature")
    private String filename;

    @Schema(description = "The content of the feature")
    private String content;
}
