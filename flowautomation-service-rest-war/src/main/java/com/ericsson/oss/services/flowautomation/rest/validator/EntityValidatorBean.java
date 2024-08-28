/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.services.flowautomation.rest.validator;

import com.ericsson.oss.services.flowautomation.error.ErrorCode;
import com.ericsson.oss.services.flowautomation.error.ErrorCode.UnknownErrorCode;
import com.ericsson.oss.services.flowautomation.error.ErrorReason;
import com.ericsson.oss.services.flowautomation.rest.exception.ValidationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * The Class EntityValidatorBean.
 */
@ApplicationScoped
public class EntityValidatorBean {

    /** The validator. */
    @Inject
    private Validator validator;

    /**
     * Validate.
     *
     * @param <T>
     *            the generic type
     * @param entity
     *            the entity
     * @param errorCodes
     *            the error codes
     */
    public <T> void validate(final T entity, final ErrorCode[] errorCodes) {
        final Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            final List<ErrorReason> errorReasons = violations.stream().map(voilation -> {
                final ErrorCode errorCode = getErrorCode(voilation.getMessage(), errorCodes);
                return ErrorReason.from(errorCode);
            }).collect(Collectors.toList());
            throw new ValidationException(errorReasons);
        }
    }

    /**
     * Gets the error code.
     *
     * @param code
     *            the code
     * @param errorCodes
     *            the error codes
     * @return the error code
     */
    private ErrorCode getErrorCode(final String code, final ErrorCode[] errorCodes) {
        return asList(errorCodes).stream() //
                .filter(errorCode -> errorCode.code().equals(code)) //
                .findFirst() //
                .orElse(UnknownErrorCode.INSTANCE);
    }
}
