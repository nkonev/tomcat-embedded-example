package com.example.employees;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Optional;

public class Main {
    public static final Integer PORT = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt).orElse(8080);

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("tomcat-temp");
        tomcat.setPort(PORT);

        tomcat.setConnector(tomcat.getConnector());
        // prevent register jsp servlet
        tomcat.setAddDefaultWebXmlToWebapp(false);

        String contextPath = "";
        String docBase = new File("/home/nkonev/javaWorkspace/employees-app/src/main/webapp").getCanonicalPath();
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


        final String defaultServletName = "default";
        Context staticContext = context;
        Wrapper defaultServlet = staticContext.createWrapper();
        defaultServlet.setName(defaultServletName);
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        staticContext.addChild(defaultServlet);
        staticContext.addServletMappingDecoded("/", defaultServletName);

        // Additions to make @WebServlet (Servlet 3.0 annotation) work
        String webAppMount = "/WEB-INF/classes";
        WebResourceRoot resources = new StandardRoot(context);
        Resource resource = getAbsoluteResourcePath();
        WebResourceSet webResourceSet;
        if (!resource.isJar()) {
            webResourceSet = new DirResourceSet(resources, webAppMount, resource.getAbsolutePath(), "/");
        } else {
            webResourceSet = new JarResourceSet(resources, webAppMount, resource.getAbsolutePath(), "/");
        }
        resources.addPreResources(webResourceSet);
        context.setResources(resources);
        // End of additions


        tomcat.start();
        tomcat.getServer().await();
    }

    public static class Resource {
        private boolean jar;
        private String absolutePath;

        public Resource(boolean jar, String absolutePath) {
            this.jar = jar;
            this.absolutePath = absolutePath;
        }

        public boolean isJar() {
            return jar;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }
    }

    public static Resource getAbsoluteResourcePath() throws UnsupportedEncodingException {
        String buildPath = "target/classes";
        File additionalWebInfClasses = new File(buildPath);
        if (additionalWebInfClasses.exists()) {
            return new Resource(false, additionalWebInfClasses.getAbsolutePath());
        } else {
            File jarFile = new File(System.getProperty("java.class.path"));
            System.out.println(jarFile.getAbsolutePath());
            return new Resource(true, jarFile.getAbsolutePath());
        }
    }
}
