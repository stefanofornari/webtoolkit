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
package ste.web.http.beanshell;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import static ste.web.beanshell.Constants.*;
import ste.web.http.HttpSessionContext;

/**
 * Executes the bsh script specified by the URL. The script is the controller
 * and shall set the view that should be rendered after the execution of the 
 * script in the bsh variable <i>view</i>.
 * 
 * The lookup of the scripts and views are controlled by the following pattern:
 * 
 * <pre>
 *   {context}{controllers-prefix}/{script-pathname}
 *   {context}{views-prefix}/{jsp-pathname}
 * </pre>
 * 
 * For example, if context=/myapp, controllers-prefix=c and views-prefix=v,
 * the URL http://myserver:8080/myapp/mycontroller.bsh will read the script
 * {webroot}/myapp/c/mycontroller.bsh. If the controller sets <i>view</i>
 * to myview.jsp, the request is forwarded to http://myserver:8080/myapp/v/myview.jsp
 * 
 * <i>controllers-prefix</i> and <i>views-prefix</i> defauult to "".
 * 
 * In addition to scripts Beanshell can be extended with commands, which must be
 * located somewhere under the classpath.
 * 
 * @author ste
 */
public class BeanShellHandler implements HttpRequestHandler {

    // --------------------------------------------------------------- Constants
    
    // ------------------------------------------------------------ Private data

    private String controllersFolder;
    private String appsRoot;

    private final Logger log;

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new BeanShellHandler looking for scripts from the given webroot
     * and using the default controller folder.
     * 
     * @param webroot - NOT NULL
     */
    public BeanShellHandler(final String webroot) {
        if (webroot == null) {
            throw new IllegalArgumentException("webroot can not be null");
        }
        this.controllersFolder = null;
        this.appsRoot = webroot;
        this.log = Logger.getLogger(LOG_NAME);
    }
    
    public BeanShellHandler(final String webroot, final String controllerFolder) {
        this(webroot);
        setControllersFolder(controllerFolder);
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
    public void handle(HttpRequest  request,
                       HttpResponse response,
                       HttpContext  context) throws HttpException, IOException {
        
        String uri = request.getRequestLine().getUri();
        int pos = uri.indexOf('?');
        if (pos >= 0) {
            uri = uri.substring(0, pos);
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("serving %s", uri));
        }

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

        File scriptFile = new File(appsRoot, uri);
        String controllerPath = scriptFile.getParent() + getControllersFolder();
        scriptFile = new File(controllerPath, scriptFile.getName());

        if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("script path: %s", scriptFile.getAbsolutePath()));
        }

        try {
            Interpreter bsh = new Interpreter();
            BeanShellUtils.setup(bsh, request, response, (HttpSessionContext)context);
            bsh.set(VAR_SOURCE, scriptFile.getAbsolutePath());
            bsh.eval(BeanShellUtils.getScript(scriptFile));

            String view = (String)bsh.get(ATTR_VIEW);
            if (view == null) {
                throw new HttpException("view not defined. Set the variable 'view' to the name of the view to show (including .v).");
            }

            if (log.isLoggable(Level.FINE)) {
                log.fine("view: " + view);
            }

            BeanShellUtils.cleanup(bsh, request);
            BeanShellUtils.setVariablesAttributes(bsh, context);
        } catch (FileNotFoundException e) {
            response.setStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "Script " + scriptFile + " not found.");
        } catch (EvalError x) {
            String msg = x.getMessage();

            if (log.isLoggable(Level.SEVERE)) {
                log.severe(String.format("error evaluating: %s: %s", uri, msg));
                log.throwing(getClass().getName(), "handleError", x);
            }
            throw new HttpException("error evaluating " + uri + ": " + msg, x);
        }
    }

    // --------------------------------------------------------- Private methods

}
