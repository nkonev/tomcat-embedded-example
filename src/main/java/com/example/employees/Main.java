package com.example.employees;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

public class Main {
    public static final Integer PORT = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt).orElse(8080);

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        tomcat.setPort(PORT);

        tomcat.setConnector(tomcat.getConnector());

        tomcat.setAddDefaultWebXmlToWebapp(false);

        String contextPath = "";
        String docBase = new File(".").getCanonicalPath();

        Context context = tomcat.addWebapp(contextPath, docBase);

        HttpServlet servlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
                PrintWriter writer = resp.getWriter();

                writer.println("<html><title>Welcome</title><body>");
                writer.println("<h1>Have a Great Day!</h1>");
                writer.println("</body></html>");
            }
        };
        String servletName = "Servlet1";
        String urlPattern = "/go";
        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded(urlPattern, servletName);


        // Additions to make @WebServlet (Servlet 3.0 annotation) work
        String buildPath = "target/classes";
        String webAppMount = "/WEB-INF/classes";

        File additionalWebInfClasses = new File(buildPath);
        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, webAppMount, additionalWebInfClasses.getAbsolutePath(), "/"));
        context.setResources(resources);
        // End of additions


        tomcat.start();
        tomcat.getServer().await();
    }
}
