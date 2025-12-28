package com.cspdog.filter;

import com.cspdog.configuration.CSPConfigurationHolder;
import com.cspdog.rewriter.RegexRewriter;
import com.cspdog.utils.CSPUtils;
import com.cspdog.utils.CSPedResponseBodyHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Base64;

import static com.cspdog.utils.Constants.ENFORCED_POLICY_HEADER_NAME;
import static com.cspdog.utils.Constants.REPORT_ONLY_POLICY_HEADER_NAME;

public class CSPDogServletFilter extends HttpFilter {

    private static final Logger logger = LoggerFactory.getLogger(CSPDogServletFilter.class);

    private static final long serialVersionUID = 1L;
    private static final RegexRewriter cspRewriter = new RegexRewriter();
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void init() {
        // Initialize  the config
        CSPConfigurationHolder.readCSPConfiguration();
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {

        if (!CSPConfigurationHolder.isCSPConfigured()) {
            chain.doFilter(req, res);
        }

        CSPDogResponseWrapper responseWrapper = new CSPDogResponseWrapper((HttpServletResponse) res);
        chain.doFilter(req, responseWrapper);

        // TODO: accounting for memory footprint, this should be configurable to be used only in test environments
        String originalResponse = responseWrapper.toString();
        PrintWriter responseWriter = res.getWriter();
        try {
            String nonce = getNonce();
            CSPedResponseBodyHolder<String> cspedResponseBodyHolder = new CSPedResponseBodyHolder<>(cspRewriter.getCSPedResponseBody(originalResponse, nonce));
            CSPUtils.setPolicyInResponse(cspedResponseBodyHolder, nonce, res);
            logger.trace("doFilter(): sending CSP-processed response {}", cspedResponseBodyHolder.get());
            responseWriter.write(cspedResponseBodyHolder.get());
        } catch (Exception e) {
            // Log and fail gracefully avoiding application failures
            // TODO: this would be the right point to send alerts on failures
            logger.error("doFilter(): exception happened, details {}", e.getMessage());
            // The next best thing to be done is to send the response with no CSP processing
            logger.warn("doFilter(): sending CSP-unprocessed response as a fallback");
            res.setHeader(ENFORCED_POLICY_HEADER_NAME, null);
            res.setHeader(REPORT_ONLY_POLICY_HEADER_NAME, null);
            responseWriter.write(originalResponse);
        }

    }

    private String getNonce() {
        byte[] nonceBytes = new byte[16]; // 16 bytes for a 128-bit nonce
        secureRandom.nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }

}
