package ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"java:S107"})
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Represents an element within a Cucumber feature")
public class Element {

    @Schema(description = "The start timestamp of the element execution")
    @JsonProperty("start_timestamp")
    private Date startTimestamp;

    @Schema(description = "The line number in the feature file")
    private int line;

    @Schema(description = "The unique ID of the element")
    private String id;

    @Schema(description = "The name of the element")
    private String name;

    @Schema(description = "The description of the element")
    private String description;

    @Schema(description = "The type of the element")
    private String type;

    @Schema(description = "The keyword of the element")
    private String keyword;

    @ArraySchema(schema = @Schema(description = "List of 'after' hooks for the element", implementation = After.class))
    private List<After> after;

    @ArraySchema(schema = @Schema(description = "List of steps in the element", implementation = Step.class))
    private List<Step> steps;

    @ArraySchema(schema = @Schema(description = "List of tags associated with the element", implementation = Tag.class))
    private List<Tag> tags;

    @ArraySchema(schema = @Schema(description = "List of comments associated with the element", implementation = Comment.class))
    private List<Comment> comments;
}
