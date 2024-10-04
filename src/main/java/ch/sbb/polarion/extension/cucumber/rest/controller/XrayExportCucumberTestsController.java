package ch.sbb.polarion.extension.cucumber.rest.controller;

import ch.sbb.polarion.extension.cucumber.helper.PolarionWorkItem;
import ch.sbb.polarion.extension.cucumber.rest.model.Feature;
import ch.sbb.polarion.extension.generic.rest.filter.Secured;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.persistence.model.IPObjectList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

@Tag(name = "Export")
@Secured
@Path("/raven/1.0/export")

public class XrayExportCucumberTestsController {

    private static final Logger logger = Logger.getLogger(XrayExportCucumberTestsController.class);

    private final PolarionService polarionService;

    @SuppressWarnings("unused")
    public XrayExportCucumberTestsController() {
        this(new PolarionService());
    }

    XrayExportCucumberTestsController(PolarionService polarionService) {
        this.polarionService = polarionService;
    }

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Get features list", responses = @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the list of features"
    ))
    public Response exportTest(
            @Parameter(description = "String which contain list of work items information (e.g. 'elibrary/EL-103;drivepilot/DP-47')") @QueryParam("keys") String keys,
            @Parameter(description = "Query which will be passed to the trackerService#queryWorkItems method") @QueryParam("filter") String query,
            @Parameter(description = "True - the response will contain zip-archive with all workitems found, false - only first workitem with no archiving") @QueryParam("fz") boolean fz) {

        List<Feature> features = polarionService.callPrivileged(() -> TransactionalExecutor.executeSafelyInReadOnlyTransaction(transaction -> findFeatures(keys, query)));

        return createResponse(features, fz);
    }

    @NotNull
    private List<Feature> findFeatures(@Nullable String keys, @Nullable String query) {
        List<Feature> result = new ArrayList<>();

        if (keys != null) {
            result.addAll(findFeaturesByWorkItemIds(keys));
        }
        if (query != null) {
            result.addAll(findFeaturesByQuery(query));
        }

        return result;
    }

    @NotNull
    private List<Feature> findFeaturesByWorkItemIds(@NotNull String keys) {
        List<Feature> result = new ArrayList<>();

        String[] workItemIds = keys.split(";");
        for (String workItemId : workItemIds) {
            Feature feature = PolarionWorkItem.getFeature(polarionService.getTrackerService(), workItemId);
            result.add(feature);
        }

        return result;
    }

    @NotNull
    @SuppressWarnings("squid:S1166") // no need to log or rethrow exception by design
    private List<Feature> findFeaturesByQuery(@NotNull String query) {
        List<Feature> result = new ArrayList<>();

        @SuppressWarnings("unchecked")
        IPObjectList<IWorkItem> workItemList = polarionService.getTrackerService().queryWorkItems(query, "id");
        workItemList.forEach(wi -> {
            try {
                Feature feature = PolarionWorkItem.getFeature(polarionService.getTrackerService(), wi.getProjectId(), wi.getId());
                result.add(feature);
            } catch (NotFoundException e) {
                // nothing to do: test case has no cucumber feature
            }
        });

        return result;
    }

    private Response createResponse(List<Feature> features, boolean fz) {
        if (features.isEmpty()) {
            throw new BadRequestException("No cucumber features found!");
        }

        if (fz) {
            return createZipFileResponse(features);
        } else {
            return createSingleFeatureResponse(features.get(0));
        }
    }

    private Response createSingleFeatureResponse(@NotNull Feature feature) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                outputStream.write(feature.getContent().getBytes(StandardCharsets.UTF_8));
            }
        };

        return Response.ok(streamingOutput).build();
    }

    private Response createZipFileResponse(@NotNull List<Feature> features) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                    for (Feature feature : features) {
                        try {
                            String filename = feature.getTitle() != null ? feature.getTitle() : feature.getFilename();
                            ZipEntry zipEntry = new ZipEntry(filename);
                            zipOutputStream.putNextEntry(zipEntry);
                            zipOutputStream.write(feature.getContent().getBytes(StandardCharsets.UTF_8));
                            zipOutputStream.closeEntry();
                        } catch (ZipException e) {
                            logger.error(e);
                        }
                    }
                }
            }
        };

        return Response.ok(streamingOutput).build();
    }
}
