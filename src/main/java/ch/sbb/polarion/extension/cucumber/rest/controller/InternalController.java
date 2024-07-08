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

    @Operation(summary = "Get feature")
    @GET
    @Path("/feature/{projectId}/{workItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Feature getFeature(@PathParam("projectId") String projectId, @PathParam("workItemId") String workItemId) {

        return TransactionalExecutor.executeSafelyInReadOnlyTransaction(
                transaction -> PolarionWorkItem.getFeature(polarionService.getTrackerService(), projectId, workItemId));
    }

    @Operation(summary = "Create/update feature")
    @POST
    @Path("/feature")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createOrUpdateFeature(Feature feature) {
        TransactionalExecutor.executeInWriteTransaction(
                transaction -> PolarionWorkItem.createOrUpdateFeature(polarionService.getTrackerService(), feature));
    }


    @Hidden
    @POST
    @Path("/cucumber/validate")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
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
