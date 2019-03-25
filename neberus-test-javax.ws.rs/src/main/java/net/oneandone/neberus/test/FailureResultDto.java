package net.oneandone.neberus.test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generic DTO for successful executions with a failed result.
 */
public class FailureResultDto {
    
    enum Resp {
        OK, OR_NOT_OK, WHATEVER
    }

    public final boolean success;

    public final String message;

    public final String dateTime;
    
    public final Resp resp;

    public static FailureResultDto withMessage(String message) {
        return new FailureResultDto(message);
    }

    private FailureResultDto(String message) {
        this.success = false;
        this.dateTime = stringFor(OffsetDateTime.now());
        this.message = message;
        this.resp = Resp.OR_NOT_OK;
    }

    private String stringFor(OffsetDateTime now) {
        return now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
