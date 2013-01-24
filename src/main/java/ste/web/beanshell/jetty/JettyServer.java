package ste.web.beanshell.jetty;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import ste.web.beanshell.BeanShellServlet;

import static ste.web.beanshell.Constants.*;

public class JettyServer {
    
    private Server server;
    
    public static final String ROOT = "src/main/webapp/console";
    
    public JettyServer() {
        server = new Server(8080);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setInitParameter(PARAM_CONTROLLERS, "/c");
        context.setInitParameter(PARAM_VIEWS, "/v");
        try {
            context.setBaseResource(Resource.newResource(new File(ROOT)));
        } catch (Exception e) {
            //
            // this should not happen!
            //
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
        context.addServlet(new ServletHolder(new BeanShellServlet()),"*.bsh");
        
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
        resourceHandler.setResourceBase(ROOT);
        
        BeanShellHandler bsHandler = new BeanShellHandler();
        bsHandler.setControllersFolder("/c");
        
        VelocityHandler velocityHandler = new VelocityHandler();
        velocityHandler.setViewsFolder("/v");
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, bsHandler, velocityHandler, new DefaultHandler() });
        
        SessionHandler sh = new SessionHandler();
        sh.setHandler(handlers);
        server.setHandler(sh);
        server.setAttribute(ATTR_APP_ROOT, ROOT);
    }
    
    public boolean start() {
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            //
            // TODO: log the error?
            //
            e.printStackTrace();
            return false;
        }
        
        return true;
    }

    public static void main(String[] args) {
        new JettyServer().start();
    }
}