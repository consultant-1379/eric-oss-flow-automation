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

package com.ericsson.oss.services.flowautomation.flow.utils;

import static java.lang.String.format;

/**
 * This is a pojo to house the flowId, flowExecution name and business key id of a given camunda business key.
 */
public class FlowBusinessKey {

    private final String camundaBusinessKey;
    private final String flowId;
    private final String flowExecutionName;
    private final String businessKeyId;

    /**
     * Instantiates a new flow business key.
     *
     * @param camundaBusinessKey
     *            the camunda business key
     */
    public FlowBusinessKey(final String camundaBusinessKey) {
        this.camundaBusinessKey = camundaBusinessKey;
        /**
         * cannot use DELIMITER because contains special regexp chars
         */
        final String[] bits = camundaBusinessKey.split("\\.-\\.");
        flowId = bits[0];
        flowExecutionName = bits[1];
        businessKeyId = bits[2];
    }

    /**
     * Instantiates a new flow business key.
     *
     * @param flowId
     *            the flow id
     * @param flowExecutionName
     *            the flow execution name
     * @param businessKeyId
     *            the business key id
     */
    public FlowBusinessKey(final String flowId, final String flowExecutionName, final String businessKeyId) {
        this.flowId = flowId;
        this.flowExecutionName = flowExecutionName;
        this.businessKeyId = businessKeyId;
        camundaBusinessKey = flowId + FlowAutomationServiceUtil.CAMUNDA_ID_DELIMITER + flowExecutionName
                + FlowAutomationServiceUtil.CAMUNDA_ID_DELIMITER + businessKeyId;
    }

    /**
     * Gets the flow execution name.
     *
     * @return the flow execution name
     */
    public String getFlowExecutionName() {
        return flowExecutionName;
    }

    /**
     * Gets the flow id.
     *
     * @return the flow id
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Gets the business key id.
     *
     * @return the business key id
     */
    public String getBusinessKeyId() {
        return businessKeyId;
    }

    /**
     * Gets the camunda business key.
     *
     * @return the camunda business key
     */
    public String getCamundaBusinessKey() {
        return camundaBusinessKey;
    }

    @Override
    public String toString() {
        return format("FlowBusinessKey: [flowId=%s, flowExecutionName=%s, businessKeyId=%s]", flowId, flowExecutionName, businessKeyId);
    }
}
