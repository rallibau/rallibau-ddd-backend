package com.rallibau.apps.commons.filters;

import com.rallibau.shared.domain.DomainError;
import com.rallibau.shared.domain.Utils;
import com.rallibau.shared.domain.bus.command.CommandHandlerExecutionError;
import com.rallibau.shared.domain.bus.query.QueryHandlerExecutionError;
import com.rallibau.shared.infrastructure.spring.api.ApiController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public final class ApiExceptionMiddleware implements Filter {

    private static final Logger logger = LogManager.getLogger(ApiExceptionMiddleware.class);
    private final RequestMappingHandlerMapping mapping;

    public ApiExceptionMiddleware(RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) {
        Optional<Object> possibleController = Optional.empty();
        HttpServletRequest httpRequest = ((HttpServletRequest) request);
        HttpServletResponse httpResponse = ((HttpServletResponse) response);
        try {


            if (Objects.requireNonNull(
                    mapping.getHandler(httpRequest)).getHandler() instanceof HandlerMethod) {
                possibleController = Optional.of((
                        (HandlerMethod) Objects.requireNonNull(
                                mapping.getHandler(httpRequest)).getHandler()
                ).getBean());
            }


            chain.doFilter(request, response);
        } catch (Exception exception) {
            if (possibleController.isPresent() && possibleController.get() instanceof ApiController) {
                handleCustomError(response,
                        httpResponse, (ApiController) possibleController.get(), exception);
            }
        }

    }

    private void handleCustomError(
            ServletResponse response,
            HttpServletResponse httpResponse,
            ApiController possibleController,
            Exception exception
    ) {
        HashMap<Class<? extends RuntimeException>, HttpStatus> errorMapping = possibleController
                .errorMapping();
        Throwable error = (
                exception.getCause() instanceof CommandHandlerExecutionError ||
                        exception.getCause() instanceof QueryHandlerExecutionError
        )
                ? exception.getCause().getCause() : exception.getCause();

        if (!(error instanceof DomainError)) {
            logger.error(error.getMessage(), error);
        }
        int statusCode = statusFor(errorMapping, error);
        String errorCode = errorCodeFor(error);
        String errorMessage = error.getMessage();


        httpResponse.reset();
        httpResponse.setHeader("Content-Type", "application/json");
        httpResponse.setStatus(statusCode);
        PrintWriter writer;
        try {
            writer = response.getWriter();
            writer.write(String.format(
                    "{\"error_code\": \"%s\", \"message\": \"%s\"}",
                    errorCode,
                    errorMessage
            ));
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private String errorCodeFor(Throwable error) {
        if (error instanceof DomainError) {
            return ((DomainError) error).errorCode();
        }


        return Utils.toSnake(error.getClass().toString());
    }

    private int statusFor(HashMap<Class<? extends RuntimeException>, HttpStatus> errorMapping, Throwable error) {
        return errorMapping.getOrDefault(error.getClass(), HttpStatus.INTERNAL_SERVER_ERROR).value();
    }
}