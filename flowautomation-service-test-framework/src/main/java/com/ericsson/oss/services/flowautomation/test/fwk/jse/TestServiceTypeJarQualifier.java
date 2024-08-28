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

package com.ericsson.oss.services.flowautomation.test.fwk.jse;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType.Value.TEST_JAR_TYPE;

import javax.enterprise.util.AnnotationLiteral;

import com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType;

public class TestServiceTypeJarQualifier extends AnnotationLiteral<TestServiceType> implements TestServiceType {

    private static final long serialVersionUID = 1L;

    @Override
    public Value value() {
        return TEST_JAR_TYPE;
    }
}