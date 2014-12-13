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
import bsh.NameSpace;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 *
 * @author ste
 */
public class BeanShellUtils {

    // --------------------------------------------------------------- Constants

    public static final String DEFAULT_CONTROLLERS_PREFIX = "/";
    public static final String DEFAULT_VIEWS_PREFIX = "/";
    public static final String LOG_NAME = "ste.web";
    public static final String PARAM_CONTROLLERS = "controllers-prefix";
    public static final String PARAM_VIEWS = "views-prefix";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final Logger log = Logger.getLogger(LOG_NAME);

    // ---------------------------------------------------------- Public methods

    /**
     * Reads the given script enclosing it into a try-catch block.
     *
     * @param script the file to read - NOT NULL
     *
     * @return the script content
     *
     * @throws IOException in case of IO errors
     */
    public static String getScript(File script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script cannot be null");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream is = null;
        try {
            is = new FileInputStream(script);

            byte[] buf = new byte[1024];
            int n = 0;
            while ((n = is.read(buf)) >= 0) {
                baos.write(buf, 0, n);
            }
            return "try { " + baos.toString() + "} catch (Throwable t) { t.printStackTrace(); throw t; }";
        } finally {
            if (is != null) {
                is.close();
            }
            baos.close();
        }
    }

    public static void setup(final Interpreter        interpreter ,
                             final HttpRequest        request     ,
                             final HttpResponse       response    ,
                             final HttpSessionContext context     )
    throws EvalError, IOException {
        //
        // Set attributes as script variables
        //
        for (String k: context.keySet()) {
            String key = normalizeVariableName(k);
            interpreter.set(key, context.getAttribute(k));
        }
        
        //
        // Set request parameters as script variables. Note that parameters
        // override attributes
        //
        try {
            QueryString qs = QueryString.parse(new URI(request.getRequestLine().getUri()));
            for (String n: qs.getNames()) {
                String name = normalizeVariableName(n);
                interpreter.set(name, qs.getValues(n).get(0));
            }
        } catch (URISyntaxException x) {
            //
            // nothing to do
            //
        }
        
        BasicHttpConnection connection = 
            (BasicHttpConnection)context.getAttribute(HttpCoreContext.HTTP_CONNECTION);

        interpreter.set(VAR_REQUEST,  request                  );
        interpreter.set(VAR_RESPONSE, response                 );
        interpreter.set(VAR_SESSION,  context                  );
        interpreter.set(VAR_OUT,      connection.getWriter()   );
        interpreter.set(VAR_LOG,      log                      );
        if (hasJSONBody(request) && (request instanceof HttpEntityEnclosingRequest)) {
            interpreter.set(VAR_BODY, getJSONBody(getEntityInputStream(request)));
        }
    }
    
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
     * @throws bsh.EvalError
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
     * Sets all variables available in the interpreter as request attributes.
     *
     * @param i the interpreter - NOT NULL
     * @param c the request context - NOT NULL
     *
     * @throws EvalError
     */
    public static void setVariablesAttributes(final Interpreter i, final HttpContext c)
    throws EvalError {
        if (i == null) {
            throw new IllegalArgumentException("interpreter cannot be null");
        }

        if (c == null) {
            throw new IllegalArgumentException("context cannot be null");
        }

        String[] vars = (String[])i.get("this.variables");
        for(String var: vars) {
            c.setAttribute(var, i.get(var));
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
    
    /**
     * Returns true if the request content is supposed to contain a json object
     * as per the specified content type
     * 
     * @param request the request
     * 
     * @return true if the content type is "application/json", false otherwise
     */
    public static boolean hasJSONBody(HttpRequest request) {
        Header[] headers = request.getHeaders(HTTP.CONTENT_TYPE);
        if ((headers == null) || (headers.length == 0)) {
            return false;
        }
        
        String contentType = headers[0].getValue();
        return CONTENT_TYPE_JSON.equals(contentType)
            || contentType.startsWith(CONTENT_TYPE_JSON + ";");
    }
    
    /**
     * Replaces '.' with "_".
     * 
     * @param name the name to normalize - NOT NULL
     * 
     * @return the normalized version of the name
     */
    public static String normalizeVariableName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        return name.replaceAll("\\.", "_");
    }
    
    public static Object getJSONBody(final InputStream in) throws IOException {
        Object o = null;
        try {
            BufferedInputStream is = new BufferedInputStream(in);
            is.mark(0);
            if (is.read() == '{') {
                is.reset();
                o = new JSONObject(
                    new JSONTokener(new InputStreamReader(is))
                );
            } else {
                is.reset();
                o = new JSONArray(
                    new JSONTokener(new InputStreamReader(is))
                );
            }
        } catch (JSONException x) {
            throw new IOException("error parsing the body as a JSON object", x);
        }
        
        return o;
    }
    
        
    private static NameSpace getNameSpace(final HttpRequest request) {
        return new NameSpace(NameSpace.JAVACODE, "request-" + request.hashCode());
    }
}
