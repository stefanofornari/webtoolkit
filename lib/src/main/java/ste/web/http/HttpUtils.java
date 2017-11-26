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

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;
import static ste.web.beanshell.BeanShellUtils.CONTENT_TYPE_JSON;

import ste.web.http.message.BasicStatusLine;

/**
 *
 * @author ste
 */
public class HttpUtils {
    public static BasicHttpResponse getBasicResponse(final boolean withEntity) {
        BasicHttpResponse response = new BasicHttpResponse(
            HttpVersion.HTTP_1_1, HttpStatus.SC_OK, 
            EnglishReasonPhraseCatalog.INSTANCE.getReason(HttpStatus.SC_OK, Locale.ENGLISH)
        );
        
        if (withEntity) {
            response.setEntity(new BasicHttpEntity());
        }
        
        return response;
    }
    
    public static BasicHttpResponse getBasicResponse() {
        return getBasicResponse(false);
    }
    
    /**
     * @param response - NOT NULL
     * @param url - NOT EMPTY
     */
    public static void sendTemporaryRedirect(final HttpResponse response, final String url) {
        if (response == null) {
            throw new IllegalArgumentException("response can not be null");
        }
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url can not be empty");
        }
        response.setStatusLine(new BasicStatusLine(
            HttpVersion.HTTP_1_1, HttpStatus.SC_TEMPORARY_REDIRECT,
            EnglishReasonPhraseCatalog.INSTANCE.getReason(HttpStatus.SC_TEMPORARY_REDIRECT, Locale.ENGLISH)
        ));
        response.setHeader(HttpHeaders.LOCATION, url);
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
        if (headers.length == 0) {
            return false;
        }
        
        String contentType = headers[0].getValue();
        return CONTENT_TYPE_JSON.equals(contentType)
            || contentType.startsWith(CONTENT_TYPE_JSON + ";");
    }
    
    public static BasicHttpRequest getSimpleGet(final String uri) {
        return new BasicHttpRequest("GET", uri);
    }
    
    public static Pair<String, String> parseBasicAuth(final Header authorization) {
        String authString = getAuthString(authorization);
        
        Pair<String, String> pair = null;
        
        if (authString != null) {
            int p = authString.indexOf(':');
            if (p>0) {
                String login = authString.substring(0, p);
                pair = new MutablePair<>(login, authString.substring(p+1));
            } else if (p<0) {
                pair = new MutablePair<>(authString, null);
            }
        }
        
        return pair;
    }
    
    public static String extractSessionId(String name, String cookies) {
        final String DELIMITER = name + "=";
        final int DELIMITER_SIZE = DELIMITER.length();
        
        if (cookies != null) {
            int i = cookies.lastIndexOf(DELIMITER);
            if (i>=0) {
                int e = cookies.indexOf(";", i+DELIMITER_SIZE);
                return (e >= 0) ? cookies.substring(i+DELIMITER_SIZE, e).replace("\"", "") 
                                : cookies.substring(i+DELIMITER_SIZE).replace("\"", "");
            }
        }
        
        return null;
    }
    
    public static String extractSessionId(String cookies) {
        return extractSessionId(SessionHeader.DEFAULT_SESSION_HEADER, cookies);
    }
    
    // --------------------------------------------------------- private methods
    
    private static String getAuthString(final Header authorization) {
        if (authorization == null) {
            return null;
        }
        
        String value = authorization.getValue();
        
        if ((value == null) || !value.startsWith("Basic ") || (value.length() == 6)) {
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
