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

import com.ericsson.oss.services.flowautomation.model.FlowExecution;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * The persistent class for the fa_flow_execution database table.
 */
@Entity
@Table(name = "fa_flow_execution")
@NamedQuery(name = FlowExecutionEntity.FIND_ALL, query = "SELECT f FROM FlowExecutionEntity f")
@NamedQuery(name = FlowExecutionEntity.FIND_BY_PROCESS_INSTANCE_ID, query = "SELECT f FROM FlowExecutionEntity f where f.processInstanceId = :pProcessInstanceId")
@NamedQuery(name = FlowExecutionEntity.IS_FLOW_EXECUTION_NAME_EXISTS, query = "SELECT count(f) FROM FlowExecutionEntity f where f.flowDetailEntity.flowEntity.flowId = :pflowId and f.flowExecutionName = :pflowExecutionName")
@NamedQuery(name = FlowExecutionEntity.IS_FLOW_EXECUTION_NAME_LIKE_EXISTS, query = "SELECT count(f) FROM FlowExecutionEntity f where f.flowDetailEntity.flowEntity.flowId = :pflowId and f.flowExecutionName LIKE :pflowExecutionName")
@NamedQuery(name = FlowExecutionEntity.GET_PROCESS_INSTANCE_ID, query = "SELECT f.processInstanceId FROM FlowExecutionEntity f where f.flowDetailEntity.flowEntity.flowId = :pflowId and f.flowExecutionName LIKE :pflowExecutionName")
@NamedQuery(name = FlowExecutionEntity.GET_ALL_PROCESS_INSTANCE_IDS, query = "SELECT f.processInstanceId FROM FlowExecutionEntity f")
@NamedQuery(name = FlowExecutionEntity.GET_FLOW_EXECUTION, query = "SELECT f FROM FlowExecutionEntity f where f.flowDetailEntity.flowEntity.flowId = :pflowId and f.flowExecutionName = :pflowExecutionName")
@NamedQuery(name = FlowExecutionEntity.GET_FLOW_EXECUTIONS_OF_VERSION, query = "SELECT f FROM FlowExecutionEntity f where f.flowDetailEntity.flowEntity.flowId = :pflowId and f.flowDetailEntity.version = :pflowVersion")
@NamedQuery(name = FlowExecutionEntity.DELETE_FLOW_EXECUTION, query = "DELETE FROM FlowExecutionEntity f where f.processInstanceId = :pProcessInstanceId")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class FlowExecutionEntity implements Serializable {

    /** Constants for Named Queries */
    public static final String FIND_ALL = "FlowExecutionEntity.findAll";
    public static final String FIND_BY_PROCESS_INSTANCE_ID = "FlowExecutionEntity.findByProcessInstanceId";
    public static final String IS_FLOW_EXECUTION_NAME_EXISTS = "FlowExecutionEntity.isFlowExecutionNameExists";
    public static final String IS_FLOW_EXECUTION_NAME_LIKE_EXISTS = "FlowExecutionEntity.isFlowExecutionNameLikeExists";
    public static final String GET_PROCESS_INSTANCE_ID = "FlowExecutionEntity.getProcessInstanceId";
    public static final String GET_FLOW_EXECUTION = "FlowExecutionEntity.getFlowExecution";
    public static final String GET_ALL_PROCESS_INSTANCE_IDS = "FlowExecutionEntity.getAllProcessInstancesIds";
    public static final String DELETE_FLOW_EXECUTION = "FlowExecutionEntity.deleteFlowExecution";
    public static final String GET_FLOW_EXECUTIONS_OF_VERSION = "FlowExecutionEntity.getFlowExecutionsOfVersion";
    /** Constants for Named Parameters */
    public static final String NAMED_PARAM_FLOW_EXECUTION_NAME = "pflowExecutionName";
    public static final String NAMED_PARAM_FLOW_ID = "pflowId";
    public static final String NAMED_PARAM_PROCESS_INSTANCE_ID = "pProcessInstanceId";
    public static final String NAMED_PARAM_FLOW_VERSION = "pflowVersion";
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    /** The id. */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    /** The process instance id. */
    @Column(name = "process_instance_id")
    private String processInstanceId;

    /** The process instance business key. */
    @Column(name = "process_instance_business_key")
    private String processInstanceBusinessKey;

    /** The flow Execution Name. */
    @Column(name = "flow_execution_name")
    private String flowExecutionName;

    /** The executed by user. */
    @Column(name = "executed_by_user")
    private String executedByUser;

    /** bi-directional many-to-one association to FlowDetailEntity. */
    @ManyToOne(cascade = PERSIST)
    @JoinColumn(name = "fa_flow_detail_id")
    private FlowDetailEntity flowDetailEntity;

    /** bi-directional one-to-many association to ReportVariableEntity */
    @OneToMany(fetch = LAZY, mappedBy = "flowExecutionEntity", cascade = ALL, orphanRemoval = true)
    private List<ReportVariableEntity> reportVariableEntities;

    //Kept this relationship only for deletion
    @OneToMany(fetch = LAZY, mappedBy = "flowExecutionEntity", cascade = REMOVE, orphanRemoval = true)
    private List<FlowExecutionEventEntity> executionEvents;

    /**
     * Returns the flow id for the current execution.
     *
     * @return the String flowId.
     */
    public String getFlowId() {
        return flowDetailEntity.getFlowEntity().getFlowId();
    }

    /**
     * Gets the flow execution.
     *
     * @param startTime       the start time
     * @param endTime         the end time
     * @param duration        the duration
     * @param summaryReport   the summary report
     * @param state           the state
     * @param activeUserTasks the active user tasks
     * @return the flow execution
     */
    public FlowExecution getFlowExecution(final String startTime, final String endTime, final String duration, final String summaryReport,
                                          final String state, final long activeUserTasks) {
        return new FlowExecution(flowExecutionName, flowDetailEntity.getFlowEntity().getFlowId(), flowDetailEntity.getFlowEntity().getName(),
                flowDetailEntity.getVersion(), flowDetailEntity.getImportedByUser(), executedByUser, startTime, endTime, duration, summaryReport,
                state, activeUserTasks, flowDetailEntity.getFlowEntity().getSource().name(), processInstanceId, processInstanceBusinessKey);
    }
}