package ch.sbb.polarion.extension.cucumber.rest.controller;

import ch.sbb.polarion.extension.cucumber.helper.PolarionTestRun;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionInfo;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionRecord;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ImportExecutionResponse;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.TestExecIssue;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.CucumberExecutionResult;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.junit.TestSuite;
import ch.sbb.polarion.extension.generic.rest.filter.Secured;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.tracker.ITestManagementService;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.core.util.StringUtils;
import com.polarion.core.util.logging.Logger;
import com.polarion.portal.internal.server.navigation.TestManagementServiceAccessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Tag(name = "Import")
@Secured
@Path("/raven/1.0/import/execution")
@SuppressWarnings("unused")
public class XrayImportExecutionResultsController {

    private static final Logger logger = Logger.getLogger(XrayImportExecutionResultsController.class);

    private final PolarionService polarionService;

    private final ITestManagementService testManagementService;

    @Context
    private UriInfo uriInfo;

    public XrayImportExecutionResultsController() {
        this(new PolarionService(), new TestManagementServiceAccessor().getTestingService());
    }

    public XrayImportExecutionResultsController(PolarionService polarionService, ITestManagementService testManagementService) {
        this.polarionService = polarionService;
        this.testManagementService = testManagementService;
    }

    @POST
    @Path("/cucumber/multipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Import cucumber test result (multipart)",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Cucumber test result successfully imported",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ImportExecutionResponse.class)
                    )
            )
    )
    public ImportExecutionResponse importExecutionCucumberMultipart(
            @FormDataParam("info") FormDataBodyPart info,
            @FormDataParam("result") FormDataBodyPart result) {
        info.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        result.setMediaType(MediaType.APPLICATION_JSON_TYPE);

        logger.info("cucumberExecutionInfo = " + info.getValueAs(String.class));
        logger.info("cucumberExecutionResult = " + result.getValueAs(String.class));

        ExecutionInfo executionInfo = info.getValueAs(ExecutionInfo.class);
        CucumberExecutionResult[] cucumberExecutionResults = result.getValueAs(CucumberExecutionResult[].class);

        List<ITestRun> testRuns = polarionService.callPrivileged(
                () -> TransactionalExecutor.executeInWriteTransaction(transaction -> PolarionTestRun.createTestRuns(
                        polarionService,
                        testManagementService,
                        executionInfo,
                        ExecutionRecord.fromCucumberExecutions(Arrays.asList(cucumberExecutionResults))))
        );

        return createImportExecutionResponse(testRuns);
    }

    @POST
    @Path("/junit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Import JUnit test result", responses = @ApiResponse(
            responseCode = "200",
            description = "JUnit test result successfully imported",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ImportExecutionResponse.class)
            )
    ))
    public ImportExecutionResponse importExecutionJunit(
            @QueryParam("projectKey") String projectKey,
            @QueryParam("testExecKey") String testExecKey,
            @QueryParam("testPlanKey") String testPlanKey,
            @QueryParam("testEnvironments") String testEnvironments,
            @QueryParam("revision") String revision,
            @QueryParam("fixVersion") String fixVersion,
            @FormDataParam("file") FormDataBodyPart file) {
        file.setMediaType(MediaType.APPLICATION_XML_TYPE);

        if (StringUtils.isEmpty(projectKey) && StringUtils.isEmpty(testExecKey)) {
            throw new BadRequestException("projectKey or testExecKey must be provided");
        }

        ExecutionInfo info = new ExecutionInfo();
        Map<String, String> projectMap = new HashMap<>();
        projectMap.put("id", projectKey);
        projectMap.put("key", testExecKey);
        info.getFields().put(ExecutionInfo.PROJECT_KEY, projectMap);

        return importJUnitTests(info, file);
    }

    private ImportExecutionResponse importJUnitTests(ExecutionInfo info, FormDataBodyPart file) {
        TestSuite testSuite = file.getValueAs(TestSuite.class);
        List<ITestRun> testRuns = polarionService.callPrivileged(
                () -> TransactionalExecutor.executeInWriteTransaction(transaction -> PolarionTestRun.createTestRuns(
                        polarionService,
                        testManagementService,
                        info,
                        ExecutionRecord.fromJUnitReport(testSuite)))
        );
        return createImportExecutionResponse(testRuns);
    }

    @Operation(summary = "Import JUnit test result (multipart)", responses = @ApiResponse(
            responseCode = "200",
            description = "JUnit test result successfully imported",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ImportExecutionResponse.class)
            )
    ))
    @POST
    @Path("/junit/multipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public ImportExecutionResponse importExecutionJunitMultipart(
            @FormDataParam("info") FormDataBodyPart info,
            @FormDataParam("file") FormDataBodyPart file) {
        info.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        file.setMediaType(MediaType.APPLICATION_XML_TYPE);

        ExecutionInfo executionInfo = info.getValueAs(ExecutionInfo.class);
        return importJUnitTests(executionInfo, file);
    }

    private ImportExecutionResponse createImportExecutionResponse(List<ITestRun> testRuns) {
        ITestRun testRun = testRuns.get(0);
        String testRunURL = getBaseUri() + "/polarion/#/project/" + testRun.getProjectId() + "/testrun?id=" + testRun.getId();
        TestExecIssue testExecIssue = new TestExecIssue(testRun.getProjectId() + "/" + testRun.getId(), testRun.getId(), testRunURL);
        return new ImportExecutionResponse(testExecIssue);
    }

    private URI getBaseUri() {
        try {
            return new URI(
                    uriInfo.getBaseUri().getScheme(),
                    null,
                    uriInfo.getBaseUri().getHost(),
                    uriInfo.getBaseUri().getPort(),
                    null,
                    null,
                    null);
        } catch (URISyntaxException e) {
            logger.error(e);
            return null;
        }
    }

    @VisibleForTesting
    public XrayImportExecutionResultsController withUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
        return this;
    }

}
