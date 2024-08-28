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

package com.ericsson.oss.services.flowautomation.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.ToString;

/**
 * The type Flow execution event specific filter that holds the different fields on which filtering is possible.
 */
@Getter
@ToString
public class FlowExecutionEventFilter extends GenericFilter {

    private final String flowId;
    private final String flowExecutionName;
    private final String target;
    private final List<FlowExecutionEventSeverity> eventsSeverity;
    private final String message;
    private final Date startDate;
    private final Date endDate;

    @SuppressWarnings("squid:S00107")
    private FlowExecutionEventFilter(final String flowId, final String flowExecutionName, final String target, final List<FlowExecutionEventSeverity> eventsSeverity, final Date startDate, final Date endDate, final String message, final PaginationData paginationData, final SortData sortData) {
        super(paginationData, sortData);
        this.flowId = flowId;
        this.flowExecutionName = flowExecutionName;
        this.target = target;
        this.eventsSeverity = Objects.nonNull(eventsSeverity) ? Collections.unmodifiableList(eventsSeverity) : null;
        this.startDate = Objects.nonNull(startDate) ? new Date(startDate.getTime()) : null;
        this.endDate = Objects.nonNull(endDate) ? new Date(endDate.getTime()) : null;
        this.message = message;
    }

    /**
     * The type Builder for building the {@code FlowExecutionEventFilter} instance.
     */
    public static final class Builder {
        private final String flowId;
        private final String flowExecutionName;
        private String target;
        private List<FlowExecutionEventSeverity> eventsSeverity;
        private String message;
        private Date startDate;
        private Date endDate;

        private PaginationData paginationData;
        private SortData sortData;

        /**
         * Instantiates a new Builder.
         *
         * @param flowId
         *         the flow id
         * @param flowExecutionName
         *         the flow execution name
         */
        public Builder(final String flowId, final String flowExecutionName) {
            this.flowId = flowId;
            this.flowExecutionName = flowExecutionName;
        }

        /**
         * Message builder.
         *
         * @param message
         *         the message
         * @return the builder
         */
        public Builder message(final String message) {
            this.message = message;
            return this;
        }

        /**
         * Start time builder.
         *
         * @param startDate
         *         the start time
         * @return the builder
         */
        public Builder startDate(final Date startDate) {
            this.startDate = Objects.nonNull(startDate) ? new Date(startDate.getTime()) : null;
            return this;
        }

        /**
         * End time builder.
         *
         * @param endDate
         *         the end time
         * @return the builder
         */
        public Builder endDate(final Date endDate) {
            this.endDate = Objects.nonNull(endDate) ? new Date(endDate.getTime()) : null;
            return this;
        }

        /**
         * Pagination data builder.
         *
         * @param paginationData
         *         the pagination data
         * @return the builder
         */
        public Builder paginationData(final PaginationData paginationData) {
            this.paginationData = paginationData;
            return this;
        }

        /**
         * Target builder.
         *
         * @param target
         *         the target
         * @return the builder
         */
        public Builder target(final String target) {
            this.target = target;
            return this;
        }

        /**
         * Events severity builder.
         *
         * @param eventsSeverity
         *         the events severity
         * @return the builder
         */
        public Builder eventsSeverity(final List<FlowExecutionEventSeverity> eventsSeverity) {
            this.eventsSeverity = Objects.nonNull(eventsSeverity) ? Collections.unmodifiableList(eventsSeverity) : null;
            return this;
        }

        /**
         * Sort data builder.
         *
         * @param sortData
         *         the sort data
         * @return the builder
         */
        public Builder sortData(final SortData sortData) {
            this.sortData = sortData;
            return this;
        }

        /**
         * Build flow execution event filter.
         *
         * @return the flow execution event filter
         */
        public FlowExecutionEventFilter build() {
            return new FlowExecutionEventFilter(flowId, flowExecutionName, target, eventsSeverity, startDate, endDate, message, paginationData, sortData);
        }
    }
}