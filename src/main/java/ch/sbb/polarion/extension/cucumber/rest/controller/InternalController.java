package ch.sbb.polarion.extension.cucumber.rest.controller;

import ch.sbb.polarion.extension.cucumber.helper.PolarionWorkItem;
import ch.sbb.polarion.extension.cucumber.rest.model.Feature;
import ch.sbb.polarion.extension.cucumber.rest.model.ValidationError;
import ch.sbb.polarion.extension.cucumber.rest.model.ValidationResult;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Source;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;

@Tag(name = "Features")
@Hidden
@Path("/internal")
public class InternalController {

    protected final PolarionService polarionService = new PolarionService();

    @GET
    @Path("/feature/{projectId}/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get feature",
            description = "Retrieve a feature by projectId and workItemId",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the feature",
                    content = @Content(schema = @Schema(implementation = Feature.class))
            )
    )
    public Feature getFeature(@Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
                              @Parameter(description = "ID of the work item", required = true) @PathParam("workItemId") String workItemId) {

        return TransactionalExecutor.executeSafelyInReadOnlyTransaction(
                transaction -> PolarionWorkItem.getFeature(polarionService.getTrackerService(), projectId, workItemId));
    }

    @POST
    @Path("/feature")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create/update feature")
    public void createOrUpdateFeature(@Parameter(description = "The feature object to be created or updated", required = true) Feature feature) {
        TransactionalExecutor.executeInWriteTransaction(
                transaction -> PolarionWorkItem.createOrUpdateFeature(polarionService.getTrackerService(), feature));
    }


    @Hidden
    @POST
    @Path("/cucumber/validate")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Validate Cucumber feature",
            description = "Validate the syntax of a Cucumber feature file",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Cucumber feature validated successfully",
                    content = @Content(schema = @Schema(implementation = ValidationResult.class))
            )
    )
    public ValidationResult validateCucumber(String cucumberFeature) {
        GherkinParser parser = GherkinParser.builder().build();
        final Envelope envelope = Envelope.of(new Source("some.feature", cucumberFeature, TEXT_X_CUCUMBER_GHERKIN_PLAIN));
        List<ValidationError> errors = parser.parse(envelope)
                .filter(e -> e.getParseError().isPresent())
                .map(e -> ValidationError.fromParseError(e.getParseError().get()))
                .toList();

        return ValidationResult.builder().result(errors.isEmpty() ? "valid" : "not-valid").errors(errors).build();
    }
}
