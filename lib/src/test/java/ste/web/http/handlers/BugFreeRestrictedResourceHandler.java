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
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ste.web.http.HttpSessionContext;
import ste.web.http.HttpUtils;
import ste.web.acl.AccessControlList;
import ste.web.acl.HashMapAuthenticator;
import ste.web.acl.User;
import ste.web.http.HttpSession;


public class BugFreeRestrictedResourceHandler {
    
    @Rule
    public final TemporaryFolder DOCROOT = new TemporaryFolder();
    
    private static final String[] RESTRICTED_URIS = {
        "/api/collections", "/item?id=10", "/private/news.html"
    };
    
    private final User[] USERS = {
        new User("one", "111"), new User("two", "222"), 
        new User("three", "333"), new User("four", "444")
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
    
    private final HashMapAuthenticator AUTHENTICATOR = new HashMapAuthenticator(USERS);
    
    @Before
    public void before() {
        ACL.add("ste.web.acl.permissions.star");
    }
    
    @Test
    public void constructors() {
        try {
            new RestrictedResourceHandler(null, ACL, AUTHENTICATOR);
            fail("missing arguments check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("handler can not be null");
        }
        
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, null, null);
        then(h.getHandler()).isSameAs(DUMMY_HANDLER);
        then(h.getAcl()).isNull();
        then(h.getAuthenticator()).isNull();
        
        h = new RestrictedResourceHandler(DUMMY_HANDLER, ACL, AUTHENTICATOR);
        then(h.getHandler()).isSameAs(DUMMY_HANDLER);
        then(h.getAcl()).isSameAs(ACL);
        then(h.getAuthenticator()).isSameAs(AUTHENTICATOR);
    }

    //
    // @TODO: we shall do it for all methods
    //
    
    @Test
    public void handle_the_request_in_authenticated_session() throws Exception {
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, ACL, AUTHENTICATOR);
        
        Set<String> permissions = new HashSet<>();
        permissions.add("ste.web.acl.permissions.star");
        for (String URI: RESTRICTED_URIS) {
            HttpSessionContext context = new HttpSessionContext();
            HttpRequest rq = HttpUtils.getSimpleGet(URI);
            HttpResponse rs = HttpUtils.getBasicResponse();
            
            context.setSession(new HttpSession());
            USERS[0].setPermissions(permissions);
            context.setPrincipal(USERS[0]);
            
            h.handle(rq, rs, context);
            
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
            new RestrictedResourceHandler(DUMMY_HANDLER, ACL, AUTHENTICATOR);
        AUTHENTICATOR.message = UUID.randomUUID().toString();
        
        for (String URI: RESTRICTED_URIS) {
            HttpSessionContext context = new HttpSessionContext();
            HttpRequest rq = HttpUtils.getSimpleGet(URI);
            HttpResponse rs = HttpUtils.getBasicResponse();
            context.setSession(new HttpSession());
            
            h.handle(rq, rs, context);
            
            StatusLine sl = rs.getStatusLine();
            then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
            then(sl.getReasonPhrase()).isEqualTo("resource " + URI + " requires authentication");
            then(rs.getFirstHeader(HttpHeaders.WWW_AUTHENTICATE).getValue()).isEqualTo(AUTHENTICATOR.getMessage());
        }
    }

    @Test
    public void returns_403_on_get_if_wrong_permissions()
    throws Exception {
        AccessControlList acl = new AccessControlList();
        acl.add("ste.web.acl.permissions.private");
        
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, acl, AUTHENTICATOR);
        
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
    public void returns_401_with_wwwauthenticate_on_get_if_unmached_credentials()
    throws Exception {
        HashMapAuthenticator a = new HashMapAuthenticator();
        a.message = UUID.randomUUID().toString();
        
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, null, a);
        
        for (Principal user: USERS) {
            HttpSessionContext session = new HttpSessionContext();
            HttpRequest rq = HttpUtils.getSimpleGet(RESTRICTED_URIS[0]);
            HttpResponse rs = HttpUtils.getBasicResponse();
            session.setPrincipal(user);

            h.handle(rq, rs, session);

            StatusLine sl = rs.getStatusLine();
            then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_UNAUTHORIZED);
            then(sl.getReasonPhrase()).isEqualTo("invalid credentials");
            then(rs.getFirstHeader(HttpHeaders.WWW_AUTHENTICATE).getValue()).isEqualTo(a.getMessage());
        }
    }
    
    @Test
    public void returns_200_on_get_if_mached_credentials()
    throws Exception {
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, null, AUTHENTICATOR);
        
        for (Principal user: USERS) {
            HttpSessionContext context = new HttpSessionContext();
            HttpRequest rq = HttpUtils.getSimpleGet(RESTRICTED_URIS[0]);
            HttpResponse rs = HttpUtils.getBasicResponse();
            context.setSession(new HttpSession());
            context.setPrincipal(user);

            h.handle(rq, rs, context);

            StatusLine sl = rs.getStatusLine();
            then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
            
            //
            // plus, session shall have the principal set
            //
            then(context.getSession().getPrincipal()).isSameAs(context.getPrincipal());
        }
    }
    
    @Test
    public void skip_authentication_if_session_is_already_authenticated() throws Exception {
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, null, AUTHENTICATOR);
        
        final User user = new User("one", "111");
        HttpSessionContext context = new HttpSessionContext();
        HttpRequest rq = HttpUtils.getSimpleGet(RESTRICTED_URIS[0]);
        HttpResponse rs = HttpUtils.getBasicResponse();
        context.setSession(new HttpSession());
        context.setPrincipal(user);

        h.handle(rq, rs, context);

        StatusLine sl = rs.getStatusLine();
        then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        
        //
        // now the session is authenticated
        //
        user.setSecret("none");
        h.handle(rq, rs, context);
        then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }
    
    @Test
    public void get_acl() {
        then(new RestrictedResourceHandler(DUMMY_HANDLER, ACL, AUTHENTICATOR).getAcl()).isSameAs(ACL);
        
        AccessControlList acl = new AccessControlList();
        then(new RestrictedResourceHandler(DUMMY_HANDLER, acl, AUTHENTICATOR).getAcl()).isSameAs(acl);
    }
    
    @Test
    public void no_auth_check_if_acl_is_null() throws Exception {
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, null, AUTHENTICATOR);
        
        HttpSessionContext context = new HttpSessionContext();
        HttpRequest rq = HttpUtils.getSimpleGet(RESTRICTED_URIS[0]);
        HttpResponse rs = HttpUtils.getBasicResponse();
        context.setSession(new HttpSession());
        context.setPrincipal(USERS[0]);

        h.handle(rq, rs, context);

        StatusLine sl = rs.getStatusLine();
        then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(rs.getFirstHeader(HttpHeaders.CONTENT_LOCATION).getValue())
            .isEqualTo(rq.getRequestLine().getUri());
    }
    
    @Test
    public void no_auth_check_if_authenticator_is_null() throws Exception {
        RestrictedResourceHandler h = 
            new RestrictedResourceHandler(DUMMY_HANDLER, ACL, null);
        
        HttpSessionContext context = new HttpSessionContext();
        HttpRequest rq = HttpUtils.getSimpleGet(RESTRICTED_URIS[0]);
        HttpResponse rs = HttpUtils.getBasicResponse();
        Set<String> permissions = new HashSet<>();
        permissions.add("ste.web.acl.permissions.star");

        context.setSession(new HttpSession());
        USERS[0].setPermissions(permissions);
        context.setPrincipal(USERS[0]);

        h.handle(rq, rs, context);

        StatusLine sl = rs.getStatusLine();
        then(sl.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(rs.getFirstHeader(HttpHeaders.CONTENT_LOCATION).getValue())
            .isEqualTo(rq.getRequestLine().getUri());
    }
}
