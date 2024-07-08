package ch.sbb.polarion.extension.cucumber.helper;

import ch.sbb.polarion.extension.cucumber.rest.model.Feature;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.IAttachment;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.logging.Logger;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class PolarionWorkItem {

    private static final Logger logger = Logger.getLogger(PolarionWorkItem.class);

    @NotNull
    public static IAttachment createOrUpdateFeature(@NotNull ITrackerService trackerService, @NotNull Feature feature) {
        IWorkItem workItem = trackerService.findWorkItem(feature.getProjectId(), feature.getWorkItemId());

        if (workItem.isUnresolvable()) {
            throw new NotFoundException(String.format("WorkItem '%s/%s' not found!", feature.getProjectId(), feature.getWorkItemId()));
        }

        IAttachment attachment = workItem.getAttachmentByFileName(feature.getFilename());

        if (attachment == null) {
            attachment = workItem.createAttachment(feature.getFilename(), feature.getTitle(), null);
        }

        attachment.setValue("title", feature.getTitle());

        try (InputStream inputStream = new ByteArrayInputStream(feature.getContent().getBytes(StandardCharsets.UTF_8))) {
            attachment.setDataStream(inputStream);
            attachment.save();
            return attachment;
        } catch (IOException e) {
            logger.error(e);
            throw new InternalServerErrorException(e);
        }
    }

    @NotNull
    public static Feature getFeature(@NotNull ITrackerService trackerService, @NotNull String projectId, @NotNull String workItemId) {
        IWorkItem workItem = trackerService.findWorkItem(projectId, workItemId);

        if (workItem.isUnresolvable()) {
            throw new NotFoundException(String.format("WorkItem '%s/%s' not found!", projectId, workItemId));
        }

        String filename = workItemId + ".feature";
        IAttachment attachment = workItem.getAttachmentByFileName(filename);

        if (attachment == null) {
            throw new NotFoundException(String.format("WorkItem '%s/%s' doesn't have attachment with filename '%s'!", projectId, workItemId, filename));
        }
        String title = attachment.getTitle();

        try (InputStream inputStream = attachment.getDataStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new Feature(projectId, workItemId, title, filename, content);
        } catch (IOException e) {
            logger.error(e);
            throw new InternalServerErrorException(e);
        }
    }

    @NotNull
    public static Feature getFeature(@NotNull ITrackerService trackerService, @NotNull String fullWorkItemId) {
        String[] tokens = fullWorkItemId.split("/");
        if (tokens.length != 2) {
            throw new BadRequestException("Provided workitem id is not in 'projectId/workItemId' format: " + fullWorkItemId);
        }
        String projectId = tokens[0];
        String workItemId = tokens[1];

        return getFeature(trackerService, projectId, workItemId);
    }
}
