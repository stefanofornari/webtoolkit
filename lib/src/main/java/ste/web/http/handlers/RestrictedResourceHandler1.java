/*
 * Copyright (C) 2015 Stefano Fornari.
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Stefano Fornari.  This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * STEFANO FORNARI MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. STEFANO FORNARI SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package ste.web.http.handlers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import org.apache.http.HttpVersion;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 *
 * @author ste
 */
public class RestrictedResourceHandler1 implements HttpRequestHandler {
    
    private final  HttpRequestHandler handler;
    private final Map<String, String> users;
    
    public RestrictedResourceHandler1(final HttpRequestHandler handler,
                                     final Map<String, String> users) {
        this.handler = handler;
        this.users = users;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Header authorization = request.getFirstHeader(HttpHeaders.AUTHORIZATION);
        if (!isAuthorized(authorization)) {
            response.setStatusLine(
                HttpVersion.HTTP_1_1, 
                SC_UNAUTHORIZED, 
                "resource " + request.getRequestLine().getUri() + " requires authentication"
            );
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"serverone\"");
            return;
        }
        
        handler.handle(request, response, context);
    }
    
    // --------------------------------------------------------- private methods
    
    private boolean isAuthorized(final Header authorization) {
        String authString = getAuthString(authorization);

        int p = (authString == null) ? -1 : authString.indexOf(':');
        if (p>0) {
            String login = authString.substring(0, p);

            return (login + ':' + users.get(login)).equals(authString);
        }
        
        return false;
    }
    
    private String getAuthString(final Header authorization) {
        if (authorization == null) {
            return null;
        }
        
        String value = authorization.getValue();
        
        if (!value.startsWith("Basic ") || (value.length() == 6)) {
            return null;
        }
        
        value = value.substring(6);
        
        try {
            return new String(Base64.getDecoder().decode(value), "UTF-8");
         } catch (UnsupportedEncodingException x) {
            return null;
        } 
    }
    
}
