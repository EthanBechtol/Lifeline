package com.bechtol.lifeline;

import java.net.URL;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

/**
 * Starts up the server, including a DefaultServlet that handles static files, and any servlet
 * classes annotated with the @WebServlet annotation.
 * 
 * Created with the help of https://happycoding.io/tutorials/google-cloud/app-engine.
 * Adapted from the Google Software Product Sprint program.
 */
public class ServerMain {

  public static void main(String[] args) throws Exception {
    Server server = new Server(8080);
    WebAppContext webAppContext = new WebAppContext();
    server.setHandler(webAppContext);

    URL webAppDir = ServerMain.class.getClassLoader().getResource("META-INF/resources");
    webAppContext.setResourceBase(webAppDir.toURI().toString());

    webAppContext.setConfigurations(
        new Configuration[] {
          new AnnotationConfiguration(), new WebInfConfiguration(),
        });

    webAppContext.setAttribute(
        "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
        ".*/target/classes/|.*\\.jar");

    webAppContext.addServlet(DefaultServlet.class, "/");

    server.start();
    System.out.println("Server started!");

    server.join();
  }
}
