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

@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface DictionaryBuilderType {

    DictionaryBuilderType.Value value();

    enum Value {

        CHOOSE_SETUP_USER_TASK_DICTIONARY_BUILDER,

        REVIEW_CONFIRM_EXECUTE_USER_TASK_DICTIONARY_BUILDER,

        SETUP_PHASE_USER_TASK_DICTIONARY_BUILDER,

        EXECUTE_PHASE_USER_TASK_DICTIONARY_BUILDER,

        FLOW_INPUT_SCHEMA_DATA_DICTIONARY_BUILDER
    }
}
