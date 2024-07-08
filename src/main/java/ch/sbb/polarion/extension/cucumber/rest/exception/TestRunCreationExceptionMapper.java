package ch.sbb.polarion.extension.cucumber.rest.exception;

import ch.sbb.polarion.extension.cucumber.exception.TestRunCreationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TestRunCreationExceptionMapper implements ExceptionMapper<TestRunCreationException> {

    public Response toResponse(TestRunCreationException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}
