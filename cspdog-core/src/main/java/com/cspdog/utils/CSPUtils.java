package com.cspdog.utils;

import com.cspdog.filter.CSPHeaders;
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

    public static CSPHeaders getCSPHeaders(String cspedResponse, String nonce) {
        CSPHeaders cspHeaders = new CSPHeaders();
        cspHeaders.ENFORCED_POLICY = cspHeaders.ENFORCED_POLICY
                .replaceAll("\\{nonce}", nonce)
                .replaceAll("\\{eventhandlerhashes}", getEventHandlerHashes(cspedResponse))
                .replaceAll("\\{inlinestyleshashes}", getInlinedStyleHashes(cspedResponse));
        cspHeaders.REPORT_ONLY_POLICY = cspHeaders.REPORT_ONLY_POLICY.replaceAll("\\{nonce}", nonce)
                .replaceAll("\\{nonce}", nonce)
                .replaceAll("\\{eventhandlerhashes}", getEventHandlerHashes(cspedResponse))
                .replaceAll("\\{inlinestyleshashes}", getInlinedStyleHashes(cspedResponse));
        return cspHeaders;
    }

    public static String getEventHandlerHashes(String htmlOutput) {
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

}
