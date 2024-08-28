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

package com.ericsson.oss.services.flowautomation.bragent.agent.behavior;

import static io.restassured.RestAssured.given;

import com.ericsson.adp.mgmt.bro.api.agent.AgentBehavior;
import com.ericsson.adp.mgmt.bro.api.agent.BackupExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.BackupPreparationActions;
import com.ericsson.adp.mgmt.bro.api.agent.CancelActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostBackupActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostRestoreActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestoreExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.RestorePreparationActions;
import com.ericsson.adp.mgmt.bro.api.registration.RegistrationInformation;
import com.ericsson.oss.services.flowautomation.bragent.agent.SoftwareVersionInformationUtils;
import com.ericsson.oss.services.flowautomation.bragent.util.BackupType;
import com.ericsson.oss.services.flowautomation.bragent.util.PropertiesHelper;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FAAgentBehavior implements AgentBehavior {

    private static final Logger log = LoggerFactory.getLogger(FAAgentBehavior.class);

    private static final String AGENT_ID_PROPERTY = "fa.agent.id";
    private static final String DEFAULT_AGENT_ID = "eric-oss-flow-automation-bragent";

    private static final String SCOPE_PROPERTY = "fa.agent.scope";
    private static final BackupType fabackupType = BackupType.DEFAULT;

    private static final String API_VERSION_PROPERTY = "fa.agent.apiVersion";
    private static final String DEFAULT_API_VERSION = "4";

    private static final String FA_SERVICE_URL = "fa.service.url";
    private static final String DEFAULT_URL =  "http://localhost";

    private static final String FA_SERVICE_PORT = "fa.service.port";
    private static final String DEFAULT_PORT = "8080";

    private static final String FA_SERVICE_BASE = "fa.service.base";
    private static final String DEFAULT_BASE = "/flow-automation";

    private static final String FA_SERVICE_PRE_RESTORE_ACTIONS = "fa.service.pre-restore-actions";
    private static final String DEFAULT_PRE_RESTORE_ACTIONS = "/internal/v1/pre-restore-actions";

    private static final String FA_SERVICE_POST_RESTORE_ACTIONS = "fa.service.post-restore-actions";
    private static final String DEFAULT_POST_RESTORE_ACTIONS = "/internal/v1/post-restore-actions";

    public FAAgentBehavior() {
        RestAssured.baseURI = PropertiesHelper.getProperty(FA_SERVICE_URL, DEFAULT_URL);
        RestAssured.port = Integer.parseInt(PropertiesHelper.getProperty(FA_SERVICE_PORT, DEFAULT_PORT));
        RestAssured.basePath = PropertiesHelper.getProperty(FA_SERVICE_BASE, DEFAULT_BASE);
    }

    @Override
    public RegistrationInformation getRegistrationInformation() {
        final RegistrationInformation registrationInfo = new RegistrationInformation();
        registrationInfo.setAgentId(getAgentId());
        registrationInfo.setApiVersion(PropertiesHelper.getProperty(API_VERSION_PROPERTY, DEFAULT_API_VERSION));
        registrationInfo.setScope(PropertiesHelper.getProperty(SCOPE_PROPERTY, fabackupType.getDescription()));
        registrationInfo.setSoftwareVersion(SoftwareVersionInformationUtils.getSoftwareVersion());
        return registrationInfo;
    }

    @Override public void prepareForRestore(RestorePreparationActions restorePreparationActions) {
        log.info("Flow Automation brAgent prepareForRestore performing...");
        try {
            log.info("Flow Automation brAgent prepareForRestore communicate to Flow Automation service via HTTP call...");
            final Response response = given().when().post(
                PropertiesHelper.getProperty(FA_SERVICE_PRE_RESTORE_ACTIONS, DEFAULT_PRE_RESTORE_ACTIONS)
            );
            log.info("Flow Automation brAgent prepareForRestore {} was returned from FA service", response.getStatusCode());
            if (response.getStatusCode() == 204) {
                restorePreparationActions.sendStageComplete(true, "eric-oss-flow-automation-brAgent has completed preRestore");
            } else {
                restorePreparationActions.sendStageComplete(false, "eric-oss-flow-automation-brAgent unexpected status code returned");
            }
        }
        catch (Exception e){
            restorePreparationActions.sendStageComplete(false, "eric-oss-flow-automation pod is not available");
            throw e;
        }
    }

    @Override public void executeRestore(RestoreExecutionActions restoreExecutionActions) {
        restoreExecutionActions.sendStageComplete(true, "oss-flow-automation-brAgent has completed executeRestore");
    }

    @Override public void postRestore(PostRestoreActions postRestoreActions) {
        log.info("Flow Automation brAgent postRestore performing...");
        try {
            log.info("eric-oss-flow-automation-brAgent postRestore communicate to Flow Automation service via HTTP call...");
            final Response response = given().when().post(
                PropertiesHelper.getProperty(FA_SERVICE_POST_RESTORE_ACTIONS, DEFAULT_POST_RESTORE_ACTIONS)
            );
            log.info("eric-oss-flow-automation-brAgent postRestore {} was returned from FA service", response.getStatusCode());
            if(response.getStatusCode() == 204){
                postRestoreActions.sendStageComplete(true, "eric-oss-flow-automation-brAgent has completed postRestore");
            }
            else{
                postRestoreActions.sendStageComplete(false, "eric-oss-flow-automation-brAgent unexpected status code returned");
            }
        }
        catch (Exception e){
            postRestoreActions.sendStageComplete(false, "eric-oss-flow-automation pod is not available");
            throw e;
        }
    }


    @Override public void executeBackup(BackupExecutionActions backupExecutionActions) { backupExecutionActions.backupComplete(true, "COMPLETED BY DEFAULT"); }

    @Override public void prepareForBackup(BackupPreparationActions backupPreparationActions) {AgentBehavior.super.prepareForBackup(backupPreparationActions); }

    @Override public void postBackup(PostBackupActions postBackupActions) {
        AgentBehavior.super.postBackup(postBackupActions);
    }

    @Override public void cancelAction(CancelActions cancelActions) {
        AgentBehavior.super.cancelAction(cancelActions);
    }

    private String getAgentId() {
        return PropertiesHelper.getProperty(AGENT_ID_PROPERTY, DEFAULT_AGENT_ID);
    }

}
