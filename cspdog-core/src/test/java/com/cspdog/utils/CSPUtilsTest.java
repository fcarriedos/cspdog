package com.cspdog.utils;

import com.cspdog.configuration.CSPConfigurationHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CSPUtilsTest {

    @Test
    public void testGetResponseHeaders() {
        CSPConfigurationHolder.readCSPConfiguration();
        CSPedResponseBodyHolder<String> cspedResponseBodyHolder = new CSPedResponseBodyHolder<>("<html><body onclick=\"myFunction()\"><p style=\"color:red;\">Hello</p></body></html>");
        String nonce = "nonce-123";
        HttpServletResponse response = mock(HttpServletResponse.class);
        CSPUtils.setPolicyInResponse(cspedResponseBodyHolder, nonce, response);
        verify(response).setHeader(Constants.ENFORCED_POLICY_HEADER_NAME, "script-src 'self' 'nonce-nonce-123' 'unsafe-eval' 'unsafe-hashes' 'SHA256-DLsspdO65egz8GmJ7gT7AHNirrsTFzKLBvF8tcsc82Y='  ; style-src 'unsafe-hashes' 'SHA256-BQ5eA/mw6jES31KSfh/A55TC7nzftLBWpZBzzDfwUrA='  ;");
        verify(response).setHeader(Constants.REPORT_ONLY_POLICY_HEADER_NAME, "script-src 'self' 'nonce-nonce-123' 'unsafe-eval' 'unsafe-hashes' 'SHA256-DLsspdO65egz8GmJ7gT7AHNirrsTFzKLBvF8tcsc82Y='  ; style-src 'unsafe-hashes' 'SHA256-BQ5eA/mw6jES31KSfh/A55TC7nzftLBWpZBzzDfwUrA='  ;");
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

    /**
     * This test verifies the behavior when both policies exceed the MAX_CSP_HEADER_SIZE.
     * It relies on the default configuration providing similar base policies for both enforced and report-only.
     * To test scenarios where only one policy is oversized, the CSP configuration
     * (e.g., in csp.properties) would need to be adjusted to create a significant length difference
     * between the enforced and report-only base policies.
     */
    @Test
    public void testSetPolicyInResponse_PoliciesExceedMaxSize() {
        CSPConfigurationHolder.readCSPConfiguration();

        // Generate a large HTML body with many inline styles to make the policies oversized.
        // Each style adds a hash ('sha256-...' ) of about 47 bytes. 200 styles will add ~9400 bytes.
        String htmlBody = generateHtmlWithManyStyles(200);
        CSPedResponseBodyHolder<String> cspedResponseBodyHolder = new CSPedResponseBodyHolder<>(htmlBody);

        String nonce = "nonce-123";
        HttpServletResponse response = mock(HttpServletResponse.class);

        CSPUtils.setPolicyInResponse(cspedResponseBodyHolder, nonce, response);

        // Verify ENFORCED policy: header should not be set, and a meta tag should be added to the body.
        verify(response, never()).setHeader(eq(Constants.ENFORCED_POLICY_HEADER_NAME), anyString());
        String modifiedBody = cspedResponseBodyHolder.get();
        assertTrue(modifiedBody.contains("<meta http-equiv=\"Content-Security-Policy\" content=\""));
        assertTrue(modifiedBody.length() > htmlBody.length());

        // Verify REPORT-ONLY policy: a modified (shortened) header should be set.
        ArgumentCaptor<String> reportOnlyCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(Constants.REPORT_ONLY_POLICY_HEADER_NAME), reportOnlyCaptor.capture());
        String reportOnlyHeader = reportOnlyCaptor.getValue();

        // The fallback logic removes 'unsafe-hashes' and tries to remove placeholders.
        // The placeholders are already replaced, so only 'unsafe-hashes' is removed.
        // We verify that the header was shortened by checking that 'unsafe-hashes' is gone.
        assertFalse(reportOnlyHeader.contains(Constants.UNSAFE_HASHES));
        // The test policies from testGetResponseHeaders have 'unsafe-hashes' so this should pass.
        assertTrue(reportOnlyHeader.length() > 0);
    }

    private String generateHtmlWithManyStyles(int count) {
        StringBuilder sb = new StringBuilder("<html><head></head><body>");
        for (int i = 0; i < count; i++) {
            // Unique style to ensure unique hash generation
            sb.append("<p style=\"padding: ").append(i).append("px;\"></p>");
        }
        sb.append("</body></html>");
        return sb.toString();
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
