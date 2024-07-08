package ch.sbb.polarion.extension.cucumber.helper;

import ch.sbb.polarion.extension.cucumber.exception.TestRunCreationException;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionInfo;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionRecord;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.Comment;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.CucumberExecutionResult;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.Element;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.Result;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.Step;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.cucumber.Tag;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.junit.TestCase;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.junit.TestSuite;
import ch.sbb.polarion.extension.cucumber.rest.model.fields.FieldDefinition;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.projects.IProjectService;
import com.polarion.alm.projects.model.IProject;
import com.polarion.alm.tracker.ITestManagementService;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.internal.model.StatusOpt;
import com.polarion.alm.tracker.model.ITestRecord;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.alm.tracker.model.ITypeOpt;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.types.Text;
import com.polarion.platform.persistence.IDataService;
import com.polarion.platform.persistence.spi.EnumOption;
import com.polarion.platform.persistence.spi.PObjectList;
import com.polarion.subterra.base.data.model.ICustomField;
import com.polarion.subterra.base.data.model.internal.PrimitiveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static ch.sbb.polarion.extension.cucumber.helper.PolarionTestRun.TYPE_REQUIRES_SIGNATURE;
import static ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionRecord.NANOSECONDS_IN_SECOND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolarionTestRunTest {
    public static final String TEST_EXECUTION = "TEST_EXECUTION";
    public static final String TEST_PROJECT_ID = "PRJ";
    public static final String TEST_ISSUE_1 = "PRJ-1";
    public static final String TEST_ISSUE_2 = "PRJ-2";
    public static final String TEST_CASE_1 = "TestCase1";
    public static final String TEST_CASE_2 = "TestCase2";
    public static final String TEST_CASE_3 = "TestCase3";
    public static final String TEST_PROJECT_KEY = "TEST_PROJECT_KEY";
    public static final String TEST_TITLE = "TEST_TITLE";
    public static final String TEST_TEMPLATE = "TEST_TEMPLATE";
    private static final String TEST_CUSTOM_FIELD_ID = "testCustomFieldId";

    @Mock
    private ITrackerService trackerService;

    @Mock
    private IProjectService projectService;

    @Mock
    private ITestManagementService testManagementService;

    @Mock
    private IProject project;

    @Mock
    private ITestRun testRun;

    @Mock
    private IWorkItem workItem1;
    @Mock
    private IWorkItem workItem2;

    @Mock
    private ITestRecord testRecord1;
    @Mock
    private ITestRecord testRecord2;

    private PolarionService polarionService;

    @BeforeEach
    public void setup() {
        polarionService = mock(PolarionService.class);
        lenient().when(polarionService.getTrackerService()).thenReturn(trackerService);
    }

    @Test
    void shouldCreatePassedTestRun() {
        // Arrange
        prepareMockedData(TEST_TEMPLATE, true);
        List<ITestRun> testRuns = PolarionTestRun.createTestRuns(polarionService, testManagementService, prepareCucumberExecutionInfo(), buildResults(null));

        // Assert
        assertThat(testRuns)
                .hasSize(1)
                .contains(testRun);
        verify(testRecord1).setTestCase(workItem1);
        verify(testRecord2).setTestCase(workItem2);
        Stream.of(testRecord1, testRecord2)
                .forEach(tr -> {
                    verify(tr).setDuration((float) 6 / NANOSECONDS_IN_SECOND);
                    verify(tr).setExecuted(any(Date.class));
                    verify(tr).setResult("passed");
                });
        verify(testRun).setTitle(TEST_TITLE);
        EnumOption enumOption = new EnumOption("testing/testrun-status", "passed", "Passed", 3, false);
        verify(testRun).setStatus(new StatusOpt(enumOption));
    }

    @Test
    void shouldGetTestRunUsingDifferentMethods() {
        // Arrange
        prepareMockedData(TEST_TEMPLATE, true);
        //lenient().when(trackerService.findWorkItem(anyString(), anyString())).thenReturn(null);
        when(trackerService.queryWorkItems(anyString(), anyString())).thenReturn(new PObjectList(mock(IDataService.class), List.of()));
        List<ExecutionRecord> executionResults = buildJUnitResults();
        List<ITestRun> testRuns = PolarionTestRun.createTestRuns(polarionService, testManagementService, prepareCucumberExecutionInfo(), executionResults);
        assertThat(testRuns)
                .hasSize(1)
                .contains(testRun);
        verify(testRun, times(0)).addRecord(); // no test run found using queryWorkItems() -> 0 test records added

        when(trackerService.queryWorkItems(anyString(), anyString())).thenReturn(new PObjectList(mock(IDataService.class), List.of()));

        when(trackerService.queryWorkItems(String.format("project.id:%s AND type:testcase AND title:\"%s\"",
                TEST_PROJECT_ID, TEST_CASE_1), "id")).thenReturn(new PObjectList(mock(IDataService.class), List.of(workItem1)));
        when(trackerService.queryWorkItems(String.format("project.id:%s AND type:testcase AND title:\"%s\"",
                TEST_PROJECT_ID, TEST_CASE_2), "id")).thenReturn(new PObjectList(mock(IDataService.class), List.of(workItem2)));

        testRuns = PolarionTestRun.createTestRuns(polarionService, testManagementService, prepareCucumberExecutionInfo(), executionResults);
        assertThat(testRuns)
                .hasSize(1)
                .contains(testRun);

        verify(testRun, times(2)).addRecord(); // no test run found using queryWorkItems() -> 0 test records added
    }

    @Test
    void shouldCreateFailedTestRun() {
        // Arrange
        prepareMockedData(TEST_TEMPLATE, false);

        // Act
        List<ITestRun> testRuns = PolarionTestRun.createTestRuns(polarionService, testManagementService, prepareCucumberExecutionInfo(), buildResults("errMsg1"));

        // Assert
        assertThat(testRuns)
                .hasSize(1)
                .contains(testRun);
        verify(testRecord1).setTestCase(workItem1);
        verify(testRecord2).setTestCase(workItem2);
        Stream.of(testRecord1, testRecord2)
                .forEach(tr -> {
                    verify(tr).setDuration((float) 6 / NANOSECONDS_IN_SECOND);
                    verify(tr).setExecuted(any(Date.class));
                    verify(tr).setResult("failed");
                    verify(tr).setComment(Text.plain("errMsg1"));
                });
        verify(testRun).setTitle(TEST_TITLE);
        EnumOption enumOption = new EnumOption("testing/testrun-status", "failed", "Failed", 3, false);
        verify(testRun).setStatus(new StatusOpt(enumOption));
        verify(testRun).setStatus(new StatusOpt(enumOption));
    }

    @Test
    void shouldFailCreateTestRunBadTemplateId() {
        // Arrange
        prepareMockedData("UNKNOWN_TEST_TEMPLATE", true);

        // Act
        ExecutionInfo executionInfo = prepareCucumberExecutionInfo();
        List<ExecutionRecord> executionResults = buildResults(null);
        assertThatThrownBy(() -> PolarionTestRun.createTestRuns(polarionService, testManagementService, executionInfo, executionResults))
                .isInstanceOf(TestRunCreationException.class);
    }

    @Test
    void shouldFailCreateTestRunBadTemplateType() {
        // Arrange
        prepareMockedData(TEST_TEMPLATE, true);

        //emulate type with 'Requires Signature for Test Case execution' turned on
        ITestRun template = mock(ITestRun.class);
        when(template.getId()).thenReturn(TEST_TEMPLATE);
        ITypeOpt type = mock(ITypeOpt.class);
        when(type.getProperty(TYPE_REQUIRES_SIGNATURE)).thenReturn("true");
        when(template.getType()).thenReturn(type);
        when(testManagementService.searchTestRunTemplates(eq("project.id:" + TEST_PROJECT_ID), any(), anyInt()))
                .thenReturn(List.of(template));

        // Act
        ExecutionInfo executionInfo = prepareCucumberExecutionInfo();
        List<ExecutionRecord> executionResults = buildResults("errMsg1");
        assertThatThrownBy(() -> PolarionTestRun.createTestRuns(polarionService, testManagementService, executionInfo, executionResults))
                .isInstanceOf(TestRunCreationException.class);
    }

    private List<ExecutionRecord> buildResults(String secondStepErrorMessage) {
        return ExecutionRecord.fromCucumberExecutions(List.of(prepareCucumberExecutionResult(secondStepErrorMessage)));
    }

    private List<ExecutionRecord> buildJUnitResults() {
        TestSuite testSuite = new TestSuite();
        testSuite.setTestCases(Arrays.asList(
                new TestCase(null, null, null, null, null, TEST_CASE_1, 1, 1d, null, null),
                new TestCase(null, null, null, null, null, TEST_CASE_2, 1, 2d, null, null),
                new TestCase(null, null, null, null, null, TEST_CASE_3, 1, 3d, null, null)
        ));
        return ExecutionRecord.fromJUnitReport(testSuite);
    }

    private void prepareMockedData(String testRunTemplateId, boolean passed) {
        when(trackerService.getProjectsService()).thenReturn(projectService);
        when(projectService.getProject(TEST_PROJECT_KEY)).thenReturn(project);
        when(project.getId()).thenReturn(TEST_PROJECT_ID);
        lenient().when(testManagementService.createTestRun(eq(TEST_PROJECT_ID), startsWith(TEST_PROJECT_ID), anyString()))
                .thenReturn(testRun);
        ITestRun template = mock(ITestRun.class);
        lenient().when(template.getId()).thenReturn(testRunTemplateId);
        lenient().when(testManagementService.searchTestRunTemplates(eq("project.id:" + TEST_PROJECT_ID), any(), anyInt()))
                .thenReturn(List.of(template));

        lenient().when(testRun.getProjectId()).thenReturn(TEST_PROJECT_ID);
        lenient().when(trackerService.findWorkItem(TEST_PROJECT_ID, TEST_ISSUE_1)).thenReturn(workItem1);
        lenient().when(trackerService.findWorkItem(TEST_PROJECT_ID, TEST_ISSUE_2)).thenReturn(workItem2);
        lenient().when(testRun.addRecord()).thenReturn(testRecord1, testRecord2);
        lenient().when(testRun.getAllRecords()).thenReturn(List.of(testRecord1, testRecord2));

        lenient().when(testRun.getCustomFieldsList()).thenReturn(List.of(TEST_CUSTOM_FIELD_ID));
        ICustomField customField = mock(ICustomField.class);
        lenient().when(customField.getType()).thenReturn(new PrimitiveType(String.class.getName(), null));
        lenient().when(testRun.getCustomFieldPrototype(any())).thenReturn(customField);

        lenient().when(testRecord1.getResult()).thenReturn(new EnumOption("testing/testrun-status", passed ? "passed" : "failed", passed ? "Passed" : "Failed", 3, false));
        lenient().when(testRecord2.getResult()).thenReturn(new EnumOption("testing/testrun-status", passed ? "passed" : "failed", passed ? "Passed" : "Failed", 3, false));
    }

    private ExecutionInfo prepareCucumberExecutionInfo() {
        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.getFields().put(ExecutionInfo.PROJECT_KEY, Collections.singletonMap("key", TEST_PROJECT_KEY));
        executionInfo.getFields().put(FieldDefinition.TITLE.getId(), TEST_TITLE);
        executionInfo.getFields().put(FieldDefinition.TEST_RUN_TEMPLATE_ID.getId(), TEST_TEMPLATE);
        executionInfo.getFields().put(TEST_CUSTOM_FIELD_ID, "customFieldValue");
        return executionInfo;
    }

    private CucumberExecutionResult prepareCucumberExecutionResult(String secondStepErrorMessage) {
        String resultName = TEST_EXECUTION;
        String issueId1 = TEST_ISSUE_1;
        String issueId2 = TEST_ISSUE_2;
        CucumberExecutionResult result = new CucumberExecutionResult();
        result.setName(resultName);
        Tag tag = new Tag();
        tag.setName(resultName);
        result.setTags(List.of(tag));

        Element executionElement1 = prepareExecutionElement(issueId1, "background", null);
        Element executionElement2 = prepareExecutionElement(issueId1, "scenario", secondStepErrorMessage);
        Element executionElement3 = prepareExecutionElement(issueId2, "background", null);
        Element executionElement4 = prepareExecutionElement(issueId2, "scenario", secondStepErrorMessage);

        result.setElements(List.of(
                executionElement1,
                executionElement2,
                executionElement3,
                executionElement4));
        return result;
    }

    private Element prepareExecutionElement(String issueId, String type, String secondStepErrorMessage) {
        Element executionElement = new Element();
        Tag elementTag = new Tag();
        elementTag.setName("@" + issueId);
        executionElement.setTags(List.of(elementTag));
        Comment comment = new Comment();
        comment.setValue("TEST_COMMENT");
        executionElement.setComments(List.of(comment));
        Step step1 = prepareCucumberStep("testStep1", 1L, "passed", null);
        Step step2 = prepareCucumberStep("testStep2", 2L, secondStepErrorMessage != null ? "failed" : "passed", secondStepErrorMessage);
        executionElement.setSteps(List.of(step1, step2));
        executionElement.setType(type);
        return executionElement;
    }

    private Step prepareCucumberStep(String stepName, long duration, String status, String errorMessage) {
        Step step = new Step();
        step.setName(stepName);
        step.setResult(new Result(status, duration, errorMessage));
        return step;
    }

}
