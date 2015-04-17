/*
 * BeanShell Web
 * Copyright (C) 2015 Stefano Fornari
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
package ste.web.http.api;

import java.net.URISyntaxException;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicRequestLine;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.json.JSONObject;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeRRequest {
    
    public static String TEST_API_URI01 = "/api/save/items/10";
    public static String TEST_API_URI02 = "/api/get/items/10";
    public static String TEST_API_URI03 = "/api/get/none";
    public static String TEST_API_URI04 = "/api/get/items";
    public static String TEST_API_URI05 = "/api/get/items/10/sets/5/subsets/2";
    public static String TEST_API_URI06 = "/api";
    public static String TEST_API_URI07 = "/api/get";
    
    @Test
    public void constructors_with_null() throws Throwable {
        try {
            RRequest r = new RRequest((RequestLine)null);
            fail("missing invalid argument check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("request can not be null");
        }
        
        try {
            RRequest r = new RRequest((String)null);
            fail("missing invalid argument check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("URI may not be null");
        }
    }
    
    @Test
    public void constructor_with_string() throws Throwable {
        RRequest r = new RRequest(TEST_API_URI05);
        
        then(r.getAction()).isEqualTo("get");
        then(r.getResource()).containsExactly("items", "10", "sets", "5", "subsets", "2");
    }
    
    @Test
    public void constructor_with_request_line() throws Throwable {
        RRequest r = new RRequest(new BasicRequestLine("POST", TEST_API_URI05, HttpVersion.HTTP_1_1));
        
        then(r.getAction()).isEqualTo("get");
        then(r.getResource()).containsExactly("items", "10", "sets", "5", "subsets", "2");
    }
    
    @Test
    public void action() throws Throwable {
        RRequest r = new RRequest(TEST_API_URI01);
        then(r.getAction()).isEqualTo("save");
        
        r = new RRequest(TEST_API_URI02);
        then(r.getAction()).isEqualTo("get");
    }

    @Test
    public void get_restful_resource() throws Exception {
        RRequest req = new RRequest(TEST_API_URI01);
        String[] res = req.getResource();
        then(res).containsExactly("items", "10");
        
        req = new RRequest(TEST_API_URI05);
        res = req.getResource();
        then(res).isNotEmpty()
                 .containsExactly("items", "10", "sets", "5", "subsets", "2");
    }

    @Test
    public void invalid_restful_request() throws Exception {
        try {
            RRequest req = new RRequest(TEST_API_URI06);
            fail("invalid request not checked");
        } catch (URISyntaxException x) {
            then(x)
                .hasMessageContaining("invalid rest request")
                .hasMessageContaining(TEST_API_URI06)
                .hasMessageContaining("/api/<action>/<resource>");
        }
        
        try {
            RRequest r = new RRequest(TEST_API_URI07);
            fail("invalid request not checked");
        } catch (URISyntaxException x) {
            then(x)
                .hasMessageContaining("invalid rest request")
                .hasMessageContaining(TEST_API_URI06)
                .hasMessageContaining("/api/<action>/<resource>");
        }
            
    }
    
    @Test
    public void first_resource_element_is_the_handler() throws Exception {
        then(new RRequest(TEST_API_URI04).getHandler()).isEqualTo("items");
        then(new RRequest(TEST_API_URI03).getHandler()).isEqualTo("none");
    }
    
    @Test
    public void get_and_set_body() throws Exception {
        RRequest rr = new RRequest(TEST_API_URI01);
        then(rr.getBody()).isNull();
        
        JSONObject TEST = new JSONObject("{'param1': 'value1'}");
        rr = new RRequest(TEST_API_URI01, TEST);
        then(rr.getBody()).isEqualTo(TEST);
        
        TEST = new JSONObject("{'param2': 'value2'}");
        rr = new RRequest(TEST_API_URI01, TEST);
        then(rr.getBody()).isEqualTo(TEST);
    }
}