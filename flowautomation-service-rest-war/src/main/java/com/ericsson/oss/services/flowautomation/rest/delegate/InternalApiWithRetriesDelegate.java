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

package com.ericsson.oss.services.flowautomation.rest.delegate;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.EJB_TYPE;
import static com.ericsson.oss.services.flowautomation.error.BackupRestoreErrorCode.ACTION_NOT_COMPLETED;
import javax.inject.Inject;
import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.brapi.RestoreHandler;
import com.ericsson.oss.services.flowautomation.exception.BackupRestoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalApiWithRetriesDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalApiWithRetriesDelegate.class);
    @Inject
    @ServiceType(EJB_TYPE)
    private RestoreHandler restoreHandlerBean;

    private static final int SLEEP_IN_SECONDS  = 5;
    private static final int MAX_TRIES  = 3;

    private static final Long THREAD_DELAY = 1000L;

    public void postRestoreActions() {
        int tries  = 0;
        boolean actionsSuccessful  = false;
        while (!actionsSuccessful && tries < MAX_TRIES) {
            try {
                restoreHandlerBean.postRestoreActions();
                actionsSuccessful = true;
            } catch (BackupRestoreException e) {
                LOGGER.error("An error occurred in post restore operation: {}", e.getMessage());
                tries++;
                threadExceptionActions();

            }
        }
        if(!actionsSuccessful){
            throw new BackupRestoreException(ACTION_NOT_COMPLETED, ACTION_NOT_COMPLETED.reason());
        }
    }


    public void preRestoreActions() {
        int tries  = 0;
        boolean actionsSuccessful  = false;
        while (!actionsSuccessful && tries < MAX_TRIES) {
            try {
                restoreHandlerBean.preRestoreActions();
                actionsSuccessful = true;
            } catch (BackupRestoreException e) {
                LOGGER.error("An error occurred in pre restore operation: {}", e.getMessage());
                tries++;
                threadExceptionActions();
            }
        }
        if(!actionsSuccessful){
            throw new BackupRestoreException(ACTION_NOT_COMPLETED, ACTION_NOT_COMPLETED.reason());
        }
    }

    public void threadExceptionActions(){
        try {
            Thread.sleep(SLEEP_IN_SECONDS * THREAD_DELAY);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BackupRestoreException(ACTION_NOT_COMPLETED, ACTION_NOT_COMPLETED.reason());
        }
    }
}
