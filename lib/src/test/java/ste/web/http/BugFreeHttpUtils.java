/*
 * Copyright (C) 2014 Stefano Fornari.
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

package ste.web.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HTTP;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.Test;
import ste.web.beanshell.BeanShellUtils;
import static ste.web.beanshell.BeanShellUtils.CONTENT_TYPE_JSON;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI_PARAMETERS;

/**
 *
 * @author ste
 */
public class BugFreeHttpUtils {
    @Test
    public void createSimpleResponse() {
        HttpResponse r = HttpUtils.getBasicResponse();
        StatusLine l = r.getStatusLine();
        then(l).isNotNull();
        then(l.getProtocolVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        then(l.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(l.getReasonPhrase()).isEqualTo("OK");
    }
    
    @Test
    public void createSimpleResponseWithEntity() {
        HttpResponse r = HttpUtils.getBasicResponse(true);
        then(r.getEntity()).isNotNull().isInstanceOf(BasicHttpEntity.class);
    }
    
    @Test
    public void sendTemporaryRedirectOK() {
        HttpResponse r = HttpUtils.getBasicResponse();
        HttpUtils.sendTemporaryRedirect(r, "newurl.html");
        
        then(r.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_TEMPORARY_REDIRECT);
        then(r.getLastHeader(HttpHeader.LOCATION.asString()).getValue()).isEqualTo("newurl.html");
    }
    
    @Test
    public void sentTemporaryRedirectWithNullOrEmptyParametetrs() {
        try {
            HttpUtils.sendTemporaryRedirect(null, "something");
            fail("missing check for not nullable parameters");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("response can not be null");
        }
        
        final HttpResponse r = HttpUtils.getBasicResponse();
        final String[] BLANKS = new String[] {null, "", " ", "  "};
        
        for(String BLANK: BLANKS) {
            try {
                HttpUtils.sendTemporaryRedirect(r, BLANK);
                fail("missing check for not emptyable parameters");
            } catch (IllegalArgumentException x) {
                then(x.getMessage()).contains("url can not be empty");
            }
        }
    }
    
    @Test
    public void hasJSONBody() throws Exception {
        BasicHttpRequest request = new BasicHttpRequest("GET", TEST_URI_PARAMETERS);
        then(HttpUtils.hasJSONBody(request)).isFalse();
        
        request.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        then(HttpUtils.hasJSONBody(request)).isFalse();
        
        request.setHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON);
        then(HttpUtils.hasJSONBody(request)).isTrue();
        
        request.setHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON + "; cherset=UTF8");
        then(HttpUtils.hasJSONBody(request)).isTrue();
    }
}
