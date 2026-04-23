/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log the full stack trace on the SERVER (not sent to client!)
        LOGGER.log(Level.SEVERE, "Unexpected error: " + ex.getMessage(), ex);

        ErrorResponse body = new ErrorResponse(
            500,
            "Internal Server Error",
            "An unexpected error occurred. Please contact the administrator."
        );
        return Response.status(500)
                       .entity(body)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

