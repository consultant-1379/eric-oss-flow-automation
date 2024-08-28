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

package com.ericsson.oss.services.flowautomation.jpa;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Utility class to get ThreadSafe formatted DateTime in UTC.
 */
public class DateTimeUtil {

    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITHOUT_TZ = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /* we can maybe revist this, we kept in due to method in UserTaskSchemaProcessor.java */
    public static final DateTimeFormatter FA_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITH_MILLI_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITH_TZ = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_SQL = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
    public static final DateTimeFormatter FORMATTER_ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter DATE_TIME_FORMATTER_UTC_ISO_8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final DateTimeFormatter ISO_RFC822_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private static final DateTimeFormatter DATE_TIME_FORMATTER_UTC = DATE_TIME_FORMATTER_WITHOUT_TZ.withZone(ZoneId.of("UTC"));
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private DateTimeUtil() {
        //
    }

    public static LocalDateTime convert(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * @return formatted timestamp in UTC
     */
    public static Timestamp getTimeStamp() {
        return Timestamp.valueOf(DATE_TIME_FORMATTER_UTC.format(Instant.now()));
    }

    public static Timestamp getTimeStampWithMilliSeconds() {
        return Timestamp.valueOf(getTimeStamp(DATE_TIME_FORMATTER_WITH_MILLI_SECONDS));
    }

    public static String getTimeStamp(final DateTimeFormatter formatter) {
        return ZonedDateTime.of(LocalDateTime.now(), ZONE_ID).format(formatter);
    }

    public static String changeDateTimeFormat(final String date, final DateTimeFormatter from, final DateTimeFormatter to) {
        return ZonedDateTime.of(LocalDateTime.parse(date, from), ZONE_ID).format(to);
    }

    public static String getIso8601UtcStringFromEpochMilli(long epochMilli) {
        return DATE_TIME_FORMATTER_UTC_ISO_8601.format(ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(epochMilli), ZONE_ID));

    }
}


