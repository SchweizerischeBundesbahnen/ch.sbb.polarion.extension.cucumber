package ch.sbb.polarion.extension.cucumber.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feature {
    private String projectId;
    private String workItemId;
    private String title;
    private String filename;
    private String content;
}
