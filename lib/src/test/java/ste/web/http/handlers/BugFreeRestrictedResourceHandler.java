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
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ste.web.http.HttpSessionContext;
import ste.web.http.HttpUtils;
import ste.web.acl.AccessControlList;
import ste.web.acl.User;


/**
 *
 * TODO: null values in contructor
 */
public class BugFreeRestrictedResourceHandler {
    @Rule
    public final TemporaryFolder DOCROOT = new TemporaryFolder();
    
    private static final String[] RESTRICTED_URIS = {
        "/api/collections", "/item?id=10", "/private/news.html"
    };
    
    private final User[] USERS = {
        new User("one"), new User("two"), new User("three"), new User("four")
    };
    
    private final AccessControlList ACL = new AccessControlList();
    
    private final HttpRequestHandler DUMMY_HANDLER = new HttpRequestHandler() {
        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
            //
            // just to be able to make sure the handler was executed
            //
            response.addHeader(HttpHeaders.CONTENT_LOCATION, request.getRequestLine().getUri());
        }
    };
    
    @Before
    public void before() {
        ACL.add("ste.web.acl.permissions.star");
    }

    //
    // @TODO: we shall do it for all methods
    //
    
    @Test
    public void handle_the_request_in_authenticated_session() throws Exception {
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, ACL);
        
        Set<String> permissions = new HashSet<>();
        permissions.add("ste.web.acl.permissions.star");
        for (String URI: RESTRICTED_URIS) {
            HttpSessionContext session = new HttpSessionContext();
            HttpRequest rq = HttpUtils.getSimpleGet(URI);
            HttpResponse rs = HttpUtils.getBasicResponse();
            
            USERS[0].setPermissions(permissions);
            session.setPrincipal(USERS[0]);
            
            h.handle(rq, rs, session);
            
            StatusLine sl = rs.getStatusLine();
            then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
            then(rs.getFirstHeader(HttpHeaders.CONTENT_LOCATION).getValue())
                .isEqualTo(rq.getRequestLine().getUri());
        }
    }
 
    @Test
    public void returns_401_with_wwwauthenticate_on_get_if_unauthenticated_session()
    throws Exception {
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, ACL);
        
        for (String URI: RESTRICTED_URIS) {
            HttpSessionContext session = new HttpSessionContext();
            HttpRequest rq = HttpUtils.getSimpleGet(URI);
            HttpResponse rs = HttpUtils.getBasicResponse();
            
            h.handle(rq, rs, session);
            
            StatusLine sl = rs.getStatusLine();
            then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
            then(sl.getReasonPhrase()).isEqualTo("resource " + URI + " requires authentication");
            then(rs.getFirstHeader(HttpHeaders.WWW_AUTHENTICATE).getValue()).isEqualTo("Basic realm=\"serverone\"");
        }
    }

    @Test
    public void returns_403_on_get_if_wrong_permissions()
    throws Exception {
        AccessControlList acl = new AccessControlList();
        acl.add("ste.web.acl.permissions.private");
        
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, acl);
        
        for (Principal user: USERS) {
            for (String URI: RESTRICTED_URIS) {
                HttpSessionContext session = new HttpSessionContext();
                HttpRequest rq = HttpUtils.getSimpleGet(URI);
                HttpResponse rs = HttpUtils.getBasicResponse();
                
                session.setPrincipal(user);

                h.handle(rq, rs, session);

                StatusLine sl = rs.getStatusLine();
                then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN);
                then(sl.getReasonPhrase()).isEqualTo("resource " + URI + " requires authorization");
            }
        }
    }
    
    @Test
    public void get_acl() {
        then(new RestrictedResourceHandler(DUMMY_HANDLER, ACL).getAcl()).isSameAs(ACL);
        
        AccessControlList acl = new AccessControlList();
        then(new RestrictedResourceHandler(DUMMY_HANDLER, acl).getAcl()).isSameAs(acl);
    }
    
    @Test
    public void no_auth_check_if_acl_is_null() throws Exception {
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, null);
        
        HttpSessionContext session = new HttpSessionContext();
        HttpRequest rq = HttpUtils.getSimpleGet(RESTRICTED_URIS[0]);
        HttpResponse rs = HttpUtils.getBasicResponse();

        h.handle(rq, rs, session);

        StatusLine sl = rs.getStatusLine();
        then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(rs.getFirstHeader(HttpHeaders.CONTENT_LOCATION).getValue())
            .isEqualTo(rq.getRequestLine().getUri());
    }
}
