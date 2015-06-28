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
import java.security.AccessControlException;
import javax.ws.rs.core.HttpHeaders;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import ste.web.http.HttpSessionContext;
import ste.web.acl.AccessControlList;
import ste.web.acl.MissingCredentialsException;
import ste.web.acl.User;

/**
 *
 * @author ste
 */
public class RestrictedResourceHandler implements HttpRequestHandler {
    
    private final HttpRequestHandler handler;
    private final  AccessControlList acl;
    
    /**
     * Creates a new RestrictedResourceHandler that wraps the given handler and
     * checks access to the resource accordingly to the given access control
     * list (acl). If acl is null, no authentication/authorization check will 
     * be performed.
     * 
     * Once handler and acl are given they cannot be changed.
     * 
     * @param handler the handler to wrap - NOT NULL
     * @param     acl  the access control list if not null - MAY BE NULL
     */
    public RestrictedResourceHandler(final HttpRequestHandler handler,
                                     final     AccessControlList acl    ) {
        this.handler = handler;
        this.acl = acl;
    }
    
    public AccessControlList getAcl() {
        return acl;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpSessionContext session = (HttpSessionContext)context;
        
        try {
            if (acl != null) {
                authorize((User)session.getPrincipal());
            }
            handler.handle(request, response, context);
        } catch (MissingCredentialsException x) {
            response.setStatusLine(
                HttpVersion.HTTP_1_1, 
                HttpStatus.SC_UNAUTHORIZED, 
                "resource " + request.getRequestLine().getUri() + " requires authentication"
            );
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"serverone\"");
            return;
        } catch (AccessControlException x) {
            response.setStatusLine(
                HttpVersion.HTTP_1_1, 
                HttpStatus.SC_FORBIDDEN, 
                "resource " + request.getRequestLine().getUri() + " requires authorization"
            );
            return;
        }
        
    }
    
    // --------------------------------------------------------- private methods
    
    private void authorize(final User user) throws AccessControlException {
        if (user == null){
            throw new MissingCredentialsException();
        }
        
        acl.checkPermissions(user.getPermissions());
    }    
}
