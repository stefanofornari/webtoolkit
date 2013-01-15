package ste.web.beanshell.jetty;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
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
import ste.web.beanshell.BeanShellJettyHandler;
import ste.web.beanshell.BeanShellServlet;

import static ste.web.beanshell.Constants.*;

public class JettyServer {
    
    public static final String ROOT = "src/main/webapp/console";
    
    public JettyServer() {
    }

    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        //request.setAttribute(target, this);
        throw new ServletException("CHECK!", new Exception());
        //response.sendError(HttpServletResponse.SC_NOT_FOUND, "Error!");
        //baseRequest.setHandled(true);
        //response.getWriter().println("<h1>Hello World</h1><p>" + request.getPathTranslated());
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setInitParameter(PARAM_CONTROLLERS, "/c");
        context.setInitParameter(PARAM_VIEWS, "/v");
        context.setBaseResource(Resource.newResource(new File(ROOT)));
        context.addServlet(new ServletHolder(new BeanShellServlet()),"*.bsh");
        
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
        resourceHandler.setResourceBase(ROOT);
        
        BeanShellJettyHandler bsHandler = new BeanShellJettyHandler();
        bsHandler.setControllersFolder("/c");
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, bsHandler, new DefaultHandler() });
        
        SessionHandler sh = new SessionHandler();
        sh.setHandler(handlers);
        server.setHandler(sh);
        server.setAttribute(ATTRIBUTE_APP_ROOT, ROOT);

        server.start();
        server.join();
    }
}