package com.cspdog.filter;

import com.cspdog.configuration.CSPConfiguration;

public class CSPHeaders {
    public String ENFORCED_POLICY;
    public String REPORT_ONLY_POLICY;

    public CSPHeaders() {
        this.ENFORCED_POLICY = CSPConfiguration.ENFORCED_POLICY;
        this.REPORT_ONLY_POLICY = CSPConfiguration.REPORT_ONLY_POLICY;
    }
}
