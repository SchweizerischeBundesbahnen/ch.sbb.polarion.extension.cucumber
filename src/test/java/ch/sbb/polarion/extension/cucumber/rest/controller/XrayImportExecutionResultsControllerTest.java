package ch.sbb.polarion.extension.cucumber.rest.controller;

import ch.sbb.polarion.extension.cucumber.helper.PolarionTestRun;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ExecutionInfo;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.ImportExecutionResponse;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.TestExecIssue;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.junit.TestCase;
import ch.sbb.polarion.extension.cucumber.rest.model.execution.junit.TestSuite;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.shared.api.transaction.RunnableInWriteTransaction;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.shared.api.transaction.WriteTransaction;
import com.polarion.alm.tracker.ITestManagementService;
import com.polarion.alm.tracker.model.ITestRun;
import lombok.SneakyThrows;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class XrayImportExecutionResultsControllerTest {

    @Mock
    private ITestManagementService testManagementService;

    @Test
    @SneakyThrows
    void testImportExecutionCucumberMultipart() {
        try (MockedStatic<TransactionalExecutor> mockedExecutor = mockStatic(TransactionalExecutor.class);
             MockedStatic<PolarionTestRun> polarionTestRun = mockStatic(PolarionTestRun.class)) {

            PolarionService polarionService = mockCommonThings(mockedExecutor, polarionTestRun);

            FormDataBodyPart infoBodyPart = mock(FormDataBodyPart.class);
            when(infoBodyPart.getValueAs(any())).thenReturn(new ExecutionInfo());

            FormDataBodyPart fileBodyPart = mock(FormDataBodyPart.class);

            TestSuite testSuite = new TestSuite();
            testSuite.setTestCases(Arrays.asList(
                    new TestCase(null, new ArrayList<>(), new ArrayList<>(), null, null, "tc1", 0, 7d, null, null),
                    new TestCase(null, new ArrayList<>(), new ArrayList<>(), null, null, "tc2", 0, 11d, null, null)));
            when(fileBodyPart.getValueAs(any())).thenReturn(testSuite);
            UriInfo uriInfo = mock(UriInfo.class);
            when(uriInfo.getBaseUri()).thenReturn(URI.create("https://somedomain.com:4242"));
            ImportExecutionResponse response = new XrayImportExecutionResultsController(polarionService, testManagementService).withUriInfo(uriInfo).importExecutionJunitMultipart(infoBodyPart, fileBodyPart);
            assertThat(response).isNotNull();
            TestExecIssue issue = response.getTestExecIssue();
            assertThat(issue).isNotNull();
            assertEquals("testRunProjId/testRunId", issue.getId());
            assertEquals("testRunId", issue.getKey());
            assertEquals("https://somedomain.com:4242/polarion/#/project/testRunProjId/testrun?id=testRunId", issue.getSelf());
        }
    }

    @Test
    @SneakyThrows
    void testImportExecutionJunit() {
        try (MockedStatic<TransactionalExecutor> mockedExecutor = mockStatic(TransactionalExecutor.class);
             MockedStatic<PolarionTestRun> polarionTestRun = mockStatic(PolarionTestRun.class)) {

            PolarionService polarionService = mockCommonThings(mockedExecutor, polarionTestRun);

            FormDataBodyPart fileBodyPart = mock(FormDataBodyPart.class);

            TestSuite testSuite = new TestSuite();
            testSuite.setTestCases(Arrays.asList(
                    new TestCase(null, new ArrayList<>(), new ArrayList<>(), null, null, "tc1", 0, 7d, null, null),
                    new TestCase(null, new ArrayList<>(), new ArrayList<>(), null, null, "tc2", 0, 11d, null, null)));
            when(fileBodyPart.getValueAs(any())).thenReturn(testSuite);

            UriInfo uriInfo = mock(UriInfo.class);
            when(uriInfo.getBaseUri()).thenReturn(URI.create("https://somedomain.com:4242"));
            ImportExecutionResponse response = new XrayImportExecutionResultsController(polarionService, testManagementService).withUriInfo(uriInfo).importExecutionJunit(
                    "projKey", "testExecLKey", "testPlanKey", "envs", "rev", "fixVersion", fileBodyPart);
            assertThat(response).isNotNull();

        }
    }

    @Test
    @SneakyThrows
    void testImportExecutionJunitMultipart() {
        try (MockedStatic<TransactionalExecutor> mockedExecutor = mockStatic(TransactionalExecutor.class);
             MockedStatic<PolarionTestRun> polarionTestRun = mockStatic(PolarionTestRun.class)) {

            PolarionService polarionService = mockCommonThings(mockedExecutor, polarionTestRun);

            FormDataBodyPart infoBodyPart = mock(FormDataBodyPart.class);
            when(infoBodyPart.getValueAs(any())).thenReturn(new ExecutionInfo());

            FormDataBodyPart fileBodyPart = mock(FormDataBodyPart.class);

            TestSuite testSuite = new TestSuite();
            testSuite.setTestCases(Arrays.asList(
                    new TestCase(null, new ArrayList<>(), new ArrayList<>(), null, null, "tc1", 0, 7d, null, null),
                    new TestCase(null, new ArrayList<>(), new ArrayList<>(), null, null, "tc2", 0, 11d, null, null)));
            when(fileBodyPart.getValueAs(any())).thenReturn(testSuite);

            UriInfo uriInfo = mock(UriInfo.class);
            when(uriInfo.getBaseUri()).thenReturn(URI.create("https://somedomain.com:4242"));
            ImportExecutionResponse response = new XrayImportExecutionResultsController(polarionService, testManagementService).withUriInfo(uriInfo).importExecutionJunitMultipart(
                    infoBodyPart, fileBodyPart);
            assertThat(response).isNotNull();

        }
    }

    private PolarionService mockCommonThings(MockedStatic<TransactionalExecutor> mockedExecutor, MockedStatic<PolarionTestRun> polarionTestRun) {
        mockedExecutor.when(() -> TransactionalExecutor.executeInWriteTransaction(any(RunnableInWriteTransaction.class)))
                .thenAnswer(invocation -> {
                    RunnableInWriteTransaction transaction = invocation.getArgument(0);
                    return transaction.run(mock(WriteTransaction.class));
                });

        PolarionService polarionService = mock(PolarionService.class);
        when(polarionService.callPrivileged(any(Callable.class))).thenAnswer(invocation -> {
            Callable callable = invocation.getArgument(0);
            return callable.call();
        });

        ITestRun testRun = mock(ITestRun.class);
        when(testRun.getProjectId()).thenReturn("testRunProjId");
        when(testRun.getId()).thenReturn("testRunId");
        polarionTestRun.when(() -> PolarionTestRun.createTestRuns(any(), any(), any(), any())).thenReturn(Collections.singletonList(testRun));

        return polarionService;
    }

}
