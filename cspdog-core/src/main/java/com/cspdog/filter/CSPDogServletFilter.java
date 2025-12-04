package com.cspdog.filter;

import com.cspdog.rewriter.RegexRewriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Base64;

public class CSPDogServletFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;
    private static final RegexRewriter cspRewriter = new RegexRewriter();
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void init() {
        // Initialize  the config
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {

        CSPDogResponseWrapper responseWrapper = new CSPDogResponseWrapper((HttpServletResponse) res);
        chain.doFilter(req, responseWrapper);

        String nonce = getNonce();

        try {
            String cspedResponse = cspRewriter.getCSPedResponse(responseWrapper.toString(), nonce);
            PrintWriter responseWriter = res.getWriter();
            res.setHeader("Content-SecurityPolicy", cspRewriter.getCSPHeaders(nonce));
            responseWriter.write(cspedResponse);
        } catch (Exception e) {
            // Log and fail
        }

    }

    private String getNonce() {
        byte[] nonceBytes = new byte[16]; // 16 bytes for a 128-bit nonce
        secureRandom.nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }

}
