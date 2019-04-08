package com.example.employees;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(
        "/path"
)
public class EmployeeServlet extends HttpServlet {
    EmployeeService employeeService = new EmployeeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        writer.println("<html><title>EMPLOYEES</title><body>");
        writer.println("<h1>Employees works!</h1>");
        writer.println("</body></html>");
    }


    private void searchEmployeeById(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long idEmployee = Integer.valueOf(req.getParameter("idEmployee"));
        Employee employee = null;
        try {
            employee = employeeService.getEmployee(idEmployee);
        } catch (Exception ex) {
            Logger.getLogger(EmployeeServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        req.setAttribute("employee", employee);
        req.setAttribute("action", "edit");
        String nextJSP = "/jsp/new-employee.jsp";
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
        dispatcher.forward(req, resp);
    }

    private void searchEmployeeByName(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String employeeName = req.getParameter("employeeName");
        List<Employee> result = employeeService.searchEmployeesByName(employeeName);
        forwardListEmployees(req, resp, result);
    }

    private void forwardListEmployees(HttpServletRequest req, HttpServletResponse resp, List employeeList)
            throws ServletException, IOException {
        String nextJSP = "/jsp/list-employees.jsp";
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
        req.setAttribute("employeeList", employeeList);
        dispatcher.forward(req, resp);
    }
}
