package ste.web.beanshell.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;

import static ste.web.beanshell.Constants.*;

public class JettyServer {
    
    public static final String PROPERTY_ROOT = "ste.web.jetty.root";
    
    private Server server;
    
    public static final String ROOT = "src/main/webapp";
    
    public JettyServer() {
        server = new Server(8080);
        
        String root = System.getProperty(PROPERTY_ROOT, ROOT);
        
        System.out.println("Root: " + root);
        
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
        resourceHandler.setResourceBase(root);
        
        BeanShellHandler bsHandler = new BeanShellHandler();
        bsHandler.setControllersFolder("/c");
        
        VelocityHandler velocityHandler = new VelocityHandler();
        velocityHandler.setViewsFolder("/v");
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, bsHandler, velocityHandler, new DefaultHandler() });
        
        SessionHandler sh = new SessionHandler();
        sh.setHandler(handlers);
        server.setHandler(sh);
        server.setAttribute(ATTR_APP_ROOT, root);
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