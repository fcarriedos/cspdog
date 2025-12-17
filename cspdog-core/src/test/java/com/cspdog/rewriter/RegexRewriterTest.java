package com.cspdog.rewriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegexRewriterTest {

    private RegexRewriter rewriter;

    @Mock
    private jakarta.servlet.http.HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        rewriter = new RegexRewriter();
    }

    @Test
    public void testGetCSPedResponseWithDoubleQuotedHref() {
        String input = "<html><body><a href=\"javascript:alert('Hello');\">Click me</a></body></html>";
        String expected = "<html><body><a href=\"#\" onclick=\"alert('Hello');\">Click me</a></body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }


    @Test
    public void testGetCSPedResponseWithDoubleQuotedHref_SeveralInvocations() {
        String input = "<html><body><a href=\"javascript:alert('Hello');console.log('I said hello!');\">Click me</a></body></html>";
        String expected = "<html><body><a href=\"#\" onclick=\"alert('Hello');console.log('I said hello!');\">Click me</a></body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCSPedResponseWithSingleQuotedHref() {
        String input = "<html><body><a href='javascript:alert(\"Hello\");'>Click me</a></body></html>";
        String expected = "<html><body><a href=\"#\" onclick=\"alert('Hello');\">Click me</a></body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCSPedResponseWithSingleQuotedHref_SeveralInvocations() {
        String input = "<html><body><a href='javascript:alert(\"Hello\");console.log(\"I said hello!\");'>Click me</a></body></html>";
        String expected = "<html><body><a href=\"#\" onclick=\"alert('Hello');console.log('I said hello!');\">Click me</a></body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCSPedResponseWithEmptyJavascriptHref() {
        String input = "<html><body><a href=\"javascript:\">Click me</a></body></html>";
        String expected = "<html><body><a href=\"#\">Click me</a></body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCSPedResponseWithVoid0() {
        String input = "<html><body><a href='javascript:void(0)'>Click me</a></body></html>";
        String expected = "<html><body><a href=\"#\">Click me</a></body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCSPedResponseWithSrcJavascriptFalse() {
        String input = "<html><body><iframe src=\"javascript:false\"></iframe></body></html>";
        String expected = "<html><body><iframe src=\"about:blank\"></iframe></body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCSPedResponseWithMixedContent() {
        String input = "<html><body>" +
                "<a href=\"javascript:alert('Hello');\">Click me</a>" +
                "<iframe src=\"javascript:false\"></iframe>" +
                "<a href='javascript:void(0)'>Do nothing</a>" +
                "</body></html>";
        String expected = "<html><body>" +
                "<a href=\"#\" onclick=\"alert('Hello');\">Click me</a>" +
                "<iframe src=\"about:blank\"></iframe>" +
                "<a href=\"#\">Do nothing</a>" +
                "</body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCSPedResponseWithNoJavascript() {
        String input = "<html><body><a href=\"/index.html\">Home</a></body></html>";
        String expected = "<html><body><a href=\"/index.html\">Home</a></body></html>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(expected, actual);
    }

    @Test
    public void testInjectNonce() {
        String input = "<html><head><script>console.log('hello');</script></head><body></body></html>";
        String actual = rewriter.injectNonce(input, "nonce-123");
        String expected = "<html><head><script nonce=\"nonce-123\" >console.log('hello');</script></head><body></body></html>";
        assertEquals(actual, expected);
    }

    @Test
    public void testProcessPartialResponseStyles() {
        String input = "<partial-response><changes><update id=\"form:j_idt12\"><![CDATA[<div id=\"form:j_idt12\" style=\"color:red;\">Partial content</div><div id=\"form:j_idt12\" style=\"color:blue;\">More partial content</div>]]></update></changes></partial-response>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertTrue(actual.contains("<style type=\"text/css\" nonce=\"nonce-123\">"));
        assertTrue(actual.contains(".cspdog-"));
        assertTrue(actual.contains("<div id=\"form:j_idt12\" class=\"cspdog-style-"));
    }

    @Test
    public void testProcessPartialResponseStyles_NoStyles() {
        String input = "<partial-response><changes><update id=\"form:j_idt12\"><![CDATA[<div id=\"form:j_idt12\">Partial content</div>]]></update></changes></partial-response>";
        String actual = rewriter.getCSPedResponse(input, "nonce-123");
        assertEquals(input, actual);
    }
}
