package com.cspdog.filter;

import com.cspdog.configuration.CSPConfiguration;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.cspdog.utils.Constants.ENFORCED_POLICY_HEADER_NAME;
import static com.cspdog.utils.Constants.REPORT_ONLY_POLICY_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CSPDogServletFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private CSPDogServletFilter filter;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        filter = new CSPDogServletFilter();
        filter.init();
    }

    @Test
    public void testDoFilter() throws Exception {

        when(response.getWriter()).thenReturn(getWriter());

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, atLeast(2)).setHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

        assertNotNull(headerValueCaptor.getAllValues().get(0));
        assertNotNull(headerValueCaptor.getAllValues().get(1));
    }

    @Test
    public void testDoFilter_NoCSPConfig() throws Exception {
        CSPConfiguration.ENFORCED_POLICY = null;
        CSPConfiguration.REPORT_ONLY_POLICY = null;
        when(response.getWriter()).thenReturn(getWriter());

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setHeader(anyString(), anyString());
    }

    @Test
    public void testDoFilter_ExceptionPath() throws Exception {

        PrintWriter pw = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(pw);

        // Simulate an exception being thrown after the headers are set
        doThrow(new RuntimeException("Test Exception")).when(response).setHeader(anyString(), notNull());

        filter.doFilter(request, response, filterChain);

        // Verify that the fallback mechanism is triggered
        ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, atLeast(3)).setHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

        // Verify that the headers are cleared
        assertEquals(ENFORCED_POLICY_HEADER_NAME, headerNameCaptor.getAllValues().get(0));
        assertNull(headerValueCaptor.getAllValues().get(1));
        assertEquals(REPORT_ONLY_POLICY_HEADER_NAME, headerNameCaptor.getAllValues().get(2));
        assertNull(headerValueCaptor.getAllValues().get(2));

        verify(pw, atMost(1)).write(anyString());
        verify(pw).write(anyString());
    }



    private PrintWriter getWriter() {
        StringWriter stringWriter = new StringWriter();
        return new PrintWriter(stringWriter);
    }

}
