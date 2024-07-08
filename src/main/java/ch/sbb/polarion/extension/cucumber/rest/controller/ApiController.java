package ch.sbb.polarion.extension.cucumber.rest.controller;

import ch.sbb.polarion.extension.cucumber.rest.model.Feature;
import ch.sbb.polarion.extension.generic.rest.filter.Secured;

import javax.ws.rs.Path;

@Secured
@Path("/api")
public class ApiController extends InternalController {

    @Override
    public Feature getFeature(String projectId, String workItemId) {
        return polarionService.callPrivileged(() -> super.getFeature(projectId, workItemId));
    }

    @Override
    public void createOrUpdateFeature(Feature feature) {
        polarionService.callPrivileged(() -> super.createOrUpdateFeature(feature));
    }

}
