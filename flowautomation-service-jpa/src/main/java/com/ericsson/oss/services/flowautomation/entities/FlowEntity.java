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

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowVersion;

/**
 * The persistent class for the fa_flow database table.
 */
@Entity
@Table(name = "fa_flow")
@NamedQuery(name = FlowEntity.FIND_ALL, query = "SELECT f FROM FlowEntity f")
@NamedQuery(name = FlowEntity.FIND_BY_FLOW_ID, query = "SELECT f FROM FlowEntity f WHERE f.flowId = :pFlowId")
@NamedQuery(name = FlowEntity.FIND_BY_NAME, query = "SELECT f FROM FlowEntity f WHERE f.name = :pName")
@NamedQuery(name = FlowEntity.ENABLE_DISABLE_FLOW, query = "UPDATE FlowEntity f SET f.status = :pFlowStatus WHERE f.flowId = :pFlowId")
@NamedQuery(name = FlowEntity.COUNT_FLOWS_ENABLED, query = "SELECT count (f) FROM FlowEntity f WHERE f.status = :pFlowStatus")
@NamedQuery(name = FlowEntity.IS_FLOW_ENABLED, query = "SELECT count (f) FROM FlowEntity f WHERE f.flowId = :pFlowId and f.status = :pFlowStatus")
@NamedQuery(name = FlowEntity.IS_FLOW_EXIST, query = "SELECT count (f) FROM FlowEntity f WHERE f.flowId = :pFlowId")
@NamedQuery(name = FlowEntity.FLOW_NAME_EXIST_FOR_ANOTHER_FLOW, query = "SELECT count (f) FROM FlowEntity f WHERE f.flowId != :pFlowId AND f.name = :pName")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class FlowEntity implements Serializable {

    /** Constants for Named Queries. */
    public static final String FIND_ALL = "FlowEntity.findAll";
    /**
     * The constant FIND_BY_FLOW_ID.
     */
    public static final String FIND_BY_FLOW_ID = "FlowEntity.findByFlowId";
    /**
     * The constant FIND_BY_NAME.
     */
    public static final String FIND_BY_NAME = "FlowEntity.findByName";
    /**
     * The constant ENABLE_DISABLE_FLOW.
     */
    public static final String ENABLE_DISABLE_FLOW = "FlowEntity.enableDisableFlow";
    /**
     * The constant COUNT_FLOWS_ENABLED.
     */
    public static final String COUNT_FLOWS_ENABLED = "FlowEntity.isCountFlowsEnabled";

    public static final String FLOW_NAME_EXIST_FOR_ANOTHER_FLOW = "FlowEntity.isFlowIdAndNameExists";

    /**
     * The constant IS_FLOW_ENABLED.
     */
    public static final String IS_FLOW_ENABLED = "FlowEntity.isFlowEnabled";
    /**
     * The constant IS_FLOW_EXIST.
     */
    public static final String IS_FLOW_EXIST = "FlowEntity.isFlowExist";
    /** Constants for Named Parameters. */
    public static final String NAMED_PARAM_FLOWID = "pFlowId";
    /**
     * The constant NAMED_PARAM_NAME.
     */
    public static final String NAMED_PARAM_NAME = "pName";
    /**
     * The constant NAMED_PARAM_FLOW_STATUS.
     */
    public static final String NAMED_PARAM_FLOW_STATUS = "pFlowStatus";
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    /** The id. */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    /** The flow id. */
    @Column(name = "flow_id")
    private String flowId;

    /** The flow name. */
    private String name;

    /** If true, flow is enabled;else disabled. */
    @Column(name = "status")
    private String status;

    /** bi-directional one-to-many association to FlowDetailEntity. */
    @OneToMany(fetch = LAZY, mappedBy = "flowEntity", cascade = ALL, orphanRemoval = true)
    @NotNull
    @Size(min = 1)
    private List<FlowDetailEntity> flowDetailEntities;

    @Column
    @Enumerated(EnumType.STRING)
    private FlowEntitySource source = FlowEntitySource.EXTERNAL;

    @Override
    public int hashCode() {
        return Objects.hash(flowId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FlowEntity that = (FlowEntity) o;
        return flowId.equals(that.flowId);
    }

    /**
     * Gets the flow description.
     *
     * @return the flow description
     */
    public FlowDefinition getFlowDefinition() {
        final List<FlowVersion> flowVersions = this.getFlowDetailEntities().stream().map(FlowDetailEntity::getFlowVersion)
                .collect(Collectors.toList());
        return new FlowDefinition(this.getFlowId(), this.getName(), this.getStatus(), this.getSource().name(), flowVersions);
    }
}
