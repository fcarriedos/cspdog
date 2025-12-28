package com.cspdog.rewriter;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cspdog.utils.Constants.*;

public class RegexRewriter implements Rewriter {

    private static final Logger logger = LoggerFactory.getLogger(
            RegexRewriter.class);

    public String getCSPedResponseBody(String regularHTMLResponse, String nonce) {
        return processPartialResponses(injectNonce(processHREFJavascript(regularHTMLResponse), nonce), nonce);
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


    private static String processPartialResponses(String htmlOutput, String nonce) {
        if (StringUtils.isBlank(htmlOutput) || StringUtils.isBlank(nonce)) {
            return htmlOutput;
        }
        if (!INLINED_STYLE_PATTERN.matcher(htmlOutput).find()) {
            return htmlOutput;
        }
        if (htmlOutput.contains(PARTIAL_RESPONSE_ATTRIBUTE)) {
            return processPartialResponseStyles(htmlOutput, nonce);
        }
        return htmlOutput;
    }

    private static String processPartialResponseStyles(String htmlOutput, String nonce) {
        // The regex pattern for extracting CDATA content
        Pattern pattern = Pattern.compile(CDATA_BLOCK_REGEX);

        // The Matcher object to find all occurrences
        Matcher matcher = pattern.matcher(htmlOutput);
        StringBuffer resultBuffer = new StringBuffer();

        while (matcher.find()) {
            // group(1) contains the content of the first capturing group
            String cdataContent = matcher.group(1);
            String allClassyNodes = processBlocksWithInlineStyles(cdataContent, nonce);
            String replacement = CDATA_OPENING + allClassyNodes + CDATA_CLOSING;
            matcher.appendReplacement(resultBuffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resultBuffer);
        return resultBuffer.toString();
    }

    private static String processBlocksWithInlineStyles(String htmlBlock, String nonce) {
        StringBuilder sb = new StringBuilder(STYLE_BLOCK_OPENING_NONCED + nonce + STYLE_BLOCK_OPENING_CLOSURE);
        Document content = Jsoup.parse(htmlBlock, EMPTY_CHAR, Parser.xmlParser());
        Elements allStylishNodes = content.select(STYLE_DOM_SELECTOR);
        if (allStylishNodes.isEmpty()) {
            logger.debug("processBlocksWithInlineStyles(): no style found, returning HTML as-is, avoiding JSoup processing");
            return htmlBlock;
        }
        int i = 0;
        Element firstStylishNode = null;
        for (Element stylishNode : allStylishNodes) {
            // Transform nodes with inlined styles
            if (i == 0) firstStylishNode = stylishNode;
            transformNodeWithInlineStyle(stylishNode, sb);
            // Prepending them, to be available ahead
            // of browser evaluation of upcoming nodes
            if (i == allStylishNodes.size() - 1) {
                sb.append(STYLE_CLOSURE);
                firstStylishNode.before(sb.toString());
            }
            i++;
        }
        return content.html();
    }

    private static void transformNodeWithInlineStyle(Element stylishNode, StringBuilder sb) {
        String newCSSClassName = CSPDOG_STYLE_PREFIX + Math.abs(random.nextLong());
        sb.append(DOT).append(newCSSClassName)
                .append(OPENING_CURLY_BRACE)
                .append(stylishNode.attr(STYLE))
                .append(CLOSING_CURLY_BRACE);
        stylishNode.removeAttr(STYLE);
        stylishNode.addClass(newCSSClassName);
    }

}
