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

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Persistent class for fa_flow_execution_report_variable database table.
 *
 */
@Entity
@Table(name = "fa_flow_execution_report_variable")
@NamedQuery(name = ReportVariableEntity.DELETE_REPORT_VARIABLE, query = "DELETE FROM ReportVariableEntity rv WHERE rv.flowExecutionEntity.id = :pFlowExecutionId AND rv.name = :pVariable")
@NamedQuery(name = ReportVariableEntity.GET_REPORT_VARIABLE, query = "SELECT rv.value FROM ReportVariableEntity rv WHERE rv.flowExecutionEntity.id = :pFlowExecutionId AND rv.name = :pVariable")
@NamedQuery(name = ReportVariableEntity.GET_REPORT_VARIABLES_WITHIN_SIZE, query = "SELECT rv FROM ReportVariableEntity rv WHERE rv.flowExecutionEntity.id = :pFlowExecutionId AND rv.size <= :pSize")
@NamedQuery(name = ReportVariableEntity.GET_REPORT_VARAIABLE_NAMES_BEYOND_SIZE, query = "SELECT rv.name FROM ReportVariableEntity rv WHERE rv.flowExecutionEntity.id = :pFlowExecutionId AND rv.size > :pSize")
@NamedQuery(name = ReportVariableEntity.GET_HIDDEN_REPORT_VARAIABLE_NAMES, query = "SELECT rv.name FROM ReportVariableEntity rv WHERE rv.flowExecutionEntity.id = :pFlowExecutionId AND rv.name LIKE 'FaInternal_%' AND value ='hidden: true'")
@Getter
@Setter
@ToString

public class ReportVariableEntity implements Serializable {

    /** Constants for Named Queries. */
    public static final String DELETE_REPORT_VARIABLE = "ReportVariableEntity.deleteReportVariable";
    public static final String GET_REPORT_VARIABLE = "ReportVariableEntity.getReportVariable";
    public static final String FA_INTERNAL_VARIABLE_SUMMARY_REPORT = "FA_INTERNAL_VARIABLE_SUMMARY_REPORT";
    public static final String GET_REPORT_VARIABLES_WITHIN_SIZE = "ReportVariableEntity.getReportVariablesWithinSize";
    public static final String GET_REPORT_VARAIABLE_NAMES_BEYOND_SIZE = "ReportVariableEntity.getReportVariableNamesBeyondSize";
    public static final String GET_HIDDEN_REPORT_VARAIABLE_NAMES = "ReportVariableEntity.getHiddenReportVariableNames";
    /** Constants for Named Parameters. */
    public static final String NAMED_FLOW_EXECUTION_ID = "pFlowExecutionId";
    public static final String NAMED_REPORT_VARIABLE = "pVariable";
    public static final String NAMED_REPORT_VARIABLE_SIZE = "pSize";
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "created_time")
    private Timestamp createdtime;

    /** bi-directional many-to-one association to FlowExecutionEntity. */
    @ManyToOne(cascade = PERSIST)
    @JoinColumn(name = "fa_flow_execution_id")
    private FlowExecutionEntity flowExecutionEntity;

    @Column(name = "name")
    private String name;

    @Column(name = "value", columnDefinition = "LONGTEXT") //This is for only having h2 to store large values.
    private String value;


    @Column(name= "size")
    private int size;

    @PrePersist
    public void onCreate() {
        this.setCreatedtime(DateTimeUtil.getTimeStamp());
    }

    /**
     * @return the created time
     */
    public Timestamp getCreatedtime() {
        return new Timestamp(createdtime.getTime());
    }

    /**
     * @param createdtime
     *            the created time to set
     */
    public void setCreatedtime(final Timestamp createdtime) {
        this.createdtime = new Timestamp(createdtime.getTime());
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ReportVariableEntity other = (ReportVariableEntity) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
