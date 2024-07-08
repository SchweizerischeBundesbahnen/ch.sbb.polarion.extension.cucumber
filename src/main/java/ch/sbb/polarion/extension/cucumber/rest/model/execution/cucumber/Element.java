package ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Element {
    @JsonProperty("start_timestamp")
    private Date startTimestamp;
    private int line;
    private String id;
    private String name;
    private String description;
    private String type;
    private String keyword;
    private List<After> after;
    private List<Step> steps;
    private List<Tag> tags;
    private List<Comment> comments;
}
