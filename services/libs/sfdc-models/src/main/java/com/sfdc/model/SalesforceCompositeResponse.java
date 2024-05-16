package com.sfdc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceCompositeResponse {
    private List<SalesforceResponse> compositeResponse;
}
