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
    public static final String EMPTY_CHAR = "";
    public static final String NONCE_OPENING = " nonce=\"";
    public static final String CLOSING_QUOTE_SPACED = "\" ";
    public static final String NONCE_REGEX = "<script(?:(?!nonce|src).)*?>|<style(?:(?!nonce).)*?>";
    public static final Pattern NONCE_PATTERN = Pattern.compile(NONCE_REGEX);

}
