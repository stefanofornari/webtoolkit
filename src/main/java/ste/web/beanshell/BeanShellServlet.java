/*
 * Copyright (C) 2011 Stefano Fornari.
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Stefano Fornari.  This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * Stefano Fornari MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. Funambol SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

package ste.web.beanshell;

import java.io.*;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.http.*;

import bsh.*;
import java.util.logging.Level;

/**
 * Executes the bsh script specified by the URL. The script is the controller
 * and shall set the view that should be rendered after the execution of the 
 * script in the bsh variable <i>view</i>.
 * 
 * The lookup of the scripts and views are controlled by the following pattern:
 * <pre>
 *   {context}{controllers-prefix}/{script-pathname}
 *   {context}{vies-prefix}/{jsp-pathname}
 * </pre>
 * For example, if context=/myapp, controllers-prefix=c and views-prefix=v,
 * the URL http://myserver:8080/myapp/mycontroller.bsh will read the script
 * {CONTAINER_HOME}/myapp/c/mycontroller.bsh. If the controller sets <i>view</i>
 * to myview.jsp, the request is forwarded to http://myserver:8080/myapp/v/myview.jsp
 * 
 * <i>controllers-prefix</i> and <i>views-prefix</i> can be set as context 
 * parameters in web.xml:
 * 
 * &lt;context-param&gt;
 *   &lt;param-name&gt;<b>controllers-prefix</b>&lt;/param-name&gt;
 *   &lt;param-value&gt;<b>controllers</b>&lt;/param-value&gt;
 * &lt;/context-param&gt;
 * &lt;context-param&gt;
 *   &lt;param-name&gt;<b>views-prefix</b>&lt;/param-name&gt;
 *   &lt;param-value&gt;<b>views</b>&lt;/param-value&gt;
 * &lt;/context-param&gt;
 * 
 * <i>controllers-prefix</i> and <i>views-prefix</i> defauult to "".
 * 
 * @author ste
 */
public class BeanShellServlet
extends HttpServlet {
    // --------------------------------------------------------------- Constants

    public static final String LOG_NAME = "ste.web";
    
    public static final String PARAM_CONTROLLERS          = "controllers-prefix";
    public static final String PARAM_VIEWS                = "views-prefix"      ;
    public static final String DEFAULT_CONTROLLERS_PREFIX = "/"                 ;
    public static final String DEFAULT_VIEWS_PREFIX       = "/"                 ;

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(LOG_NAME);
    
    private static String controllersPrefix = "controllers";
    private static String viewsPrefix       = "views"      ;

    // ---------------------------------------------------------- Public methods
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = getServletContext();
        
        controllersPrefix = context.getInitParameter(PARAM_CONTROLLERS);
        if (controllersPrefix == null) {
            controllersPrefix = context.getInitParameter(DEFAULT_CONTROLLERS_PREFIX);
        } else {
            //
            // let's fix a common mistake :)
            //
            if (!controllersPrefix.startsWith("/")) {
                controllersPrefix = '/' + controllersPrefix;
            }
        }
        
        viewsPrefix = context.getInitParameter(PARAM_VIEWS);
        if (viewsPrefix == null) {
            context.getInitParameter(DEFAULT_VIEWS_PREFIX);
        }  else {
            //
            // let's fix a common mistake :)
            //
            if (!viewsPrefix.endsWith("/")) {
                viewsPrefix = viewsPrefix + '/';
            }
        }
        
        if (log.isLoggable(Level.FINE)) {
            log.fine("controllers-prefix: " + controllersPrefix);
            log.fine("views-prefix: " + viewsPrefix);
        }
    }

    public void doGet(final HttpServletRequest  request ,
                      final HttpServletResponse response)
    throws ServletException, IOException {
        doWork(request, response);
    }

    public void doPost(final HttpServletRequest  request ,
                      final HttpServletResponse response)
    throws ServletException, IOException {
        doWork(request, response);
    }

    // --------------------------------------------------------- Private methods

    private void doWork(final HttpServletRequest  request ,
                        final HttpServletResponse response)
    throws ServletException, IOException {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Serving " + request.getRequestURI());
        }

        try {
            Interpreter interpreter = createInterpreter(request, response);

            interpreter.eval(getScript(request));
            
            String nextView = (String)interpreter.get("view");
            if ((nextView != null) && (nextView instanceof String)) {
                nextView = viewsPrefix + nextView;
                
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Forwarding to " + nextView);
                }
                
                request.getRequestDispatcher(nextView).forward(request, response);
            }
        } catch (Exception e) {
            handleError(request, response, e);
        }

    }

    private Interpreter createInterpreter(final HttpServletRequest  request ,
                                          final HttpServletResponse response)
    throws EvalError, IOException {
        Interpreter interpreter = new Interpreter();

        //
        // Set request parameters as script variables
        //
        String key;
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
            key = (String)e.nextElement();
            interpreter.set(key, request.getParameter(key));
        }

        interpreter.set("request" , request                   );
        interpreter.set("response", response                  );
        interpreter.set("session" , request.getSession(false) );
        interpreter.set("out"     , response.getWriter()      );
        interpreter.set("log"     , log                       );

        //
        // Import common commands... I am commenting it out for now; I will 
        // reintroduce it if and when I will add the helpers
        //
        //interpreter.eval("importCommands(\"commands\");");

        return interpreter;
    }

    private String getScript(final HttpServletRequest request)
    throws IOException {
        String script     = request.getServletPath();
        File   scriptFile = new File(request.getSession().getServletContext().getRealPath(script));
        String controllerPath = scriptFile.getParent() + controllersPrefix;
        File   controllerFile = new File(controllerPath, scriptFile.getName());
        
        if (log.isLoggable(Level.FINE)) {
            log.fine("script: " + script);
            log.fine("controllerFile: " + controllerFile);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream       is   = null;

        try {
            is = new FileInputStream(controllerFile);

            if (is == null) {
                throw new FileNotFoundException(script);
            }

            byte[] buf = new byte[1024];
            int n = 0;
            while ((n = is.read(buf))>=0) {
                baos.write(buf, 0, n);
            }

            return "try { " + baos.toString() + "} catch (Throwable t) { t.printStackTrace(); throw t; }";
        } finally {
            if (is != null) {
                is.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
    }

    /**
     * Handles errors conditions returning an appropriate content to the client.
     *
     * @param request the request object
     * @param response the response object
     * @t     a throwable object
     *
     */
    private void handleError(final HttpServletRequest   request,
                             final HttpServletResponse response,
                             final Throwable                  t) {

        String msg = t.getMessage();

        if (log.isLoggable(Level.SEVERE)) {
            log.severe("Error message: " + msg);
            log.throwing(getClass().getName(), "handleError", t);
        }
        
        try {
            if (t instanceof FileNotFoundException) {
                response.sendError(response.SC_NOT_FOUND, msg);
            } else {
                response.sendError(response.SC_BAD_REQUEST, msg);
            }
        } catch (IOException e) {
            if (log.isLoggable(Level.SEVERE)) {
                log.severe(e.getMessage());
                log.throwing(getClass().getName(), "handleError", e);
            }
        }
    }
}