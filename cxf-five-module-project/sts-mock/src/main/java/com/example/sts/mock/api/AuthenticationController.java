package com.example.sts.mock.api;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Path("/authentication")
public class AuthenticationController {

    @POST
    @Path("/token")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getToken(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded);
        // Expecting admin:password
        if ("admin:password".equals(credentials)) {
            // Return a dummy bearer token
            return Response.ok("mock-bearer-token-123").build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
