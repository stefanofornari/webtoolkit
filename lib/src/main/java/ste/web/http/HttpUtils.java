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

import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

/**
 *
 * @author ste
 */
public class HttpUtils {
    public static BasicHttpResponse getBasicResponse(final boolean withEntiry) {
        BasicHttpResponse response = new BasicHttpResponse(
            HttpVersion.HTTP_1_1, HttpStatus.SC_OK, 
            EnglishReasonPhraseCatalog.INSTANCE.getReason(HttpStatus.SC_OK, Locale.ENGLISH)
        );
        
        if (withEntiry) {
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
}
