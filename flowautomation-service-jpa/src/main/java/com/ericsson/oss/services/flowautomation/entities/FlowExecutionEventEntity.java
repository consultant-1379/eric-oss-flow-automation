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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The type Flow execution event entity.
 */
@Entity
@Table(name = "fa_flow_execution_event")
@Getter
@ToString
@NoArgsConstructor
public class FlowExecutionEventEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_time")
    private Timestamp eventTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_severity")
    private FlowExecutionEventSeverity severity;

    @Column(name = "target")
    private String target;

    @Column(name = "message", columnDefinition = "LONGTEXT") //This is for only having h2 to store large values.
    private String message;

    @Column(name = "event_data", columnDefinition = "LONGTEXT") //This is for only having h2 to store large values.
    private String eventData;

    @ManyToOne
    @JoinColumn(name = "fa_flow_execution_id")
    private FlowExecutionEntity flowExecutionEntity;

    /**
     * Sets timestamp.
     */
    @PrePersist
    public void setTimestamp() {
        this.eventTime = DateTimeUtil.getTimeStampWithMilliSeconds();
    }

    /**
     * Gets event time.
     *
     * @return the event time
     */
    public Timestamp getEventTime() {
        return new Timestamp(eventTime.getTime());
    }

    /**
     * Instantiates a new Flow execution event entity.
     *
     * @param flowExecutionEntity the flow execution entity
     * @param severity            the severity
     * @param message             the message
     * @param target              the target
     * @param eventData           the event data
     */
    public FlowExecutionEventEntity(final FlowExecutionEntity flowExecutionEntity, final FlowExecutionEventSeverity severity, final String message, final String target, final String eventData) {
        this.flowExecutionEntity = flowExecutionEntity;
        this.severity = severity;
        this.message = message;
        this.target = target;
        this.eventData = eventData;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FlowExecutionEventEntity that = (FlowExecutionEventEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
