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

package com.ericsson.oss.services.flowautomation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * This is generic class to be used to be used to enable,disable,activate,deactivate flow.
 */
@Getter
@Setter
public class FlowStatus {

    /**
     * The status. Flow enable - true, Flow disable - false, Flow Version activate - true, Flow Version deactivate - false.
     */
    private boolean status;

}
