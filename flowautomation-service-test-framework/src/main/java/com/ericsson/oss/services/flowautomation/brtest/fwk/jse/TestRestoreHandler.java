/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.brtest.fwk.jse;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.brapi.RestoreHandler;
import com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType;
import com.ericsson.oss.services.flowautomation.test.fwk.jse.TransactionalOperationExecutor;

import javax.inject.Inject;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType.Value.TEST_JAR_TYPE;

@TestServiceType(TEST_JAR_TYPE)
public class TestRestoreHandler implements RestoreHandler {

    @Inject
    @ServiceType(JAR_TYPE)
    private RestoreHandler restoreHandler;

    @Inject
    private TransactionalOperationExecutor transactionalOperationExecutor;

    @Override
    public void preRestoreActions() {
        transactionalOperationExecutor.execute(() -> {
            restoreHandler.preRestoreActions();
            return null;
        });
    }

    @Override
    public void postRestoreActions() {
        transactionalOperationExecutor.execute(() -> {
            restoreHandler.postRestoreActions();
            return null;
        });
    }
}
