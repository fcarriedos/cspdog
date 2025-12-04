package com.cspdog.rewriter;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;

import static com.cspdog.utils.Constants.*;

public class RegexRewriter implements Rewriter {

    private static final Logger logger = LoggerFactory.getLogger(
            RegexRewriter.class);

    public String getCSPedResponse(String regularHTMLResponse, String nonce) {
        return injectNonce(processHREFJavascript(regularHTMLResponse), nonce);
    }

    @Override
    public String injectNonce(String preNonceResponse, String nonce) {
        if (noProcessingNeeded(preNonceResponse, nonce)) {
            logger.trace("injectNonce(): skipping {} for nonce injection", preNonceResponse);
            return preNonceResponse;
        }
        logger.trace("injectNonce(): Pre-CSP-Processing HTML output {}", preNonceResponse);
        logger.debug("injectNonce(): inserting nonce {} in response", nonce);
        final String nonceSegment = NONCE_OPENING + nonce + CLOSING_QUOTE_SPACED;
        Matcher patternMatcher = NONCE_PATTERN.matcher(preNonceResponse);
        StringBuilder sb = new StringBuilder();
        while (patternMatcher.find()) {
            patternMatcher.appendReplacement(sb, addNoncesToScriptBlocks(patternMatcher.group(), nonceSegment));
        }
        patternMatcher.appendTail(sb);

        String noncedResponse = sb.toString();
        logger.debug("injectNonce(): nonce {} inserted in response", nonce);
        return noncedResponse;
    }

    private static String addNoncesToScriptBlocks(String unNoncedScriptTag, String nonce) {
        if (noProcessingNeeded(unNoncedScriptTag, nonce)) {
            return unNoncedScriptTag;
        }
        // Optimized into a StringBuilder by the JVM
        return unNoncedScriptTag.substring(0, unNoncedScriptTag.length() - 1) + nonce + unNoncedScriptTag.charAt(unNoncedScriptTag.length() - 1);
    }

    private static boolean noProcessingNeeded(String tag, String nonce) {
        logger.trace("noProcessingNeeded(): skipping nonce adding for {}", tag);
        return StringUtils.isBlank(tag) || StringUtils.isBlank(nonce);
    }


    @Override
    public String getHashesForEventHandlers(String regularHTMLResponse) {
        return null;
    }

    @Override
    public String getCSPHeaders(String nonce) {
        return "script-src 'self' 'nonce-{nonce}' 'unsafe-inline' 'unsafe-eval';".replaceAll("\\{nonce}", nonce);

    }

    private static String processHREFJavascript(String preCSPResponse) {
        String processedResponse = HREF_JAVASCRIPT_PATTERN.matcher(preCSPResponse).replaceAll(matchResult -> {
            String doubleQuotedInvocation = matchResult.group(1);
            String singleQuotedInvocation = matchResult.group(2);

            if (StringUtils.isNotBlank(doubleQuotedInvocation)) {
                // href="javascript:alert('Hello');" -> onclick="alert('Hello')"
                return transformHREFIntoOnclickEventHandler(doubleQuotedInvocation);
            }
            if (StringUtils.isNotBlank(singleQuotedInvocation)) {
                // href='javascript:alert("Hello");' -> onclick="alert('Hello')"
                return transformHREFIntoOnclickEventHandler(singleQuotedInvocation.replaceAll(DOUBLE_QUOTES, SINGLE_QUOTE));
            }
            // href="javascript:" -> href="#"
            return transformHREFIntoOnclickEventHandler(null);
        });
        return processedResponse.replaceAll(SRC_JAVASCRIPT_FALSE_REGEX, SRC_ABOUT_BLANK);
    }

    private static String transformHREFIntoOnclickEventHandler(String scriptContent) {
        if (StringUtils.isBlank(scriptContent) || VOID0_PATTERN.matcher(scriptContent).matches()) return HREF_ANCHOR;
        return new StringBuilder(HREF_ANCHOR).append(BLANK_SPACE).append(ONCLICK).append(OPENING_EQUALS).append(scriptContent).append(DOUBLE_QUOTES).toString();
    }

}
