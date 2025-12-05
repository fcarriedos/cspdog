package com.cspdog.configuration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CSPConfigurationHolderTest {

    @Test
    public void testReadCSPConfiguration() {
        CSPConfigurationHolder.readCSPConfiguration();
        assertTrue(CSPConfigurationHolder.isCSPConfigured());
    }

    @Test
    public void testIsCSPConfigured() {
        CSPConfiguration.ENFORCED_POLICY = null;
        CSPConfiguration.REPORT_ONLY_POLICY = null;
        assertFalse(CSPConfigurationHolder.isCSPConfigured());

        CSPConfiguration.ENFORCED_POLICY = "script-src 'self'";
        CSPConfiguration.REPORT_ONLY_POLICY = null;
        assertFalse(CSPConfigurationHolder.isCSPConfigured());

        CSPConfiguration.ENFORCED_POLICY = null;
        CSPConfiguration.REPORT_ONLY_POLICY = "script-src 'self'";
        assertFalse(CSPConfigurationHolder.isCSPConfigured());

        CSPConfiguration.ENFORCED_POLICY = "script-src 'self'";
        CSPConfiguration.REPORT_ONLY_POLICY = "script-src 'self'";
        assertTrue(CSPConfigurationHolder.isCSPConfigured());
    }
}
