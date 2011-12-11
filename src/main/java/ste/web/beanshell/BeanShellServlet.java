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

public class BeanShellServlet
extends HttpServlet {
    // --------------------------------------------------------------- Constants

    public static final String LOG_NAME = "com.funambol.web";

    public static final String BSH_PREFIX = "WEB-INF/bsh";

    // ------------------------------------------------------------ Private data

    private static final Logger log = Logger.getLogger(LOG_NAME);

    // ---------------------------------------------------------- Public methods
    public void init() throws ServletException {
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
        log.info("Serving " + request.getRequestURI());

        try {
            Interpreter interpreter = createInterpreter(request, response);

            interpreter.eval(getScript(request));

            Object nextView = interpreter.get("nextView");

            if ((nextView != null) && (nextView instanceof String)) {
                log.info("Forwarding to " + nextView);
                request.getRequestDispatcher((String)nextView).forward(request, response);
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

        //
        // Import common commands
        //
        interpreter.eval("importCommands(\"commands\");");

        return interpreter;
    }

    private String getScript(final HttpServletRequest  request)
    throws IOException {
        String script = request.getServletPath();

        log.info(BSH_PREFIX + script);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream           is   = null;

        try {
            is = getClass().getClassLoader().getResourceAsStream(BSH_PREFIX + script);

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
     * @msg   a desctiptive message
     * @t     a throwable object
     *
     */
    private void handleError(final HttpServletRequest   request,
                             final HttpServletResponse response,
                             final Throwable                  t) {

        String msg = t.getMessage();

        log.severe(msg);
        log.throwing(getClass().getName(), "unknown", t);

        try {
            if (t instanceof FileNotFoundException) {
                response.sendError(response.SC_NOT_FOUND, msg);
            } else {
                response.sendError(response.SC_BAD_REQUEST, msg);
            }
        } catch (IOException e) {
            log.severe(e.getMessage());
            log.throwing(getClass().getName(), "handleError", e);
        }
    }
}