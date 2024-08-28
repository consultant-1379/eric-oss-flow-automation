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

package com.ericsson.oss.services.flowautomation.resources;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * The Interface UserTaskSchemaBuilderType.
 */
@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface UserTaskSchemaBuilderType {
    Value value();

    enum Value {

        /** The choose setup user task schema builder. */
        CHOOSE_SETUP_USER_TASK_SCHEMA_BUILDER,

        /** The review confirm execute user task schema builder. */
        REVIEW_CONFIRM_EXECUTE_USER_TASK_SCHEMA_BUILDER,

        /** The setup phase user task schema builder. */
        SETUP_PHASE_USER_TASK_SCHEMA_BUILDER,

        /** The execute phase user task schema builder. */
        EXECUTE_PHASE_USER_TASK_SCHEMA_BUILDER
    }

}
