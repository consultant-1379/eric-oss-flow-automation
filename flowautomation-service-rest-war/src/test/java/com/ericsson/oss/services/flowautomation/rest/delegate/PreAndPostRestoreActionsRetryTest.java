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

import static com.ericsson.oss.services.flowautomation.error.BackupRestoreErrorCode.ACTION_NOT_COMPLETED;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ericsson.oss.services.flowautomation.brapi.RestoreHandler;
import com.ericsson.oss.services.flowautomation.exception.BackupRestoreException;
import org.mockito.MockitoAnnotations;

public class PreAndPostRestoreActionsRetryTest {

    @Mock
    RestoreHandler restoreHandlerBean;

    @InjectMocks
    InternalApiWithRetriesDelegate internalApiWithRetriesDelegate;

    @Before
    public void init_mocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void beanPreRestoreActionsInvokesRetriesOnBackupRestoreException() {
        doThrow(new BackupRestoreException(ACTION_NOT_COMPLETED, ACTION_NOT_COMPLETED.reason())).when(restoreHandlerBean).preRestoreActions();
        try {
            internalApiWithRetriesDelegate.preRestoreActions();
        } catch (BackupRestoreException e) {
            //catch if retries exhausted
            verify(restoreHandlerBean, times(3)).preRestoreActions();
            return;
        }
        fail();
    }

    @Test
    public void beanPostRestoreActionsInvokesRetriesOnBackupRestoreException() {
        doThrow(new BackupRestoreException(ACTION_NOT_COMPLETED, ACTION_NOT_COMPLETED.reason())).when(restoreHandlerBean).postRestoreActions();
        try {
            internalApiWithRetriesDelegate.postRestoreActions();
        } catch (BackupRestoreException e) {
            //catch if retries exhausted
            verify(restoreHandlerBean, times(3)).postRestoreActions();
            return;
        }
        fail();
    }

}
