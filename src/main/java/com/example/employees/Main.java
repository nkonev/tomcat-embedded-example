package com.example.employees;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.Wrapper;
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
import java.net.URL;
import java.util.Optional;

public class Main {
    public static final Integer PORT = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt).orElse(8080);

    public static final String STATICDIR = Optional.ofNullable(System.getenv("STATICDIR")).orElse(".");

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("tomcat-temp");
        tomcat.setPort(PORT);

        tomcat.setConnector(tomcat.getConnector());
        // prevent register jsp servlet
        tomcat.setAddDefaultWebXmlToWebapp(false);

        String contextPath = ""; // root context
        String docBase = new File(STATICDIR).getCanonicalPath();
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


        WebResourceRoot webResourceRoot = new StandardRoot(context);

        // Additions to make @WebServlet (Servlet 3.0 annotation) work
        {
            String webAppMount = "/WEB-INF/classes";
            WebResourceSet webResourceSet;
            if (!isJar()) {
                webResourceSet = new DirResourceSet(webResourceRoot, webAppMount, getResourceFromFs(), "/");
            } else {
                webResourceSet = new JarResourceSet(webResourceRoot, webAppMount, getResourceFromJarFile(), "/");
            }
            webResourceRoot.addJarResources(webResourceSet);
        }


        // Additions to make serving static work
        final String defaultServletName = "default";
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName(defaultServletName);
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        context.addChild(defaultServlet);
        context.addServletMappingDecoded("/", defaultServletName);
        {
            String webAppMount = "/META-INF/resources";
            WebResourceSet webResourceSet;
            if (!isJar()) {
                // potential dangerous - if last argument will "/" that means tomcat will serves self jar with .class files
                webResourceSet = new DirResourceSet(webResourceRoot, "/", getResourceFromFs(), webAppMount);
            } else {
                webResourceSet = new JarResourceSet(webResourceRoot, "/", getResourceFromJarFile(), webAppMount);
            }
            webResourceRoot.addJarResources(webResourceSet);
        }
        context.setResources(webResourceRoot);


        tomcat.start();
        tomcat.getServer().await();
    }


    public static boolean isJar() {
        URL resource = Main.class.getResource("/");
        return resource == null;
    }

    public static String getResourceFromJarFile() {
        File jarFile = new File(System.getProperty("java.class.path"));
        return jarFile.getAbsolutePath();
    }

    public static String getResourceFromFs() {
        URL resource = Main.class.getResource("/");
        return resource.getFile();
    }

}
