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

import bsh.EvalError;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
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
        viewsFolder = null;
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
     * @param viewsFolder the viewsFolder to set
     */
    public void setViewsFolder(String viewsFolder) {
        this.viewsFolder = viewsFolder;
    }

    @Override
    protected void doStart() throws Exception {
        engine = new VelocityEngine();
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
        
        String root = (String)getServer().getAttribute(ATTR_APP_ROOT);
        
        if (viewsFolder == null) {
            viewsFolder = DEFAULT_VIEWS_PREFIX;
        } else {
            //
            // let's fix a common mistake :)
            //
            if (!viewsFolder.startsWith("/")) {
                setViewsFolder('/' + getViewsFolder());
            }
        }

        File viewFile = new File(root, view);
        String viewPath = viewFile.getParent() + getViewsFolder();
        viewFile = new File(viewPath, viewFile.getName());
        
        Template t = engine.getTemplate(viewFile.getAbsolutePath());
        VelocityContext context = new VelocityContext();
        t.merge(context, hresponse.getWriter());
        request.setHandled(true);
        /*
         //} catch (FileNotFoundException e) {
         hresponse.sendError(HttpStatus.NOT_FOUND_404, "Script " + scriptFile + " not found.");
         request.setHandled(true);
         //} catch (EvalError e) {
         //throw new ServletException("Error evaluating " + uri + ": " + e, e);
         //}
         */
    }

    /**
     * @return the engine
     */
    public VelocityEngine getEngine() {
        return engine;
    }
}
