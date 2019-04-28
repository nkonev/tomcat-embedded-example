package com.example.employees;

import org.apache.catalina.*;
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
import java.net.URL;
import java.util.Optional;
import org.slf4j.bridge.SLF4JBridgeHandler;


public class Main {
    public static final Integer PORT = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt).orElse(8080);

    public static final String STATICDIR = Optional.ofNullable(System.getenv("STATICDIR")).orElse("/tmp/tomcat-static");
    public static final String TMPDIR = Optional.ofNullable(System.getenv("TMPDIR")).orElse("/tmp/tomcat-tmp");

    public static void main(String[] args) throws Exception {
        // initialize logback
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(TMPDIR);
        tomcat.setPort(PORT);

        tomcat.setConnector(tomcat.getConnector());
        // prevent register jsp servlet
        tomcat.setAddDefaultWebXmlToWebapp(false);

        String contextPath = ""; // root context
        new File(STATICDIR).mkdirs();
        String docBase = new File(STATICDIR).getCanonicalPath();
        Context context = tomcat.addWebapp(contextPath, docBase);
        context.setAddWebinfClassesResources(true); // process /META-INF/resources for static

        // fix Illegal reflective access by org.apache.catalina.loader.WebappClassLoaderBase
        // https://github.com/spring-projects/spring-boot/issues/15101#issuecomment-437384942
        StandardContext standardContext = (StandardContext) context;
        standardContext.setClearReferencesObjectStreamClassCaches(false);
        standardContext.setClearReferencesRmiTargets(false);
        standardContext.setClearReferencesThreadLocals(false);


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
        // display index.html on http://127.0.0.1:8080
        context.addWelcomeFile("index.html");

        // add itself jar with static resources (html) and annotated servlets

        String webAppMount = "/WEB-INF/classes";
        WebResourceSet webResourceSet;
        if (!isJar()) {
            // potential dangerous - if last argument will "/" that means tomcat will serves self jar with .class files
            webResourceSet = new DirResourceSet(webResourceRoot, webAppMount, getResourceFromFs(), "/");
        } else {
            webResourceSet = new JarResourceSet(webResourceRoot, webAppMount, getResourceFromJarFile(), "/");
        }
        webResourceRoot.addJarResources(webResourceSet);
        context.setResources(webResourceRoot);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tomcat.getServer().stop();
                } catch (LifecycleException e) {
                    e.printStackTrace();
                }
            }
        }));


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