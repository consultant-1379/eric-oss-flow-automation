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

package com.ericsson.oss.services.flowautomation.flow.wrapper;

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.EXECUTING;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.FA_INTERNAL_FLOW_INPUT_DATA;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.FLOW_INPUT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;

/**
 * The listener interface for receiving executeStart events. The class that is interested in processing a executeStart event implements this
 * interface, and the object created with that class is registered with a component using the component's <code>addExecuteStartListener<code> method.
 * When the executeStart event occurs, that object's appropriate method is invoked.
 *
 * @see ExecuteStartEvent
 */
public class ExecuteStartListener extends WrapperFlowTaskExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteStartListener.class);

    @Override
    public State getExecutionState() {
        return EXECUTING;
    }

    @Override
    public void notify(final DelegateExecution execution) throws Exception {
        final Map<String, Object> flowInput = (Map<String, Object>) execution.getVariable(FLOW_INPUT.getName());
            final Map<String, Object> clonedFlowInput = deepClone(flowInput);
            if (MapUtils.isNotEmpty(clonedFlowInput)) {
                execution.setVariable(FA_INTERNAL_FLOW_INPUT_DATA.getName(), clonedFlowInput);
            }
        super.notify(execution);
    }

    @SuppressWarnings("squid:S00108")
    public static <T> T deepClone(T object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                objectOutputStream.writeObject(object);
                try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                    ObjectInputStream objectInputStream = new ObjectInputStream(bais);
                    return (T) objectInputStream.readObject();
                }
            }
        } catch (final Exception ignore) {
            LOGGER.warn("deepClone() Failed to process: {}", ignore.getMessage());
            return null;
        }
    }
}
