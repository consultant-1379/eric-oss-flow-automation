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

package com.ericsson.oss.services.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.Dependent;
import javax.naming.InitialContext;

/**
 * Wrapper around TimerService provided by JEE environment. This way we intercept all invocations to TimerService and we can do logging etc.
 */
@Dependent
public class TimerServiceWrapper implements TimerService {

    private TimerService ts;

    private TimerService getTimerService() {
        if (ts != null) {
            return ts;
        }
        try {
            final InitialContext ctx = new InitialContext();
            ts = (TimerService) ctx.lookup("java:comp/TimerService");
        } catch (final Exception exc) {
            throw new IllegalStateException("Unable to find TimerService. Details: " + exc);
        }
        return ts;
    }

    @Override
    public Timer createCalendarTimer(final ScheduleExpression arg0) {
        return getTimerService().createCalendarTimer(arg0);
    }

    @Override
    public Timer createCalendarTimer(final ScheduleExpression arg0, final TimerConfig arg1) {
        return getTimerService().createCalendarTimer(arg0, arg1);
    }

    @Override
    public Timer createIntervalTimer(final long arg0, final long arg1, final TimerConfig arg2) {
        return getTimerService().createIntervalTimer(arg0, arg1, arg2);
    }

    @Override
    public Timer createIntervalTimer(final Date arg0, final long arg1, final TimerConfig arg2) {
        return getTimerService().createIntervalTimer(arg0, arg1, arg2);
    }

    @Override
    public Timer createSingleActionTimer(final long arg0, final TimerConfig arg1) {
        return getTimerService().createSingleActionTimer(arg0, arg1);
    }

    @Override
    public Timer createSingleActionTimer(final Date arg0, final TimerConfig arg1) {
        return getTimerService().createSingleActionTimer(arg0, arg1);
    }

    @Override
    public Timer createTimer(final long arg0, final Serializable arg1) {
        return getTimerService().createTimer(arg0, arg1);
    }

    @Override
    public Timer createTimer(final Date arg0, final Serializable arg1) {
        return getTimerService().createTimer(arg0, arg1);
    }

    @Override
    public Timer createTimer(final long arg0, final long arg1, final Serializable arg2){
        return getTimerService().createTimer(arg0, arg1, arg2);
    }

    @Override
    public Timer createTimer(final Date arg0, final long arg1, final Serializable arg2) {
        return getTimerService().createTimer(arg0, arg1, arg2);
    }

    @Override
    public Collection<Timer> getTimers()  {
        return getTimerService().getTimers();
    }

    @Override
    public Collection<Timer> getAllTimers() {
        return getTimerService().getAllTimers();

    }

}
