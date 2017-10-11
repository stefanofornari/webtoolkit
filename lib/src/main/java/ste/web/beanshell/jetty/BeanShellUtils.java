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
import bsh.Interpreter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpRequest;

import static ste.web.beanshell.Constants.*;
import ste.web.http.QueryString;

/**
 *
 * @author ste
 */
public class BeanShellUtils extends ste.web.beanshell.BeanShellUtils {

    // ---------------------------------------------------------- Public methods

    public static void setup(final Interpreter         interpreter,
                             final HttpServletRequest  request    ,
                             final HttpServletResponse response   )
    throws EvalError, IOException {
        //
        // Set attributes as script variables
        //
        String k, key;
        for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();) {
            k = (String)e.nextElement(); key = normalizeVariableName(k);
            interpreter.set(key, request.getAttribute(k));
        }

        //
        // Set request parameters as script variables. Note that parameters
        // override attributes
        //
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
            k = (String)e.nextElement(); key = normalizeVariableName(k);
            interpreter.set(key, request.getParameter(k));
        }

        interpreter.set(VAR_REQUEST,  request                  );
        interpreter.set(VAR_RESPONSE, response                 );
        interpreter.set(VAR_SESSION,  request.getSession(false));
        interpreter.set(VAR_OUT,      response.getWriter()     );
        interpreter.set(VAR_LOG,      log                      );
        if (hasJSONBody(request)) {
            interpreter.set(VAR_BODY, getJSONBody(request.getInputStream()));
        }
    }

    /**
     * Cleans up request variables so that they won't be set in next invocations
     *
     * @param interpreter the beanshell interpreter
     * @param request the request
     *
     */
    public static void cleanup(
        final Interpreter         interpreter,
        final HttpServletRequest  request    ) throws EvalError
    {
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            interpreter.unset(params.nextElement());
        }
    }
    /**
     * Cleans up request variables so that they won't be set in next invocations
     *
     * @param interpreter the beanshell interpreter
     * @param request the request
     *
     */
    public static void cleanup(
        final Interpreter interpreter,
        final HttpRequest request    
    ) throws EvalError {
        try {
            URI uri = new URI(request.getRequestLine().getUri());
            for(String name: QueryString.parse(uri).getNames()) {
                interpreter.unset(name);
            }
        } catch (URISyntaxException x) {
            //
            // nothing to do
            //
        }
    }
    

    /**
     * Sets all variables available in the interpreter as request attributes.
     *
     * @param i the interpreter - NOT NULL
     * @param r - the request - NOT NULL
     *
     * @throws EvalError
     */
    public static void setVariablesAttributes(final Interpreter i, final HttpServletRequest r)
    throws EvalError {
        if (i == null) {
            throw new IllegalArgumentException("i cannot be null");
        }

        if (r == null) {
            throw new IllegalArgumentException("r cannot be null");
        }

        String[] vars = (String[])i.get("this.variables");
        for(String var: vars) {
            r.setAttribute(var, i.get(var));
        }
    }
    
    /**
     * Returns true if the request content is supposed to contain a json object
     * as per the specified content type
     * 
     * @param request the request
     * 
     * @return true if the content type is "application/json", false otherwise
     */
    public static boolean hasJSONBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return CONTENT_TYPE_JSON.equals(contentType)
            || contentType.startsWith(CONTENT_TYPE_JSON + ";");
    }
}
