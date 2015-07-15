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

import bsh.EvalError;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpCoreContext;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.json.JSONObject;
import org.junit.Test;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_QUERY_STRING;
import static ste.web.beanshell.Constants.*;
import ste.web.http.HttpSessionContext;
import ste.web.http.HttpUtils;
import ste.web.http.beanshell.BeanShellUtils;
import ste.xtest.reflect.PrivateAccess;

/**
 * 
 * @author ste
 */
public class BugFreeApiHandlerExec extends BugFreeApiHandlerBase {
    
    public static final String TEST_URI_WITHEVALERROR = "/api/app/get/withevalerror";
    public static final String TEST_URI_WITHTARGETERROR = "/api/app/get/withtargeterror";
    public static final String TEST_URI_ITEMS1 = "/api/store/get/items";
    public static final String TEST_URI_ITEMS2 = "/api/store/get/items2";
    public static final String TEST_URI_ITEMS3 = "/api/store/get/items3";
    public static final String TEST_URI_ITEMS4 = "/api/store/get/items4";
    public static final String TEST_URI_ITEMS5 = "/api/store/get/items5";
    public static final String TEST_URI_PARAMETERS = "/api/app/get/parameters?" + TEST_QUERY_STRING;
    
    public BugFreeApiHandlerExec() {
        super("src/test/apiroot");
    }

    @Test
    public void constructors() throws Exception {
        try {
            new ApiHandler(null);
            fail("missing check for null parameters");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("apiroot").contains("not be null");
        }

        String root = new File("src/test/apiroot").getAbsolutePath();
        ApiHandler h = new ApiHandler(root);
        then(PrivateAccess.getInstanceValue(h, "apiroot")).isEqualTo(root);
    }

    @Test
    public void script_error() throws IOException {
        try {
            handler.handle(request(TEST_URI_WITHEVALERROR), response, context);
            fail(TEST_URI_WITHEVALERROR + " error shall throw a HttpException");
        } catch (HttpException x) {
            then(x.getCause()).isInstanceOf(EvalError.class);
        }

        try {
            handler.handle(request(TEST_URI_WITHTARGETERROR), response, context);
            fail(TEST_URI_WITHTARGETERROR + " error shall throw a HttpException");
        } catch (HttpException x) {
            then(x.getCause()).isInstanceOf(EvalError.class);
        } catch (Exception x) {
            fail(TEST_URI_WITHTARGETERROR + " error shall throw a HttpException instead of " + x);
        }
    }

    @Test
    public void set_main_variables() throws Exception {
        BasicHttpRequest request = request(TEST_URI_ITEMS1);
        handler.handle(request, response, context);

        then(context.get(VAR_REQUEST)).isSameAs(request);
        then(context.get(VAR_RESPONSE)).isSameAs(response);
        then(context.get(VAR_SESSION)).isSameAs(context);
        then(context.get(VAR_LOG)).isNotNull();
        then(context.get(VAR_OUT)).isNotNull();
        then(new File("src/test/apiroot/store/items/items.bsh").getAbsolutePath())
                .isEqualTo(context.get(VAR_SOURCE));
    }


    @Test
    public void request_attributes() throws Exception {
        handler.handle(request(TEST_URI_ITEMS1), response, context);
        for (String name : context.keySet()) {
            System.out.println("key: " + name + ", value: " + context.getAttribute(name));
            then(context.get(BeanShellUtils.normalizeVariableName(name))).isEqualTo(context.getAttribute(name));
        }
    }

    @Test
    public void variables_attribute() throws Exception {
        handler.handle(request(TEST_URI_ITEMS1), response, context);
        then((boolean) context.getAttribute("first")).isTrue();
        // just to make sure it does not always return the same
        then(context.getAttribute("something")).isNull(); 
    }

    @Test
    public void variables_parameters() throws Exception {
        handler.handle(request(TEST_URI_PARAMETERS), response, context);
        then(context.get("p1")).isEqualTo("uno");
        then(context.get("p2")).isEqualTo("due");
        then(context.get("p3")).isEqualTo("tre");
    }

    @Test
    public void running_multiple_thread_in_different_contexts() throws Exception {
        final HttpSessionContext CTX1 = new HttpSessionContext();
        final HttpSessionContext CTX2 = new HttpSessionContext();
        CTX1.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        CTX2.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.handle(request("/api/app/get/multithreading"), response, CTX1);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.handle(request("/api/app/get/multithreading"), response, CTX2);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        then(CTX1.get("view")).isEqualTo(t1.getName());
        then(CTX2.get("view")).isEqualTo(t2.getName());
    }
    
    @Test
    public void content_type_is_json_if_not_already_set() throws Exception {
        response.setEntity(new BasicHttpEntity());
        handler.handle(request(TEST_URI_ITEMS1), response, context);
        then(response.getEntity().getContentType().getValue()).isEqualTo("application/json");
    }
    
    @Test
    public void content_type_is_unchanged_if_already_set() throws Exception {
        BasicHttpEntity e = new BasicHttpEntity();
        e.setContentType("text/plain");
        response.setEntity(e);
        handler.handle(request(TEST_URI_ITEMS1), response, context);
        then(response.getEntity().getContentType().getValue()).isEqualTo("text/plain");
    }
    
    @Test
    public void return_content_in_the_body_as_string() throws Exception {
        handler.handle(request(TEST_URI_ITEMS1), response, context);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        
        JSONObject o = new JSONObject(baos.toString());
        then(o.getString("one")).isEqualTo("111");
        then(o.getString("two")).isEqualTo("222");
        then(response.getEntity().getContentLength()).isEqualTo(24);
    }
    
    @Test
    public void return_content_in_the_body_as_json_object() throws Exception {
        handler.handle(request(TEST_URI_ITEMS2), response, context);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        
        JSONObject o = new JSONObject(baos.toString());
        then(o.getString("one")).isEqualTo("111");
        then(o.getString("two")).isEqualTo("222");
        then(response.getEntity().getContentLength()).isEqualTo(25);
    }
    
    @Test
    /**
     * When the content is returned as file (the result variable is of type
     * java.io.File, the response body shall contain an EntityFile.
     */
    public void return_content_in_the_body_as_file() throws Exception {
        BasicHttpRequest request = request(TEST_URI_ITEMS5+"?file=/tmp/afile.txt");
        BasicHttpResponse response = HttpUtils.getBasicResponse(true);
        
        handler.handle(request, response, context);
        
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(context.getAttribute("items5")).isEqualTo(new File("/tmp/afile.txt"));
        then(response.getEntity()).isInstanceOf(FileEntity.class);
        then(response.getEntity().getContentType().getValue()).isEqualTo("text/plain");
        
        request = request(TEST_URI_ITEMS5+"?file=/anotherfile.txt");
        response = HttpUtils.getBasicResponse(true);
        handler.handle(request, response, context);
        
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(context.getAttribute("items5")).isEqualTo(new File("/anotherfile.txt"));
        then(response.getEntity()).isInstanceOf(FileEntity.class);
    }
    
    
    @Test
    public void return_no_content_if_no_body() throws Exception {
        handler.handle(request(TEST_URI_ITEMS3), response, context);
        
        then(response.getEntity().getContentLength()).isEqualTo(-1);
    }
    
    @Test
    public void utf8_support() throws Exception {
        handler.handle(request(TEST_URI_ITEMS4), response, context);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        
        //
        // The issue with non-ascii characters is that the actual number of 
        // bytes to transfer may be different be the number of characters 
        // returned by String.length()
        //
        byte[] bytes = baos.toByteArray();
        String json = new String(Arrays.copyOf(bytes, (int)response.getEntity().getContentLength()));
        JSONObject o = new JSONObject(json);
        then(o.getString("two")).isEqualTo("papà");
    }
    
    

    // --------------------------------------------------------- Private methods
}
