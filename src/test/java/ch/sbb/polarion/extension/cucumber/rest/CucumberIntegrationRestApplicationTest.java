package ch.sbb.polarion.extension.cucumber.rest;

import ch.sbb.polarion.extension.cucumber.rest.controller.ApiController;
import ch.sbb.polarion.extension.cucumber.rest.controller.InternalController;
import ch.sbb.polarion.extension.cucumber.rest.controller.JiraRestApiController;
import ch.sbb.polarion.extension.cucumber.rest.controller.XrayExportCucumberTestsController;
import ch.sbb.polarion.extension.cucumber.rest.controller.XrayImportExecutionResultsController;
import ch.sbb.polarion.extension.cucumber.rest.exception.TestRunCreationExceptionMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CucumberIntegrationRestApplicationTest {

    private final CucumberIntegrationRestApplication application = new CucumberIntegrationRestApplication();

    @Test
    void exposesAllExtensionControllers() {
        Set<Class<?>> controllers = application.getExtensionControllerClasses();

        assertThat(controllers).containsExactlyInAnyOrder(
                ApiController.class,
                InternalController.class,
                JiraRestApiController.class,
                XrayExportCucumberTestsController.class,
                XrayImportExecutionResultsController.class);
    }

    @Test
    void exposesTestRunCreationExceptionMapper() {
        Set<Object> mappers = application.getExtensionExceptionMapperSingletons();

        assertThat(mappers).hasSize(1);
        assertThat(mappers.iterator().next()).isInstanceOf(TestRunCreationExceptionMapper.class);
    }
}
