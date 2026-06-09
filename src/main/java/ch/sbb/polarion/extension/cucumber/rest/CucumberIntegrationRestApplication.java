package ch.sbb.polarion.extension.cucumber.rest;

import ch.sbb.polarion.extension.cucumber.rest.controller.ApiController;
import ch.sbb.polarion.extension.cucumber.rest.controller.InternalController;
import ch.sbb.polarion.extension.cucumber.rest.controller.JiraRestApiController;
import ch.sbb.polarion.extension.cucumber.rest.controller.XrayExportCucumberTestsController;
import ch.sbb.polarion.extension.cucumber.rest.controller.XrayImportExecutionResultsController;
import ch.sbb.polarion.extension.cucumber.rest.exception.TestRunCreationExceptionMapper;
import ch.sbb.polarion.extension.generic.rest.GenericRestApplication;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CucumberIntegrationRestApplication extends GenericRestApplication {

    @Override
    @NotNull
    protected Set<Object> getExtensionExceptionMapperSingletons() {
        return Set.of(new TestRunCreationExceptionMapper());
    }

    @Override
    @NotNull
    protected Set<Class<?>> getExtensionControllerClasses() {
        return Set.of(
                ApiController.class,
                InternalController.class,
                JiraRestApiController.class,
                XrayExportCucumberTestsController.class,
                XrayImportExecutionResultsController.class
        );
    }
}
