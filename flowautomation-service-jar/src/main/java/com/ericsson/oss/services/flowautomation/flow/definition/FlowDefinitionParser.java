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

package com.ericsson.oss.services.flowautomation.flow.definition;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_DEFINITION_IS_INVALID;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_INVALID;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_PARSE_ERROR;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_PACKAGE_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_PACKAGE_EMPTY;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil.FLOW_DEFINITION_FILE_NAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.exception.ValidationException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowDefinitionException;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil;
import com.ericsson.oss.services.flowautomation.flow.utils.JsonParser;

/**
 * The Class FlowDefinitionParser.
 */
public final class FlowDefinitionParser {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(FlowDefinitionParser.class);

    /** The ValidatorFactory and Validator instances are Thread-Safe */
    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    /**
     * Instantiates a new flow definition parser.
     */
    private FlowDefinitionParser() {
        // Empty constructor
    }

    /**
     * Parses the flow definition json.
     *
     * @param flowPackage
     *            the flow package
     * @return the flow config
     */
    public static FlowConfig parseFlowDefinitionJson(final byte[] flowPackage) {
        final Predicate<ZipEntry> configFileFilter = zipEntry -> FLOW_DEFINITION_FILE_NAME.equalsIgnoreCase(zipEntry.getName());
        try {
            final boolean isFlowPackageNull = FlowPackageUtil.checkIfZipIsNull(flowPackage);
            if (isFlowPackageNull){
                throw new EntityNotFoundException(FLOW_PACKAGE_NOT_FOUND, FLOW_PACKAGE_NOT_FOUND.reason());
            }
            final boolean isFlowPackageEmpty = FlowPackageUtil.checkIfZipIsEmpty(flowPackage);
            if (isFlowPackageEmpty){
                throw new EntityNotFoundException(FLOW_PACKAGE_EMPTY, FLOW_PACKAGE_EMPTY.reason());
            }
            final byte[] fileBytes = FlowPackageUtil.extractFile(flowPackage, configFileFilter)
                    .orElseThrow(() -> new FlowDefinitionException(FLOW_DEFINITION_NOT_FOUND));
            final FlowConfig flowConfig = JsonParser.convertToType(new String(fileBytes, StandardCharsets.UTF_8), FlowConfig.class);
            final Set<ConstraintViolation<FlowConfig>> violations = validator.validate(flowConfig);
            if (!violations.isEmpty()) {
                final List<String> errorReasons = violations.stream().map(
                        violation -> String.format(FLOW_DEFINITION_IS_INVALID, violation.getInvalidValue(), violation.getPropertyPath().toString()))
                        .collect(Collectors.toList());
                logger.error("Invalid flow definition");
                throw new ValidationException(FLOW_DEFINITION_INVALID, errorReasons.toString());
            }
            return flowConfig;
        } catch (final IOException e) {
            throw new FlowDefinitionException(FLOW_DEFINITION_PARSE_ERROR, e);
        }
    }
}
