package ch.sbb.polarion.extension.cucumber.helper;

import ch.sbb.polarion.extension.cucumber.exception.TestRunCreationException;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionInfo;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionRecord;
import ch.sbb.polarion.extension.cucumber.rest.model.fields.FieldType;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.projects.model.IProject;
import com.polarion.alm.tracker.ITestManagementService;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.internal.model.StatusOpt;
import com.polarion.alm.tracker.model.IStatusOpt;
import com.polarion.alm.tracker.model.ITestRecord;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.alm.tracker.model.ITypeOpt;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.StringUtils;
import com.polarion.core.util.types.Text;
import com.polarion.platform.persistence.model.IPObjectList;
import com.polarion.platform.persistence.spi.EnumOption;
import com.polarion.subterra.base.data.model.ICustomField;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.BadRequestException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@UtilityClass
public class PolarionTestRun {

    public static final String PASSED_STATUS = "passed";
    public static final int PASSED_STATUS_NUMBER = 3;
    public static final String FAILED_STATUS = "failed";
    public static final int FAILED_STATUS_NUMBER = 2;
    public static final String TYPE_REQUIRES_SIGNATURE = "requiresSignatureForTestCaseExecution";
    private static final String TIMESTAMP_PATTERN = "yyyyMMdd-HHmmssSSS";

    public static List<ITestRun> createTestRuns(
            @NotNull PolarionService polarionService,
            @NotNull ITestManagementService testManagementService,
            @NotNull ExecutionInfo executionInfo,
            @NotNull List<ExecutionRecord> executionResults) {

        String projectKey = getProjectKey(executionInfo);
        if (projectKey == null) {
            throw new TestRunCreationException("Project key not specified, skipping test run creation");
        }

        ITrackerService trackerService = polarionService.getTrackerService();
        IProject project = trackerService.getProjectsService().getProject(projectKey);
        if (project == null) {
            throw new TestRunCreationException(String.format("Project '%s' not found, skipping test run creation", projectKey));
        }

        ITestRun testRun = createTestRun(testManagementService, polarionService, project, executionInfo);
        for (ExecutionRecord executionRecord : executionResults) {
            addTestRecord(trackerService, testRun, executionRecord);
        }

        testRun.setStatus(getTestRunStatus(testRun));
        testRun.save();

        return Collections.singletonList(testRun);
    }

    private static String getProjectKey(ExecutionInfo info) {
        return info.getProject().getId() != null ? info.getProject().getId() : info.getProject().getKey();
    }

    private static @NotNull ITestRun createTestRun(
            @NotNull ITestManagementService testManagementService,
            @NotNull PolarionService polarionService,
            @NotNull IProject project,
            @NotNull ExecutionInfo executionInfo) {

        String testRunId = String.format("%s_%s", project.getId(), new SimpleDateFormat(TIMESTAMP_PATTERN).format(new Date()));
        String testRunTemplateId = StringUtils.isEmpty(executionInfo.getTestRunTemplateId()) ? "xUnit Build Test" : executionInfo.getTestRunTemplateId();

        ITestRun template = testManagementService.searchTestRunTemplates("project.id:" + project.getId(), null, -1)
                .stream().filter(t -> t.getId().equals(testRunTemplateId)).findFirst().orElse(null);
        if (template == null) {
            throw new TestRunCreationException(String.format("Test run template '%s' not found in project '%s', skipping test run creation", testRunTemplateId, project.getId()));
        } else {
            ITypeOpt type = template.getType();
            if (type != null && Boolean.TRUE.equals(Boolean.parseBoolean(type.getProperty(TYPE_REQUIRES_SIGNATURE)))) {
                throw new TestRunCreationException(String.format("Test run template '%s' of type '%s' requires signature for Test Case execution, skipping test run creation", testRunTemplateId, type.getName()));
            }
        }

        ITestRun testRun = testManagementService.createTestRun(project.getId(), testRunId, testRunTemplateId);
        testRun.setTitle(executionInfo.getTitle());
        if (executionInfo.getDescription() != null) {
            testRun.setCustomField("description", Text.html(
                    executionInfo.getDescription().replaceAll(System.lineSeparator(), "<br>")));
        }
        fillCustomFields(polarionService, testRun, executionInfo);

        return testRun;
    }

    private static void addTestRecord(@NotNull ITrackerService trackerService,
                                      @NotNull ITestRun testRun, @NotNull ExecutionRecord executionRecord) {

        IWorkItem testCase = findTestCase(trackerService, testRun, executionRecord);
        if (testCase != null) {
            ITestRecord testRecord = testRun.addRecord();
            testRecord.setTestCase(testCase);
            testRecord.setDuration(executionRecord.getDuration());
            testRecord.setExecuted(executionRecord.getDate());
            testRecord.setResult(executionRecord.getStatus());
            testRecord.setComment(executionRecord.getComment());
        }
    }

    private static @Nullable IWorkItem findTestCase(@NotNull ITrackerService trackerService,
                                                    @NotNull ITestRun testRun, @NotNull ExecutionRecord executionRecord) {

        if (!executionRecord.getTestCaseIds().isEmpty()) {
            for (String testCaseId : executionRecord.getTestCaseIds()) {
                IWorkItem testCase = trackerService.findWorkItem(testRun.getProjectId(), testCaseId);
                if (testCase != null) {
                    return testCase;
                }
            }
        }
        if (!StringUtils.isEmpty(executionRecord.getTestCaseTitle())) {

            @SuppressWarnings("unchecked")
            IPObjectList<IWorkItem> list = trackerService.queryWorkItems(
                    String.format("project.id:%s AND type:testcase AND title:\"%s\"",
                            testRun.getProjectId(), executionRecord.getTestCaseTitle()), "id");
            if (!list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    private static @Nullable IStatusOpt getTestRunStatus(@NotNull ITestRun testRun) {
        if (!testRun.getAllRecords().isEmpty()) {
            String status = testRun.getAllRecords().stream()
                    .map(testRecord -> testRecord.getResult().getId())
                    .reduce(PASSED_STATUS, (a, b) -> a.equals(PASSED_STATUS) && b.equals(PASSED_STATUS) ? PASSED_STATUS : FAILED_STATUS);
            if (status.equals(PASSED_STATUS)) {
                return new StatusOpt(new EnumOption("testing/testrun-status", PASSED_STATUS, "Passed", PASSED_STATUS_NUMBER, false));
            } else {
                return new StatusOpt(new EnumOption("testing/testrun-status", FAILED_STATUS, "Failed", FAILED_STATUS_NUMBER, false));
            }
        }
        return null;
    }

    private static void fillCustomFields(PolarionService polarionService, ITestRun testRun, ExecutionInfo executionInfo) {
        for (String customFieldId : executionInfo.getCustomFieldIds()) {
            if (!testRun.getCustomFieldsList().contains(customFieldId)) {
                throw new BadRequestException(String.format("Unknown custom field %s", customFieldId));
            }
            ICustomField fieldPrototype = testRun.getCustomFieldPrototype(customFieldId);
            FieldType fieldType = FieldType.fromIType(fieldPrototype.getType());
            if (fieldType == null) {
                throw new BadRequestException(String.format("Unsupported type for custom field %s", customFieldId));
            }
            Object valueToSet = fieldType.getConverter().convert(
                    polarionService.getTrackerService(), testRun, fieldPrototype, executionInfo.getFields().get(customFieldId));
            polarionService.setFieldValue(testRun, customFieldId, valueToSet);
        }
    }
}
