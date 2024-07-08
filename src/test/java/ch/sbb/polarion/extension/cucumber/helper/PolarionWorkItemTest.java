package ch.sbb.polarion.extension.cucumber.helper;

import ch.sbb.polarion.extension.cucumber.rest.model.Feature;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.IAttachment;
import com.polarion.alm.tracker.model.IWorkItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolarionWorkItemTest {

    String testProjectId = "TEST_PROJECT_ID";
    String testWi = "TEST_WI";
    String title = "path/to/the/feature/AAA.feature";
    String filename = "TEST_WI.feature";
    String fileContent = "content";
    Feature created = new Feature(testProjectId, testWi, title, filename, fileContent);
    @Mock
    private ITrackerService trackerService;
    @Mock
    private IWorkItem workItem;
    @Mock
    private IAttachment attachment;

    @Test
    void getFeatureWithUnresolvableWorkItem() {
        when(trackerService.findWorkItem(testProjectId, testWi)).thenReturn(workItem);
        when(workItem.isUnresolvable()).thenReturn(true);

        assertThatThrownBy(() ->
                PolarionWorkItem.getFeature(trackerService, testProjectId, testWi))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.format("WorkItem '%s/%s' not found!", testProjectId, testWi));
    }

    @Test
    void getFeatureWithoutAttachment() {
        when(trackerService.findWorkItem(testProjectId, testWi)).thenReturn(workItem);
        when(workItem.isUnresolvable()).thenReturn(false);
        when(workItem.getAttachmentByFileName(filename)).thenReturn(null);

        Exception thrown = assertThrows(Exception.class, () ->
                PolarionWorkItem.getFeature(trackerService, testProjectId, testWi)
        );

        assertEquals(
                String.format("WorkItem '%s/%s' doesn't have attachment with filename '%s'!", testProjectId, testWi, filename),
                thrown.getMessage()
        );
    }

    @Test
    void getFeatureWithoutErrors() {
        when(trackerService.findWorkItem(testProjectId, testWi)).thenReturn(workItem);
        when(workItem.isUnresolvable()).thenReturn(false);
        when(workItem.getAttachmentByFileName(filename)).thenReturn(attachment);
        when(attachment.getTitle()).thenReturn(title);
        when(attachment.getDataStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)));

        Feature feature = PolarionWorkItem.getFeature(trackerService, testProjectId, testWi);
        Feature expected = new Feature(testProjectId, testWi, title, filename, fileContent);

        assertThat(feature.getWorkItemId()).isEqualTo(expected.getWorkItemId());
        assertThat(feature.getContent()).isEqualTo(expected.getContent());
        assertThat(feature.getProjectId()).isEqualTo(expected.getProjectId());
        assertThat(feature.getFilename()).isEqualTo(expected.getFilename());
        assertThat(feature.getTitle()).isEqualTo(expected.getTitle());
    }

    @Test
    void getFeatureByFullWorkItemId() {
        BadRequestException thrown = assertThrows(BadRequestException.class, () ->
                PolarionWorkItem.getFeature(trackerService, "noSlashId")
        );
        assertEquals("Provided workitem id is not in 'projectId/workItemId' format: noSlashId", thrown.getMessage());

        try (MockedStatic<PolarionWorkItem> mockedPolarionWorkItem = mockStatic(PolarionWorkItem.class)) {
            Feature feature = new Feature();
            mockedPolarionWorkItem.when(() -> PolarionWorkItem.getFeature(any(ITrackerService.class), anyString(), anyString()))
                    .thenAnswer(invocation -> feature);
            mockedPolarionWorkItem.when(() -> PolarionWorkItem.getFeature(any(ITrackerService.class), anyString())).thenCallRealMethod();
            Feature resultFeature = PolarionWorkItem.getFeature(trackerService, "proj/wiId");
            assertEquals(feature, resultFeature);
        }
    }

    @Test
    void createOrUpdateFeatureWihUnresolvableWorkItem() {
        Feature created = new Feature(testProjectId, testWi, title, filename, fileContent);

        when(trackerService.findWorkItem(testProjectId, testWi)).thenReturn(workItem);
        when(workItem.isUnresolvable()).thenReturn(true);

        assertThatThrownBy(() ->
                PolarionWorkItem.createOrUpdateFeature(trackerService, created))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.format("WorkItem '%s/%s' not found!", testProjectId, testWi));
    }

    @Test
    void createOrUpdateFeatureWithoutAttachment() {
        when(trackerService.findWorkItem(testProjectId, testWi)).thenReturn(workItem);
        when(workItem.isUnresolvable()).thenReturn(false);
        when(workItem.getAttachmentByFileName(filename)).thenReturn(null);
        when(workItem.createAttachment(created.getFilename(), title, null)).thenReturn(attachment);

        PolarionWorkItem.createOrUpdateFeature(trackerService, created);
        verify(attachment, times(1)).save();
    }

    @Test
    void createOrUpdateFeature() {
        when(trackerService.findWorkItem(testProjectId, testWi)).thenReturn(workItem);
        when(workItem.isUnresolvable()).thenReturn(false);
        when(workItem.getAttachmentByFileName("TEST_WI.feature")).thenReturn(attachment);

        PolarionWorkItem.createOrUpdateFeature(trackerService, created);
        verify(attachment, times(1)).save();
    }
}
