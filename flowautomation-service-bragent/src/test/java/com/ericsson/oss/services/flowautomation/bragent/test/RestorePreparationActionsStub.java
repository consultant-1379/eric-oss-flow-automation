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

package com.ericsson.oss.services.flowautomation.bragent.test;

import com.ericsson.adp.mgmt.bro.api.agent.Agent;
import com.ericsson.adp.mgmt.bro.api.agent.RestoreInformation;
import com.ericsson.adp.mgmt.bro.api.agent.RestorePreparationActions;

public class RestorePreparationActionsStub extends RestorePreparationActions {

    private boolean successful;
    private String message;

    public RestorePreparationActionsStub(){
        super(null,null);
    }

    public RestorePreparationActionsStub(Agent agent, RestoreInformation restoreInformation) {
        super(agent, restoreInformation);
    }

    @Override
    public void sendStageComplete(final boolean success, final String message) {
        this.successful = success;
        this.message = message;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

}
