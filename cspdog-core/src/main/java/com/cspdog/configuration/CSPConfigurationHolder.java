package com.cspdog.configuration;

import org.apache.commons.lang3.StringUtils;

public class CSPConfigurationHolder {

    // Hook into startup lifecycle, read configuration from different sources:
    // - Config file
    // - Environment variable
    // - Configuration server
    // - Passed as a parameter
    // - Custom passing a lambda
    // - ...
    // Runs periodically, so configuration can be hot-reloaded
    public static void readCSPConfiguration() {
        CSPConfiguration.ENFORCED_POLICY = "script-src 'self' 'nonce-{nonce}' 'unsafe-eval' 'unsafe-hashes' {eventhandlerhashes} ; style-src 'unsafe-hashes' {inlinestyleshashes} ;";
        CSPConfiguration.REPORT_ONLY_POLICY = "script-src 'self' 'nonce-{nonce}' 'unsafe-eval' 'unsafe-hashes' {eventhandlerhashes} ; style-src 'unsafe-hashes' {inlinestyleshashes} ;";
    }

    public static void setConfiguration(CSPConfiguration configuration) {
        // TODO: hot-reload configuration
    }

    public static boolean isCSPConfigured() {
        return StringUtils.isNotBlank(CSPConfiguration.ENFORCED_POLICY) &&
                StringUtils.isNotBlank(CSPConfiguration.REPORT_ONLY_POLICY);
    }

}
