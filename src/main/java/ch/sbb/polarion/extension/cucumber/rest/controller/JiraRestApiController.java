package ch.sbb.polarion.extension.cucumber.rest.controller;

import ch.sbb.polarion.extension.cucumber.rest.model.fields.FieldDefinition;
import ch.sbb.polarion.extension.cucumber.rest.model.fields.FieldType;
import ch.sbb.polarion.extension.generic.fields.model.FieldMetadata;
import ch.sbb.polarion.extension.generic.rest.filter.Secured;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.projects.model.IProject;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.tracker.model.ITestRun;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Singleton
@Tag(name = "Fields")
@Secured
@Path("/api/2")
public class JiraRestApiController {

    public static final String PROJECT_ID_HEADER = "projectId";

    private final PolarionService polarionService;

    @SuppressWarnings("unused")
    public JiraRestApiController() {
        this(new PolarionService());
    }

    public JiraRestApiController(PolarionService polarionService) {
        this.polarionService = polarionService;
    }

    @GET
    @Path("/field")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get declared fields list for project",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the list of declared fields",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FieldDefinition.class)))
            )
    )
    public List<FieldDefinition> field(@Parameter(description = "ID of the project", required = true) @HeaderParam(value = PROJECT_ID_HEADER) String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new BadRequestException(String.format("No proper %s header found!", PROJECT_ID_HEADER));
        }

        return polarionService.callPrivileged(() ->
                TransactionalExecutor.executeInReadOnlyTransaction(transaction -> {
                    IProject project = polarionService.getProject(projectId); // validate provided projectId

                    List<FieldDefinition> resultList = new ArrayList<>();
                    Collection<FieldMetadata> customFields = polarionService.getCustomFields(ITestRun.PROTO, project.getContextId(), null);
                    for (FieldMetadata customField : customFields) {
                        //filter out unsupported types
                        if (FieldType.fromIType(customField.getType()) != null) {
                            resultList.add(FieldDefinition.fromFieldMetadata(customField));
                        }
                    }
                    return addStaticFields(resultList);
                })
        );
    }

    private List<FieldDefinition> addStaticFields(List<FieldDefinition> resultList) {
        return Stream.concat(resultList.stream(), Stream.of(FieldDefinition.TITLE, FieldDefinition.DESCRIPTION))
                .toList();
    }
}
