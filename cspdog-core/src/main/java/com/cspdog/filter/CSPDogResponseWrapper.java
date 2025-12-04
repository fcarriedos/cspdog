package com.cspdog.filter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class CSPDogResponseWrapper extends HttpServletResponseWrapper {

    private CharArrayWriter charArrayWriter;
    private PrintWriter writer;

    public CSPDogResponseWrapper(HttpServletResponse response) {
        super(response);
        charArrayWriter = new CharArrayWriter();
        writer = new PrintWriter(charArrayWriter);
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public String toString() {
        writer.flush();
        return charArrayWriter.toString();
    }

}
