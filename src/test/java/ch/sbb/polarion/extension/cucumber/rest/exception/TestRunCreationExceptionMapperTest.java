package ch.sbb.polarion.extension.cucumber.rest.exception;

import ch.sbb.polarion.extension.cucumber.exception.TestRunCreationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRunCreationExceptionMapperTest {

    @Test
    void toResponseMapsToBadRequestWithMessage() {
        Response response = new TestRunCreationExceptionMapper().toResponse(new TestRunCreationException("creation failed"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("creation failed", response.getEntity());
    }
}
