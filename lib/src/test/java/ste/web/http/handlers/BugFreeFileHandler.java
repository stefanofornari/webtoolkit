/*
 * Web Toolkit Library
 * Copyright (C) 2014 Stefano Fornari
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
package ste.web.http.handlers;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.web.http.HttpSessionContext;
import ste.web.http.HttpUtils;


/**
 * TODO: malformed URL
 * 
 * @author ste
 */
public class BugFreeFileHandler {
    
    @Test
    public void mimeTypeBasedOnFileExtension() throws Exception {
        FileHandler h = new FileHandler("src/test/mime");
        
        BasicHttpRequest request = new BasicHttpRequest("GET", "/test.txt");
        BasicHttpResponse response = HttpUtils.getBasicResponse();
        
        h.handle(request, response, new HttpSessionContext());
        
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getEntity().getContentType().getValue()).isEqualTo(ContentType.TEXT_PLAIN.getMimeType());
        
        request = new BasicHttpRequest("GET", "/test.html");
        h.handle(request, response, new HttpSessionContext());
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getEntity().getContentType().getValue()).isEqualTo(ContentType.TEXT_HTML.getMimeType());
        
        request = new BasicHttpRequest("GET", "/test.png");
        h.handle(request, response, new HttpSessionContext());
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getEntity().getContentType().getValue()).isEqualTo(ContentType.create("image/png").getMimeType());
    }
    
    @Test
    public void defaultMimeTypeIsOctetBinary() throws Exception {
        FileHandler h = new FileHandler("src/test/mime");
        
        BasicHttpRequest request = new BasicHttpRequest("GET", "/test.bin");
        BasicHttpResponse response = HttpUtils.getBasicResponse();
        
        h.handle(request, response, new HttpSessionContext());
        
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getEntity().getContentType().getValue()).isEqualTo(ContentType.APPLICATION_OCTET_STREAM.getMimeType());
    }
    
    @Test
    public void notFound() throws Exception {
        FileHandler h = new FileHandler("src/test/mime");
        
        BasicHttpRequest request = new BasicHttpRequest("GET", "/none.bin");
        BasicHttpResponse response = HttpUtils.getBasicResponse();
        
        h.handle(request, response, new HttpSessionContext());
        
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
        then(IOUtils.toString(response.getEntity().getContent())).contains("src/test/mime/none.bin");
    }
    
    @Test
    public void ignoreQueryString() throws Exception {
        FileHandler h = new FileHandler("src/test/mime");
        
        BasicHttpResponse response = HttpUtils.getBasicResponse();
        
        
        for (String q: new String[] {"", "p1", "p1=v1", "p1=v1&", "p1=v1&p2=v2"}) {
            BasicHttpRequest request = new BasicHttpRequest("GET", "/test.html?");
                
            h.handle(request, response, new HttpSessionContext());
        
            then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        }
    }
}
