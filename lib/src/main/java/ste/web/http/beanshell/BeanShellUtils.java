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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import static ste.web.beanshell.Constants.*;
import ste.web.http.BasicHttpConnection;
import ste.web.http.HttpSessionContext;
import ste.web.http.HttpUtils;
import ste.web.http.QueryString;

/**
 *
 * @author ste
 */
public class BeanShellUtils extends ste.web.beanshell.BeanShellUtils {

    // ---------------------------------------------------------- Public methods

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
        // If the request contains url-encoded body, set the given parameters
        //
        Header[] headers = request.getHeaders(HttpHeaders.CONTENT_TYPE);
        if (headers.length > 0) {
            String contentType = headers[0].getValue();
            if (contentType.matches(ContentType.APPLICATION_FORM_URLENCODED.getMimeType() + "( *;.*)?")) {
                HttpEntityEnclosingRequest r = (HttpEntityEnclosingRequest)request;
                HttpEntity e = r.getEntity();

                QueryString qs = QueryString.parse(IOUtils.toString(e.getContent()));
                for (String n: qs.getNames()) {
                    String name = normalizeVariableName(n);
                    interpreter.set(name, qs.getValues(n).get(0));
                }
            }
        }
        
        //
        // Set request parameters as script variables. Note that parameters
        // override attributes (note that these override form content
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

        interpreter.set(VAR_REQUEST,  request                             );
        interpreter.set(VAR_RESPONSE, response                            );
        interpreter.set(VAR_SESSION,  context                             );
        interpreter.set(VAR_OUT,      connection.getWriter()              );
        interpreter.set(VAR_LOG,      log                                 );
        if (HttpUtils.hasJSONBody(request) && (request instanceof HttpEntityEnclosingRequest)) {
            interpreter.set(VAR_BODY, getJSONBody(getEntityInputStream(request)));
        }
    }
    
    /**
     * Cleans up request variables so that they won't be set in next invocations
     *
     * @param interpreter the beanshell interpreter
     * @param request the request
     * 
     * @throws bsh.EvalError in case of syntax errors
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

    // --------------------------------------------------------- private methods
    
    private static InputStream getEntityInputStream(HttpRequest r) throws IOException {
        return ((HttpEntityEnclosingRequest)r).getEntity().getContent();
    }
}
