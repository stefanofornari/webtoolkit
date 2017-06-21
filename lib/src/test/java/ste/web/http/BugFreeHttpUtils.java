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

import java.util.Base64;
import java.util.Base64.Encoder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HTTP;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.Test;
import static ste.web.beanshell.BeanShellUtils.CONTENT_TYPE_JSON;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI_PARAMETERS;

/**
 *
 * @author ste
 */
public class BugFreeHttpUtils {
    @Test
    public void create_simple_response() {
        HttpResponse r = HttpUtils.getBasicResponse();
        StatusLine l = r.getStatusLine();
        then(l).isNotNull();
        then(l.getProtocolVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        then(l.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(l.getReasonPhrase()).isEqualTo("OK");
    }
    
    @Test
    public void create_simple_response_with_entity() {
        HttpResponse r = HttpUtils.getBasicResponse(true);
        then(r.getEntity()).isNotNull().isInstanceOf(BasicHttpEntity.class);
    }
    
    @Test
    public void send_temporary_redirect_ok() {
        HttpResponse r = HttpUtils.getBasicResponse();
        HttpUtils.sendTemporaryRedirect(r, "newurl.html");
        
        then(r.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_TEMPORARY_REDIRECT);
        then(r.getLastHeader(HttpHeader.LOCATION.asString()).getValue()).isEqualTo("newurl.html");
    }
    
    @Test
    public void sent_temporary_redirect_with_null_or_empty_parametetrs() {
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
    public void has_JSON_body() throws Exception {
        BasicHttpRequest request = new BasicHttpRequest("GET", TEST_URI_PARAMETERS);
        then(HttpUtils.hasJSONBody(request)).isFalse();
        
        request.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        then(HttpUtils.hasJSONBody(request)).isFalse();
        
        request.setHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON);
        then(HttpUtils.hasJSONBody(request)).isTrue();
        
        request.setHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON + "; cherset=UTF8");
        then(HttpUtils.hasJSONBody(request)).isTrue();
    }
    
    @Test
    public void get_simple_get() throws Exception {
        final String[] URIS = new String[] {
            "/test1", "index.html", "/test/test1/test2"
        };
        
        for (String URI: URIS) {
            BasicHttpRequest r = HttpUtils.getSimpleGet(URI);

            then(r).isNotNull();
            RequestLine l = r.getRequestLine();
            then(l.getMethod()).isEqualTo("GET");
            then(l.getUri()).isEqualTo(URI);
        }
    }
    
    @Test
    public void parse_basic_auth_ok() throws Exception {
        final Pair<String, String>[] PAIRS = new Pair[] {
            new ImmutablePair<>("abcd", "1234"),
            new ImmutablePair<>("defg", "5678"),
            new ImmutablePair<>("hij", ""),
        };
        
        Encoder b64 = Base64.getEncoder();
        
        for (Pair<String, String> p: PAIRS) {
            then(HttpUtils.parseBasicAuth(
                new BasicHeader(
                    HttpHeaders.AUTHORIZATION, 
                    "Basic " + b64.encodeToString((p.getLeft() + ':' + p.getRight()).getBytes("UTF-8"))
                )
            )).isEqualTo(p);
        }
        
        Pair<String, String> p = new ImmutablePair<>("klm", null);
        then(HttpUtils.parseBasicAuth(
            new BasicHeader(
                HttpHeaders.AUTHORIZATION, 
                "Basic " + b64.encodeToString(p.getLeft().getBytes("UTF-8"))
            )
        )).isEqualTo(p);
    }
    
    @Test
    public void parse_basic_auth_ko() throws Exception {
        Encoder b64 = Base64.getEncoder();
        
        final String[] PAIRS = new String[] {
            null,
            "",
            "Basic ",
            "Basic " + b64.encodeToString("".getBytes("UTF-8")),
            "Basic " + b64.encodeToString(":onlypassword".getBytes("UTF-8")),
            "something " + b64.encodeToString("abc:123".getBytes("UTF-8")),
            "somethingsimple"
        };
        
        for (String P: PAIRS) {
            then(HttpUtils.parseBasicAuth(
                new BasicHeader(HttpHeaders.AUTHORIZATION, P)
            )).isNull();
        }
    }
    
    @Test
    public void extract_jsessionid_from_cookies() throws Exception {
        final String[] COOKIES = new String[] {
          "JSESSIONID=123456;", "JSESSIONID=\"123456\"", "JSESSIONID=123456",
            "COOKIE1=one;JSESSIONID=123456;COOKIE2=two", "COOKIE1=one;JSESSIONID=123456;",
            "JSESSIONID=123456;COOKIE1=two"
        };
        
        for(String C: COOKIES) {
            System.out.println(C);
            then(HttpUtils.extractSessionId(C)).isEqualTo("123456");
        }
    }
    
    @Test
    public void extract_jsession_from_cookies_null_if_not_found() throws Exception {
        final String[] COOKIES = new String[] {
          "SESSIONID=123456;", "", null, " ", "\t"
        };
        
        for(String C: COOKIES) {
            System.out.println(C);
            then(HttpUtils.extractSessionId(C)).isNull();
        }
    }
}
