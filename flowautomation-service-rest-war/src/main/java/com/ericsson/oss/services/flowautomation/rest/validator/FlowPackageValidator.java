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

import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * The Class FlowPackageValidator.
 */
public class FlowPackageValidator implements ConstraintValidator<ValidPackage, byte[]> {

    @Override
    public void initialize(final ValidPackage constraintAnnotation) {
        // The @ValidPackage annotation does not have any parameter.
    }

    @Override
    public boolean isValid(final byte[] value, final ConstraintValidatorContext context) {
        final Optional<FlowPackageErrorCode> violation = Optional.ofNullable(FlowPackageUtil.validate(value));

        if (violation.isPresent()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(violation.get().code()).addConstraintViolation();
        }
        return !violation.isPresent();
    }
}
