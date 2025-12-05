package com.cspdog.utils;

import com.cspdog.configuration.CSPConfigurationHolder;
import com.cspdog.filter.CSPHeaders;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CSPUtilsTest {

    @Test
    public void testGetCSPHeaders() {
        CSPConfigurationHolder.readCSPConfiguration();
        String cspedResponse = "<html><body onclick=\"myFunction()\"><p style=\"color:red;\">Hello</p></body></html>";
        String nonce = "nonce-123";
        CSPHeaders headers = CSPUtils.getCSPHeaders(cspedResponse, nonce);

        assertTrue(headers.ENFORCED_POLICY.contains(nonce));
        assertTrue(headers.REPORT_ONLY_POLICY.contains(nonce));

        String eventHandlerHashes = CSPUtils.getEventHandlerHashes(cspedResponse);
        assertTrue(headers.ENFORCED_POLICY.contains(eventHandlerHashes));
        assertTrue(headers.REPORT_ONLY_POLICY.contains(eventHandlerHashes));

        String styleHashes = getInlinedStyleHashes(cspedResponse);
        assertTrue(headers.ENFORCED_POLICY.contains(styleHashes));
        assertTrue(headers.REPORT_ONLY_POLICY.contains(styleHashes));
    }

    @Test
    public void testGetEventHandlerHashes() {
        String html = "<div onclick=\"doSomething()\" onmouseover=\"doSomethingElse()\"></div>";
        String hashes = CSPUtils.getEventHandlerHashes(html);
        assertEquals("'SHA256-R3B3/rKdLEsrff7b4FBPDftRf5CqOX4DQwhWurRtw28=' 'SHA256-bnQkgwAfjTxnZSlFxZe1ogJadBHLnRuuL54WC+v+tMY=' ", hashes);
    }

    @Test
    public void testGetEventHandlerHashes_NoHandlers() {
        String html = "<div><p>No event handlers here.</p></div>";
        String hashes = CSPUtils.getEventHandlerHashes(html);
        assertEquals("", hashes);
    }

    @Test
    public void testGetInlinedStyleHashes() {
        String html = "<p style=\"color: blue; font-size: 12px;\"></p>";
        String hashes = getInlinedStyleHashes(html);
        assertTrue(hashes.contains("SHA256-A+ItDGS5NiYzaVZpo6WJ3+5z11h9nvWJmneA/ci5bgQ="));
    }

    @Test
    public void testGetInlinedStyleHashes_NoStyles() {
        String html = "<p>No inline styles here.</p>";
        String hashes = getInlinedStyleHashes(html);
        assertEquals("", hashes);
    }

    @Test
    public void testGenerateCSPHashForInlinedElement() throws Exception {
        Method method = CSPUtils.class.getDeclaredMethod("generateCSPHashForInlinedElement", String.class);
        method.setAccessible(true);

        String script = "myFunction()";
        String expectedHash = "SHA256-DLsspdO65egz8GmJ7gT7AHNirrsTFzKLBvF8tcsc82Y=";
         String actualHash = (String) method.invoke(null, script);
         assertEquals(expectedHash, actualHash);
    }

    // Helper method to access private getInlinedStyleHashes for testing
    private String getInlinedStyleHashes(String html) {
        try {
            Method method = CSPUtils.class.getDeclaredMethod("getInlinedStyleHashes", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, html);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
