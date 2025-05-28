package gr.aueb.cf.phtrade.core.services;


import gr.aueb.cf.phtrade.dto.ResponseMessageDTO;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AppNotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Override
    public Response toResponse(NotAuthorizedException exception) {

        Response.Status status = Response.Status.UNAUTHORIZED;

        return Response.status(status)
                .entity(new ResponseMessageDTO("UserNotAuthorized", exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
