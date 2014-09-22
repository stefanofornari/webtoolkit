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

package ste.web.http.handlers;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import ste.web.http.MimeUtils;

public class FileHandler implements HttpRequestHandler  {

    protected final String docRoot;
    protected final String webContext;

    /**
     * Creates a new FileHandler that reads files located under docRoot, which
     * is taken as parent of the URI.
     * 
     * @param docRoot the root directory for files - NOT NULL
     * @param webContext the base uri for previews - NOT BLANK
     *
     * @throws IllegalArgumentException if any of the parameter has an illegal
     *         value
     */
    public FileHandler(final String docRoot, final String webContext) {
        if (docRoot == null) {
            throw new IllegalArgumentException("docRoot cannot be null");
        }
        this.docRoot = docRoot;
        this.webContext = webContext;
    }


    public FileHandler(final String docRoot) {
        this(docRoot, null);
    }

    public void handle(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        
        checkHttpMethod(request);
        
        String target = request.getRequestLine().getUri();
        if (StringUtils.isNotBlank(webContext) && target.startsWith(webContext)) {
            target = target.substring(webContext.length());
        }

        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            byte[] entityContent = EntityUtils.toByteArray(entity);
        }

        final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
        if (!file.exists()) {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            StringEntity entity = new StringEntity(
                    "<html><body><h1>File " + file.getPath() +
                    " not found</h1></body></html>",
                    ContentType.TEXT_HTML);
            response.setEntity(entity);
        } else if (!file.canRead() || file.isDirectory()) {
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            StringEntity entity = new StringEntity(
                    "<html><body><h1>Access denied</h1></body></html>",
                    ContentType.TEXT_HTML);
            response.setEntity(entity);
        } else {
            response.setStatusCode(HttpStatus.SC_OK);
            
            String mimeType = MimeUtils.getInstance().getMimeType(file);
            ContentType type = MimeUtils.MIME_UNKNOWN.equals(mimeType)
                             ? ContentType.APPLICATION_OCTET_STREAM
                             : ContentType.create(mimeType)
                             ;
            FileEntity body = new FileEntity(file, type);
            response.setEntity(body);
        }
    }

    // --------------------------------------------------------- private methods
    
    private void checkHttpMethod(final HttpRequest request) throws MethodNotSupportedException {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
    }
}
