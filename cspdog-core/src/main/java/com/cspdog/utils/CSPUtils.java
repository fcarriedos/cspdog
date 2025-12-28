package com.cspdog.utils;

import com.cspdog.filter.CSPHeaders;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import static com.cspdog.utils.Constants.*;

public class CSPUtils {

    private static final Logger logger = LoggerFactory.getLogger(CSPUtils.class);

    private static CSPHeaders getResponseHeaders(CSPedResponseBodyHolder<String> cspedResponseBody, String nonce, HttpServletResponse response) {
        CSPHeaders cspHeaders = new CSPHeaders();
        cspHeaders.ENFORCED_POLICY = cspHeaders.ENFORCED_POLICY
                .replaceAll("\\{nonce}", nonce)
                .replaceAll("\\{eventhandlerhashes}", getEventHandlerHashes(cspedResponseBody.get()))
                .replaceAll("\\{inlinestyleshashes}", getInlinedStyleHashes(cspedResponseBody.get()));
        cspHeaders.REPORT_ONLY_POLICY = cspHeaders.REPORT_ONLY_POLICY.replaceAll("\\{nonce}", nonce)
                .replaceAll("\\{nonce}", nonce)
                .replaceAll("\\{eventhandlerhashes}", getEventHandlerHashes(cspedResponseBody.get()))
                .replaceAll("\\{inlinestyleshashes}", getInlinedStyleHashes(cspedResponseBody.get()));
        return cspHeaders;
    }

    private static String getEventHandlerHashes(String htmlOutput) {
        if (StringUtils.isBlank(htmlOutput)) {
            return StringUtils.EMPTY;
        }
        Matcher eventAttributeMatcher = TAGS_WITH_EVENT_HANDLERS_PATTERN.matcher(htmlOutput);
        Set<String> handlerHashes = new HashSet<>();
        while (eventAttributeMatcher.find()) {
            String eventAttributeMatch = eventAttributeMatcher.group(0);
            Matcher invocationMatcher = EVENT_HANDLER_INVOCATION_PATTERN.matcher(eventAttributeMatch);
            logger.trace("getEventHandlerHashes(): event handler {} ", eventAttributeMatch);
            // There could be more than one event attribute, e.g.: onclick="..." onblur="..."
            while (invocationMatcher.find()) {
                String invocationMatch = invocationMatcher.group(2);
                logger.trace("getEventHandlerHashes(): invocation {} ", invocationMatch);
                String correspondingHash = generateCSPHashForInlinedElement(invocationMatch);
                logger.trace("getEventHandlerHashes(): corresponding hash {}", correspondingHash);
                // The line below appends hashes single quoted, e.g.:
                // 'sha256-gbbBOORvGXZmIA6BOnpHAMAWTvCqLZu3ENf57MWLd+Y='
                StringBuilder sb = new StringBuilder();
                sb.append(SINGLE_QUOTE).append(correspondingHash).append(SINGLE_QUOTE).append(WHITE_SPACE_SEPARATOR);
                handlerHashes.add(sb.toString());
            }
        }
        return StringUtils.join(handlerHashes, StringUtils.EMPTY);
    }

    private static String getInlinedStyleHashes(String htmlOutput) {
        if (StringUtils.isEmpty(htmlOutput)) {
            return StringUtils.EMPTY;
        }
        if (!INLINED_STYLE_PATTERN.matcher(htmlOutput).find()) {
            return StringUtils.EMPTY;
        }
        Matcher inlineStyleAttributeMatcher = INLINED_STYLE_PATTERN.matcher(htmlOutput);
        Set<String> inlineStyleHashes = new HashSet<>();
        while (inlineStyleAttributeMatcher.find()) {
            String inlineStyleMatch = inlineStyleAttributeMatcher.group(2);
            logger.trace("getInlinedStyleHashes(): detected style attribute {} ", inlineStyleMatch);
            String correspondingHash = generateCSPHashForInlinedElement(inlineStyleMatch);
            logger.trace("getInlinedStyleHashes(): corresponding hash {}", correspondingHash);
            StringBuilder sb = new StringBuilder();
            sb.append(SINGLE_QUOTE).append(correspondingHash).append(SINGLE_QUOTE).append(WHITE_SPACE_SEPARATOR);
            inlineStyleHashes.add(sb.toString());
        }
        return StringUtils.join(inlineStyleHashes, StringUtils.EMPTY);
    }

    /**
     * Calculates the SHA256 hash of a given string (namely an event handler invocation or an inlined style), the CSP way, e.g.:
     * Examples:
     * - If inlinedElement value is sayHi(); (extracted by the invoker from onclick="sayHi();") this method
     * will return the SHA-256 hash corresponding to sayHi(); -> gbbBOORvGXZmIA6BOnpHAMAWTvCqLZu3ENf57MWLd+Y=
     * - If inlinedElement value is font-family: Verdana; color: green; (extracted by the invoker from an inlined style attribute)
     * this method will return Hn7UXd9eddwUHOboQALO8jCkSiOuoWVjc/3b/4JTsL4=
     */
    private static String generateCSPHashForInlinedElement(String inlinedElement) {
        if  (StringUtils.isEmpty(inlinedElement)) return inlinedElement;
        try {
            MessageDigest digest = MessageDigest.getInstance(HASHING_ALGORITHM);
            // Avoid producing the wrong hashes due to already encoded HTML
            inlinedElement = StringEscapeUtils.unescapeHtml4(inlinedElement);
            byte[] hash = digest.digest(inlinedElement.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(HASHING_ALGORITHM_PREFIX);
            hexString.append(Base64.getEncoder().encodeToString(hash));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("generateCSPHashForInlinedElement(): {} hashing algorithm not found: {}", HASHING_ALGORITHM, e.getMessage());
            return StringUtils.EMPTY;
        }
    }

    public static void setPolicyInResponse(CSPedResponseBodyHolder<String> cspedResponseBodyHolder, String nonce, HttpServletResponse res) {
        CSPHeaders cspHeaders = CSPUtils.getResponseHeaders(cspedResponseBodyHolder, nonce, res);
        setEnforcedHeaders(cspedResponseBodyHolder, cspHeaders, res);
        setReportOnlyHeaders(cspHeaders, res);
    }

    private static void setEnforcedHeaders(CSPedResponseBodyHolder<String> cspedResponseBodyHolder, CSPHeaders cspHeaders, HttpServletResponse res) {
        if (isPolicyNotEmptyAndUnderTheLimit(cspHeaders.ENFORCED_POLICY)) {
            res.setHeader(ENFORCED_POLICY_HEADER_NAME, cspHeaders.ENFORCED_POLICY);
            return;
        }
        logger.warn("setEnforcedHeaders(): the Content-Security-Policy header size ({} bytes) exceeds the current limit ({} bytes), refusing to apply it to avoid application failures");
        // TODO: report this separately to CSPDog Reporting Servers
        // Insert the policy as a meta-tag in the body
        cspedResponseBodyHolder
                .set(cspedResponseBodyHolder
                .get()
                .replaceFirst("<head[^>]*>", "$0\n<meta http-equiv=\"Content-Security-Policy\" content=\"" + cspHeaders.ENFORCED_POLICY +  "\" />"));
    }

    private static void setReportOnlyHeaders(CSPHeaders cspHeaders, HttpServletResponse res) {
        if (isPolicyNotEmptyAndUnderTheLimit(cspHeaders.REPORT_ONLY_POLICY)) {
            res.setHeader(REPORT_ONLY_POLICY_HEADER_NAME, cspHeaders.REPORT_ONLY_POLICY);
            return;
        }
        // Removing the hashes to fail-fast but preventing application failure,
        // this needs further revision by either the AppSec team or the CSPDog agent
        logger.warn("setReportOnlyHeaders(): the Content-Security-Policy-Report-Only header size ({} bytes) exceeds the current limit ({} bytes), refusing to apply it to avoid application failures");
        // TODO: report this separately to CSPDog Reporting Servers
        res.setHeader(REPORT_ONLY_POLICY_HEADER_NAME, cspHeaders.REPORT_ONLY_POLICY
                .replace(UNSAFE_HASHES, EMPTY_CHAR)
                .replaceAll(INVOCATION_HASHES_PLACEHOLDER_REGEX, EMPTY_CHAR)
                .replaceAll(INLINE_STYLE_HASHES_PLACEHOLDER_REGEX, EMPTY_CHAR));
    }

    private static boolean isPolicyNotEmptyAndUnderTheLimit(String policy) {
        return StringUtils.isNotEmpty(policy) && policy.length() < MAX_CSP_HEADER_SIZE;
    }

}
