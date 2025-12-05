package com.cspdog.example;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/hello")
public class HelloWorldServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.println("<html><head><title>Hello World</title></head><body>");
        writer.println("<h1>Hello World!</h1>");
        writer.println("<script>function sayHi() { alert('Hi!'); }</script>");
        writer.println("<a href=\"javascript:sayHi()\" style=\"color: green\">Say hi!</a>");
        writer.println("</body></html>");
        writer.close();
    }
}
