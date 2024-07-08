package ch.sbb.polarion.extension.cucumber.rest.controller;

import ch.sbb.polarion.extension.cucumber.helper.PolarionWorkItem;
import ch.sbb.polarion.extension.cucumber.rest.model.Feature;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.shared.api.transaction.ReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.RunnableInReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.platform.persistence.model.IPObjectList;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class XrayExportCucumberTestsControllerTest {

    @Test
    @SneakyThrows
    void testExportTest() {

        try (MockedStatic<TransactionalExecutor> mockedExecutor = mockStatic(TransactionalExecutor.class);
             MockedStatic<PolarionWorkItem> mockedPolarionWorkItem = mockStatic(PolarionWorkItem.class)) {

            PolarionService polarionService = mock(PolarionService.class);

            mockedExecutor.when(() -> TransactionalExecutor.executeSafelyInReadOnlyTransaction(any(RunnableInReadOnlyTransaction.class)))
                    .thenAnswer(invocation -> {
                        RunnableInReadOnlyTransaction transaction = invocation.getArgument(0);
                        return transaction.run(mock(ReadOnlyTransaction.class));
                    });

            Feature testFeatureOne = new Feature("projId1", "wiId1", "title1", "filename1", "content1");
            mockedPolarionWorkItem.when(() -> PolarionWorkItem.getFeature(any(), any())).thenReturn(testFeatureOne);
            Feature testFeatureTwo = new Feature("projId2", "wiId2", "title2", "filename2", "content2");
            mockedPolarionWorkItem.when(() -> PolarionWorkItem.getFeature(any(), any(), any())).thenReturn(testFeatureTwo);

            when(polarionService.callPrivileged(any(Callable.class))).thenAnswer(invocation -> {
                Callable callable = invocation.getArgument(0);
                return callable.call();
            });

            ITrackerService trackerService = mock(ITrackerService.class);
            IPObjectList wiList = mock(IPObjectList.class);
            IWorkItem workItem = mock(IWorkItem.class);
            when(wiList.iterator()).thenReturn(Collections.singletonList(workItem).iterator());
            doCallRealMethod().when(wiList).forEach(any());
            when(trackerService.queryWorkItems(any(), any())).thenReturn(wiList);
            when(polarionService.getTrackerService()).thenReturn(trackerService);

            try (Response response = new XrayExportCucumberTestsController(polarionService).exportTest("keys", "query", true)) {
                assertThat(response).isNotNull();
                assertTrue(response.getEntity() instanceof StreamingOutput);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ((StreamingOutput) response.getEntity()).write(outputStream);
                ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
                ZipEntry firstEntry = zipInputStream.getNextEntry();
                ZipEntry secondEntry = zipInputStream.getNextEntry();
                assertThat(firstEntry).isNotNull();
                assertEquals("title1", firstEntry.getName());
                assertThat(secondEntry).isNotNull();
                assertEquals("title2", secondEntry.getName());
            }

            try (Response response = new XrayExportCucumberTestsController(polarionService).exportTest("keys", "query", false)) {
                assertThat(response).isNotNull();
                assertTrue(response.getEntity() instanceof StreamingOutput);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ((StreamingOutput) response.getEntity()).write(outputStream);
                assertEquals("content1", outputStream.toString(StandardCharsets.UTF_8));
            }
        }
    }

}
