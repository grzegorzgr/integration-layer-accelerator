package com.sfdc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressFBWarnings("URF_UNREAD_FIELD")
public class SalesforceResponse {
    /**
     * SFDC returns body as a single object or array, for now we just toString it.
     */
    private Object body;
    private SfdcHttpHeaders httpHeaders;
    private int httpStatusCode;
    private String referenceId;

    @NoArgsConstructor
    @AllArgsConstructor
    public static class SfdcHttpHeaders {
        @JsonProperty("Location")
        private String location;
    }
}
