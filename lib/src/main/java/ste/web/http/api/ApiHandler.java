/*
 * BeanShell Web
 * Copyright (C) 2015 Stefano Fornari
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
package ste.web.http.api;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import ste.web.http.HttpSessionContext;
import static ste.web.http.api.Constants.*;
import ste.web.http.beanshell.BeanShellUtils;

/**
 *
 * @author ste
 */
public class ApiHandler  implements HttpRequestHandler {
    
    private final String apiroot;
    private final Logger log;

    /**
     * 
     * @param apiroot - NOT NULL
     * 
     * @throws IllegalArgumentException if webroot is null
     * 
     */
    public ApiHandler(final String apiroot) {
        if (apiroot == null) {
            throw new IllegalArgumentException("apiroot can not be null");
        }
        
        this.apiroot = apiroot;
        this.log = Logger.getLogger(LOG_NAME);
    }

    /**
     * Note that we expect response to have a body entity set (@see HttpEntiry)
     * 
     * @param request
     * @param response
     * @param context
     * 
     * @throws HttpException
     * @throws IOException 
     */
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        RRequest rr = null;
        File scriptFile = null;
        
        try {
            rr = new RRequest(request.getRequestLine());

            if (log.isLoggable(Level.FINE)) {
                log.fine(String.format("serving %s", rr.getPath()));
            }

            scriptFile = new File(apiroot, getHandlerScript(rr));

            if (log.isLoggable(Level.FINE)) {
                log.fine(String.format("script path: %s", scriptFile.getAbsolutePath()));
            }
        
            Interpreter bsh = new Interpreter();
            BeanShellUtils.setup(bsh, request, response, (HttpSessionContext)context);
            bsh.set(VAR_SOURCE, scriptFile.getAbsolutePath());
            bsh.set(VAR_RREQUEST, rr);
            bsh.eval(BeanShellUtils.getScript(scriptFile));
            
            Object body = bsh.get(rr.getHandler());
            
            BasicHttpEntity e = (BasicHttpEntity)response.getEntity();
            if (body != null) {
                String bodyString = String.valueOf(body);
                byte[] buf = bodyString.getBytes();
                ByteArrayInputStream is = new ByteArrayInputStream(buf);
                e.setContent(is);
                e.setContentLength(buf.length);
            } else {
                e.setContentLength(0);
            }
            
            if (e.getContentType() == null) {
                e.setContentType("application/json");
            }

            BeanShellUtils.cleanup(bsh, request);
            BeanShellUtils.setVariablesAttributes(bsh, context);
        } catch (FileNotFoundException e) {
            response.setStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "Script " + scriptFile + " not found.");
        } catch (EvalError x) {
            String msg = x.getMessage();

            if (log.isLoggable(Level.SEVERE)) {
                log.severe(String.format("error evaluating: %s: %s", scriptFile, msg));
                log.throwing(getClass().getName(), "handleError", x);
            }
            throw new HttpException("error evaluating " + scriptFile + ": " + msg, x);
        } catch (URISyntaxException x) {
            response.setStatusLine(
                HttpVersion.HTTP_1_1, 
                HttpStatus.SC_BAD_REQUEST, 
                StringEscapeUtils.escapeHtml(x.getMessage())
            );
        } catch (Exception x) {
            response.setStatusLine(
                HttpVersion.HTTP_1_1, 
                HttpStatus.SC_INTERNAL_SERVER_ERROR, 
                StringEscapeUtils.escapeHtml(x.getMessage())
            );
        }
    }
    
    // --------------------------------------------------------- private methods
    
    private String getHandlerScript(RRequest rr) {
        return rr.getApplication() + '/' + rr.getHandler() + '/' + rr.getHandler() + ".bsh";
    }
    
}
