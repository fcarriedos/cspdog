package com.cspdog.utils;

import java.util.regex.Pattern;

public class Constants {

    public static final String HREF_DEEPLINKS_REGEX =
            "href\\s*=\\s*\"javascript:([^\"]*)\"|href\\s*=\\s*'javascript:([^']*)'";
    public static final Pattern HREF_JAVASCRIPT_PATTERN = Pattern.compile(HREF_DEEPLINKS_REGEX, Pattern.CASE_INSENSITIVE);
    public static final String SRC_JAVASCRIPT_FALSE_REGEX = "src\\s*=\\s*\"javascript:\\s*false;?\"" +
            "|href\\s*=\\s*\"javascript:([^\"]*)\"" +
            "|href\\s*=\\s*'javascript:([^']*)'" +
            "|src\\s*=\\s*'javascript:\\s*false;?'" +
            "|src\\s*=\\s*javascript:false;?";
    public static final String SRC_ABOUT_BLANK = "src=\"about:blank\"";
    public static final String VOID0_REGEX = "(?i)void\\(0\\).*";
    public static final Pattern VOID0_PATTERN = Pattern.compile(VOID0_REGEX);
    public static final String HREF_ANCHOR = "href=\"#\"";
    public static final String BLANK_SPACE = " ";
    public static final String ONCLICK = "onclick";
    public static final String OPENING_EQUALS = "=\"";
    public static final String SINGLE_QUOTE = "'";
    public static final String DOUBLE_QUOTES = "\"";
    public static final String NONCE_OPENING = " nonce=\"";
    public static final String CLOSING_QUOTE_SPACED = "\" ";
    public static final String NONCE_REGEX = "<script(?:(?!nonce|src).)*?>|<style(?:(?!nonce).)*?>";
    public static final Pattern NONCE_PATTERN = Pattern.compile(NONCE_REGEX);
    private static final String TAG_WITH_EVENT_HANDLER_REGEX =
            "<[^>]*\\s(on[a-zA-Z]+\\s*=\\s*\"[^\"]*\"|on[a-zA-Z]+\\s*=\\s*'[^']*'|on[a-zA-Z]+\\s*=[^'\"\\s>]*)[^>]*>";
    public static final Pattern TAGS_WITH_EVENT_HANDLERS_PATTERN = Pattern.compile(TAG_WITH_EVENT_HANDLER_REGEX, Pattern.CASE_INSENSITIVE);
    private static final String EVENT_HANDLER_REGEX = "on[a-zA-Z]+\\s*=\\s*(['\\\"])(.*?)\\1";
    public static final Pattern EVENT_HANDLER_INVOCATION_PATTERN = Pattern.compile(EVENT_HANDLER_REGEX, Pattern.CASE_INSENSITIVE);
    private static final String INLINED_STYLE_REGEX = "style\\s*=\\s*(['\"])(.*?)\\1";
    public static final Pattern INLINED_STYLE_PATTERN = Pattern.compile(INLINED_STYLE_REGEX, Pattern.CASE_INSENSITIVE);
    public static final String DASH = "-";
    public static final String HASHING_ALGORITHM = "SHA256";
    public static final String HASHING_ALGORITHM_PREFIX = HASHING_ALGORITHM + DASH;
    public static final char WHITE_SPACE_SEPARATOR = ' ';
    public static final String ENFORCED_POLICY_HEADER_NAME = "Content-Security-Policy";
    public static final String REPORT_ONLY_POLICY_HEADER_NAME = "Content-Security-Policy-Report-Only";
}
