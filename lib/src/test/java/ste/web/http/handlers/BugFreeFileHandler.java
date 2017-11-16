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
    public void mime_type_based_on_file_extension() throws Exception {
        FileHandler h = new FileHandler("src/test/mime");
        
        BasicHttpRequest request = HttpUtils.getSimpleGet("/test.txt");
        BasicHttpResponse response = HttpUtils.getBasicResponse();
        
        h.handle(request, response, new HttpSessionContext());
        
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getEntity().getContentType().getValue()).isEqualTo(ContentType.TEXT_PLAIN.getMimeType());
        
        request = HttpUtils.getSimpleGet("/test.html");
        h.handle(request, response, new HttpSessionContext());
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getEntity().getContentType().getValue()).isEqualTo(ContentType.TEXT_HTML.getMimeType());
        
        request = HttpUtils.getSimpleGet("/test.png");
        h.handle(request, response, new HttpSessionContext());
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getEntity().getContentType().getValue()).isEqualTo(ContentType.create("image/png").getMimeType());
    }
    
    @Test
    public void default_mime_type_is_octet_binary() throws Exception {
        FileHandler h = new FileHandler("src/test/mime");
        
        BasicHttpRequest request = HttpUtils.getSimpleGet("/test.bin");
        BasicHttpResponse response = HttpUtils.getBasicResponse();
        
        h.handle(request, response, new HttpSessionContext());
        
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(response.getEntity().getContentType().getValue()).isEqualTo(ContentType.APPLICATION_OCTET_STREAM.getMimeType());
    }
    
    @Test
    public void not_found() throws Exception {
        FileHandler h = new FileHandler("src/test/mime");
        
        BasicHttpRequest request = HttpUtils.getSimpleGet("/none.bin");
        BasicHttpResponse response = HttpUtils.getBasicResponse();
        
        h.handle(request, response, new HttpSessionContext());
        
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
        then(IOUtils.toString(response.getEntity().getContent())).contains("/none.bin");
    }
    
    @Test
    public void ignore_query_string() throws Exception {
        FileHandler h = new FileHandler("src/test/mime");
        
        BasicHttpResponse response = HttpUtils.getBasicResponse();

        for (String q: new String[] {"", "p1", "p1=v1", "p1=v1&", "p1=v1&p2=v2"}) {
            BasicHttpRequest request = HttpUtils.getSimpleGet("/test.html?");
                
            h.handle(request, response, new HttpSessionContext());
        
            then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        }
    }
    
    @Test
    public void exclude_files_in_exclude_pattern() throws Exception {
        FileHandler h = new FileHandler("src/test/webroot", "null");
        
        h.exclude("(.*)\\.bsh");
        
        BasicHttpResponse response = HttpUtils.getBasicResponse();
        BasicHttpRequest request = HttpUtils.getSimpleGet("/firstlevelscript.bsh");
        h.handle(request, response, new HttpSessionContext());
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
        
        request = HttpUtils.getSimpleGet("/some/parameters.bsh");
        h.handle(request, response, new HttpSessionContext());
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }
    
    @Test
    public void set_exclude_pattern() {
        final String TEST_PATTERN1 = "(.*)\\.bsh";
        final String TEST_PATTERN2 = "[a-z]";
                
        FileHandler h = new FileHandler("src/test/webroot", "null");
        then(h.getExcludes()).isEmpty();
        
        then(h.exclude(TEST_PATTERN1)).isSameAs(h);
        then(h.getExcludes()).containsExactly(TEST_PATTERN1);
        
        h.exclude(TEST_PATTERN1, TEST_PATTERN2);
        then(h.getExcludes()).containsExactly(TEST_PATTERN1, TEST_PATTERN2);
        
        h.exclude(null);
        then(h.getExcludes()).isEmpty();
    }
    
}
