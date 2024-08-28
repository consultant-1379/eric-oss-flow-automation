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

package com.ericsson.oss.services.flowautomation.execution.resource;

import java.util.Objects;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The type Flow execution resource type literal.
 */
public class FlowExecutionResourceTypeLiteral extends AnnotationLiteral<FlowExecutionResourceType> implements FlowExecutionResourceType {

    private static final long serialVersionUID = -1861815520160618178L;

    private final String type;

    /**
     * Instantiates a new Flow execution resource type literal.
     *
     * @param type the type
     */
    public FlowExecutionResourceTypeLiteral(final String type) {
        this.type = type;
    }

    @Override
    public String value() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final FlowExecutionResourceTypeLiteral that = (FlowExecutionResourceTypeLiteral) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}
