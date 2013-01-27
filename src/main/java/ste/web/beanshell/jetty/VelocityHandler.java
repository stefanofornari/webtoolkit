/*
 * BeanShell Web
 * Copyright (C) 2012 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */
package ste.web.beanshell.jetty;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import static ste.web.beanshell.Constants.*;

/**
 *
 * @author ste
 */
public class VelocityHandler extends AbstractHandler {

    // --------------------------------------------------------------- Constants
    // ------------------------------------------------------------ Private data
    private VelocityEngine engine;
    private String viewsFolder;

    // ------------------------------------------------------------ Constructors
    public VelocityHandler() {
        engine = null;
        setViewsFolder(null);
    }

    // ---------------------------------------------------------- Public methods
    /**
     * @return the viewsFolder
     *
     */
    public String getViewsFolder() {
        return viewsFolder;
    }

    /**
     * Sets the folder where views are located. If the the given value is null,
     * it defaults to DEFAULT_VIEWS_PREFIX.
     * 
     * @param viewsFolder the viewsFolder to set - NULL
     */
    public void setViewsFolder(final String viewsFolder) {
        if (viewsFolder == null) {
            this.viewsFolder = DEFAULT_VIEWS_PREFIX;
        } else {
            //
            // let's fix a common mistake :)
            //
            this.viewsFolder = (!viewsFolder.startsWith("/"))
                             ? ('/' + viewsFolder)
                             : viewsFolder
                             ;
        }
    }

    @Override
    protected void doStart() throws Exception {
        String root = (String)getServer().getAttribute(ATTR_APP_ROOT);
        
        engine = new VelocityEngine();
        engine.setProperty("file.resource.loader.path", root);
        engine.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        engine.setProperty( "resource.loader", "file" );
        
        engine.init();
    }

    @Override
    public void handle(String uri,
            Request request,
            HttpServletRequest hrequest,
            HttpServletResponse hresponse) throws IOException, ServletException {
        request.setHandled(false);
        
        String view = (String)request.getAttribute(ATTR_VIEW);
        if (view == null) {
            return;
        }
        
        if (!view.endsWith(".v")) {
            return;
        }
        
        File viewFile = new File(viewsFolder, view);
        
        try {
            Template t = engine.getTemplate(viewFile.getAbsolutePath());
            Writer w = hresponse.getWriter();
            t.merge(buildContext(hrequest), w); w.flush();
            
            request.setHandled(true); hresponse.setStatus(HttpStatus.OK_200);
        } catch (ResourceNotFoundException e) {
            hresponse.sendError(HttpStatus.NOT_FOUND_404, "View " + viewFile + " not found.");
            request.setHandled(true);
        } catch (ParseErrorException e) {
            throw new ServletException("Parse error evaluating " + view + ": " + e, e);
        } catch (MethodInvocationException e) {
            throw new ServletException("Method invocation error evaluating " + view + ": " + e, e);
        }
    }

    /**
     * @return the engine
     */
    public VelocityEngine getEngine() {
        return engine;
    }
    
    // --------------------------------------------------------- Private methods
    
    /**
     * Creates a velocity context filling it with all request attributes
     * 
     * @param request the request to create the context upon
     * 
     * @return the newly created context
     */
    private VelocityContext buildContext(HttpServletRequest request) {
        VelocityContext context = new VelocityContext();
        
        String key = null;
        for (Enumeration<String> e = request.getAttributeNames(); e.hasMoreElements();) {
            key = e.nextElement();
            context.put(key, request.getAttribute(key));
        }
        
        return context;
    }
}
