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

package com.ericsson.oss.services.flowautomation.entities;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowVersion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The persistent class for the fa_flow_detail database table.
 */
@Entity
@Table(name = "fa_flow_detail")
@NamedQuery(name = FlowDetailEntity.FIND_ALL, query = "SELECT f FROM FlowDetailEntity f")
@NamedQuery(name = FlowDetailEntity.FIND_BY_PROCESS_DEFINITION_KEY, query = "SELECT f FROM FlowDetailEntity f WHERE f.processDefinitionKey = :pProcessDefinitionKey")
@NamedQuery(name = FlowDetailEntity.GET_PROCESS_DEFINITION_KEY, query = "SELECT f FROM FlowDetailEntity f WHERE f.flowEntity.flowId = :pFlowId and f.isActive = :pFlowVersionStatus and f.flowEntity.status = :pFlowStatus")
@NamedQuery(name = FlowDetailEntity.ACTIVATE_DEACTIVATE_SPECIFIC_FLOW_VERSION, query = "UPDATE FlowDetailEntity f SET f.isActive = :pNewstatus WHERE f.processDefinitionKey = :pProcessDefinitionKey")
@NamedQuery(name = FlowDetailEntity.DEACTIVATE_FLOW_VERSION, query = "UPDATE FlowDetailEntity f SET f.isActive = :pNewstatus WHERE f.flowEntity.flowId = :pFlowId and f.isActive = :pOldstatus")
@NamedQuery(name = FlowDetailEntity.UPDATE_DEPLOYMENT_ID, query = "UPDATE FlowDetailEntity f SET f.deploymentId = :pDeploymentId WHERE f.processDefinitionKey = :pProcessDefinitionKey")
@NamedQuery(name = FlowDetailEntity.FLOW_VERSION_CURRENTLY_ACTIVE, query = "SELECT count(f) from FlowDetailEntity f WHERE f.isActive = :pFlowVersionStatus")
@NamedQuery(name = FlowDetailEntity.IS_FLOW_VERSION_ACTIVE, query = "SELECT 1 FROM FlowDetailEntity f WHERE f.flowEntity.flowId = :pFlowId AND f.version = :pVersion AND f.isActive = true")
@NamedQuery(name = FlowDetailEntity.FIND_BY_FLOWID_AND_VERSION, query = "SELECT f FROM FlowDetailEntity f WHERE f.flowEntity.flowId = :pFlowId AND f.version = :pVersion")
@NamedQuery(name = FlowDetailEntity.IS_FLOW_VERSION_EXISTS, query = "SELECT count (f) FROM FlowDetailEntity f WHERE f.flowEntity.flowId = :pFlowId AND f.version = :pVersion")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class FlowDetailEntity implements Serializable {

    /** Constants for Named Queries */
    public static final String FIND_ALL = "FlowDetailEntity.findAll";
    public static final String FIND_BY_PROCESS_DEFINITION_KEY = "FlowDetailEntity.findByProcessDefinitionKey";
    public static final String GET_PROCESS_DEFINITION_KEY = "FlowDetailEntity.getProcessDefinitionKey";
    public static final String ACTIVATE_DEACTIVATE_SPECIFIC_FLOW_VERSION = "FlowDetailEntity.activateDeactivateSpecificFlowVersion";
    public static final String DEACTIVATE_FLOW_VERSION = "FlowDetailEntity.deactivateFlowVersion";
    public static final String UPDATE_DEPLOYMENT_ID = "FlowDetailEntity.updateDeploymentId";
    public static final String FLOW_VERSION_CURRENTLY_ACTIVE = "FlowDetailEntity.isFlowVersionCurrentlyActive";
    public static final String IS_FLOW_VERSION_ACTIVE = "FlowDetailEntity.isFlowVersionActive";
    public static final String FIND_BY_FLOWID_AND_VERSION = "FlowDetailEntity.findByFlowIdAndVersion";
    public static final String IS_FLOW_VERSION_EXISTS = "FlowDetailEntity.isFlowVersionExists";
    /** Constants for Named Parameters */
    public static final String NAMED_PARAM_FLOW_VERSION_STATUS = "pFlowVersionStatus";
    public static final String NAMED_PARAM_FLOW_ID = "pFlowId";
    public static final String NAMED_PARAM_VERSION = "pVersion";
    public static final String NAMED_PARAM_PROCESS_DEFINITION_KEY = "pProcessDefinitionKey";
    private static final long serialVersionUID = 1L;
    /** The id. */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    /** The process definition key. */
    @Column(name = "process_definition_key")
    private String processDefinitionKey;

    /** The deployment id. */
    @Column(name = "deployment_id")
    private String deploymentId;

    /** The description. */
    /** The columnDefinition would impact only DDL generation. */
    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    /** The execute id. */
    @Column(name = "execute_id")
    private String executeId;

    /** The imported by user. */
    @Column(name = "imported_by_user")
    private String importedByUser;

    /** The imported date. */
    @Column(name = "imported_date")
    private Timestamp importedDate;

    /** The setup id. */
    @Column(name = "setup_id")
    private String setupId;

    /** Is the report supported? */
    @Column(name = "is_report_supported")
    private Boolean isReportSupported;

    /** If true, flow version is active;else inactive. */
    @Column(name = "is_active")
    private Boolean isActive;

    /** The version. */
    private String version;

    /** bi-directional many-to-one association to FlowEntity */
    @ManyToOne(cascade = PERSIST)
    @JoinColumn(name = "fa_flow_id")
    private FlowEntity flowEntity;

    /** bi-directional one-to-many association to FlowExecutionEntity */
    @OneToMany(fetch = LAZY, mappedBy = "flowDetailEntity", cascade = ALL, orphanRemoval = true)
    private List<FlowExecutionEntity> flowExecutionEntities;

    @Column(name = "back_enabled")
    private Boolean backEnabled;

    /**
     * Gets the imported date.
     *
     * @return the imported date
     */
    public Timestamp getImportedDate() {
        return new Timestamp(this.importedDate.getTime());
    }

    /**
     * Sets the imported date.
     *
     * @param importedDate the new imported date
     */
    public void setImportedDate(final Timestamp importedDate) {
        this.importedDate = new Timestamp(importedDate.getTime());
    }

    /**
     * Adds the flow execution entity.
     *
     * @param flowExecutionEntity the flow execution entity
     * @return the flow execution entity
     */
    public FlowExecutionEntity addFlowExecutionEntity(final FlowExecutionEntity flowExecutionEntity) {
        this.flowExecutionEntities.add(flowExecutionEntity);
        flowExecutionEntity.setFlowDetailEntity(this);

        return flowExecutionEntity;
    }

    public boolean isBackEnabled() {
        return TRUE.equals(backEnabled);
    }

    /**
     * On create.
     */
    @PrePersist
    void onCreate() {
        this.setImportedDate(DateTimeUtil.getTimeStampWithMilliSeconds());
    }

    /**
     * Gets the flow version.
     *
     * @return the flow version
     */
    public FlowVersion getFlowVersion() {
        final String isoTimeStamp = DateTimeUtil.getIso8601UtcStringFromEpochMilli(getImportedDate().getTime());
        return new FlowVersion(this.version, this.description, this.getIsActive(), this.getImportedByUser(), isoTimeStamp, ofNullable(this.getSetupId()).isPresent(), this.getIsReportSupported());
    }

    public FlowDefinition getFlowDefinition() {
        final List<FlowVersion> flowVersions = new ArrayList<>();
        flowVersions.add(getFlowVersion());
        return new FlowDefinition(this.getFlowEntity().getFlowId(), this.getFlowEntity().getName(), this.getFlowEntity().getStatus(), this.getFlowEntity().getSource().name(), flowVersions);
    }
}