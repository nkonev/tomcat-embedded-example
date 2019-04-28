package com.example.employees;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/employee")
public class EmployeeServlet extends HttpServlet {

    private static  final Logger LOGGER = LoggerFactory.getLogger(EmployeeServlet.class);

    @Override
    public void init() {
        LOGGER.info("Initializing {}", EmployeeServlet.class);
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        writer.println("<html><title>EMPLOYEES</title><body>");
        writer.println("<h1>Employees works!</h1>");
        writer.println("</body></html>");
    }

    @Override
    public void destroy() {
        LOGGER.info("Destroying {}", EmployeeServlet.class);
    }
}
