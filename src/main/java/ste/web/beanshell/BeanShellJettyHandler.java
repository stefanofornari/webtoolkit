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
package ste.web.beanshell;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import static ste.web.beanshell.Constants.*;

/**
 *
 * @author ste
 */
public class BeanShellJettyHandler extends AbstractHandler {
    
    // --------------------------------------------------------------- Constants
    
    // ------------------------------------------------------------ Private data
    
    private Interpreter bsh;
    
    private String controllersFolder;
    
    // ------------------------------------------------------------ Constructors
    
    public BeanShellJettyHandler() {
        bsh = null;
        controllersFolder = null;
    }

    // ---------------------------------------------------------- Public methods
    
    
    /**
     * @return the controllersFolder
     */
    public String getControllersFolder() {
        return controllersFolder;
    }

    /**
     * @param controllersFolder the controllersFolder to set
     */
    public void setControllersFolder(String controllersFolder) {
        this.controllersFolder = controllersFolder;
    }
    
    
    @Override
    protected void doStart() throws Exception {
        bsh = new Interpreter();
    }


    @Override
    public void handle(String uri, 
                       Request request, 
                       HttpServletRequest hrequest, 
                       HttpServletResponse hresponse) throws IOException, ServletException {
        request.setHandled(false);
        
        //
        // TODO: handle parameters
        //
        if (!uri.endsWith(".bsh")) {
            return;
        }
        
        String root = (String)getServer().getAttribute(ATTRIBUTE_APP_ROOT);
        
        if (controllersFolder == null) {
            controllersFolder = DEFAULT_CONTROLLERS_PREFIX;
        } else {
            //
            // let's fix a common mistake :)
            //
            if (!controllersFolder.startsWith("/")) {
                setControllersFolder('/' + getControllersFolder());
            }
        }
        
        File scriptFile = new File(root, uri);
        String controllerPath = scriptFile.getParent() + getControllersFolder();
        scriptFile = new File(controllerPath, scriptFile.getName());
        
        try {
            BeanShellUtils.setup(bsh, hrequest, hresponse);
            bsh.eval(BeanShellUtils.getScript(scriptFile));
            request.setHandled(true);
        } catch (FileNotFoundException e) {
            hresponse.sendError(HttpStatus.NOT_FOUND_404, "Script " + scriptFile + " not found.");
            request.setHandled(true);
        } catch (EvalError e) {
            throw new ServletException("Error evaluating " + uri + ": " + e, e);
        }
    }
    
    /**
     * Returns the interpreter used by this handler
     * 
     * @return the interpreter used by this handler
     */
    public Interpreter getInterpreter() {
        return bsh;
    }
    
}
