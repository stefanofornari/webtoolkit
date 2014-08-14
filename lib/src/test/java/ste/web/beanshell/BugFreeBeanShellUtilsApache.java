/*
 * BeanShell Web
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
package ste.web.beanshell;

import bsh.Interpreter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpCoreContext;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.eclipse.jetty.http.HttpURI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import static ste.web.beanshell.BeanShellUtils.CONTENT_TYPE_JSON;

import static ste.web.beanshell.Constants.*;
import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_REQ_ATTR_NAME1;
import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_URI_PARAMETERS;
import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_VALUE1;
import ste.web.http.BasicHttpConnection;
import ste.web.http.BasicHttpRequest;
import ste.web.http.HttpSessionContext;

import ste.xtest.jetty.TestRequest;
import ste.xtest.jetty.TestResponse;
import ste.xtest.jetty.TestSession;
import ste.xtest.net.TestSocket;

/**
 * We add some basic tests since the methods are mostly covered by the client
 * classes' tests.
 * 
 * TODO: handle JSON errors when content is JSON
 *
 * @author ste
 */
public class BugFreeBeanShellUtilsApache extends BugFreeBeanShellUtils {

    /**
     * Test of setup method, of class BeanShellUtils.
     */
    @Test
    public void setup() throws Exception {
        BasicHttpRequest request = new BasicHttpRequest(TEST_URI_PARAMETERS);
        BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        HttpSessionContext context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        context.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, response, context);
        
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(TEST_REQ_ATTR_NAME1, Arrays.asList(TEST_VALUE1));
        
        checkSetup(i, request.getQueryString().getMap(), attributes);
    }

    @Test
    public void cleanup() throws Exception {
        BasicHttpRequest request = new BasicHttpRequest(TEST_URI_PARAMETERS);
        BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        HttpSessionContext context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        context.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, response, context);
        BeanShellUtils.cleanup(i, request);

        //
        // We need to make sure that after the handling of the request,
        // parameters are not valid variable any more so to avoid that next
        // invocations will inherit them
        //
        checkCleanup(i, request.getQueryString().getNames());
    }

    @Test
    public void setRequestAttributes() throws Exception {
        Interpreter i = new Interpreter();
        BasicHttpContext c = new BasicHttpContext();

        try {
            BeanShellUtils.setVariablesAttributes(null, (BasicHttpContext)null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }

        try {
            BeanShellUtils.setVariablesAttributes(null, c);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }

        try {
            BeanShellUtils.setVariablesAttributes(i, (BasicHttpContext)null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }

        i.eval("one=1; two=2;");
        BeanShellUtils.setVariablesAttributes(i, c);
        then(c.getAttribute("one")).isEqualTo(1);
        then(c.getAttribute("two")).isEqualTo(2);
        then(c.getAttribute("three")).isNull(); // just to make sure it
                                                // does not always return
                                                // the same
    }
    /*
    @Test
    public void bodyAsNotSpecifiedType() throws Exception {
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("get", TEST_URI_PARAMETERS);
        BasicHttpContext context = new BasicHttpContext();
        context.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setEntity(new StringEntity("one=1&two=2"));

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, new TestResponse());
        
        Object o = i.get(VAR_BODY);
        then(o).isNull();
    }*/
    
    @Test
    public void bodyAsJSONObject() throws Exception {
        final String TEST_LABEL1 = "label1";
        final String TEST_LABEL2 = "label2";
        final String TEST_VALUE1 = "a first label";
        final String TEST_VALUE2 = "a second label";
        
        TestRequest request = new TestRequest();
        request.setUri(new HttpURI(TEST_URI_PARAMETERS));
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setSession(new TestSession());
        request.setContentType("application/json");
        request.setContent(String.format("{%s:'%s'}", TEST_LABEL1, TEST_VALUE1));

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, new TestResponse());
        
        Object o = i.get(VAR_BODY);
        then(o).isNotNull().isInstanceOf(JSONObject.class);
        then(((JSONObject)o).getString(TEST_LABEL1)).isEqualTo(TEST_VALUE1);
        
        request.setContent(
            String.format(
                "[{%s:'%s'}, {%s:'%s'}]",  
                TEST_LABEL1, TEST_VALUE1,
                TEST_LABEL2, TEST_VALUE2
            )
        );
        
        BeanShellUtils.setup(i, request, new TestResponse());
        
        o = i.get(VAR_BODY);
        then(o).isNotNull().isInstanceOf(JSONArray.class);
        JSONArray a = (JSONArray)o;
        then(a.length()).isEqualTo(2); 
        o = a.getJSONObject(0);
        then(((JSONObject)o).getString(TEST_LABEL1)).isEqualTo(TEST_VALUE1);
        o = a.getJSONObject(1);
        then(((JSONObject)o).getString(TEST_LABEL2)).isEqualTo(TEST_VALUE2);
    }
    
    @Test
    public void bodyAsJSONObjectWithCharset() throws Exception {
        final String TEST_LABEL1 = "label1";
        final String TEST_LABEL2 = "label2";
        final String TEST_VALUE1 = "a first label";
        final String TEST_VALUE2 = "a second label";
        
        TestRequest request = new TestRequest();
        request.setUri(new HttpURI(TEST_URI_PARAMETERS));
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setSession(new TestSession());
        request.setContentType("application/json;charset=utf-8");
        request.setContent(String.format("{%s:'%s'}", TEST_LABEL1, TEST_VALUE1));

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, new TestResponse());
        
        Object o = i.get(VAR_BODY);
        then(o).isNotNull().isInstanceOf(JSONObject.class);
        then(((JSONObject)o).getString(TEST_LABEL1)).isEqualTo(TEST_VALUE1);
    }
    
    @Test
    public void bodyAsCorruptedJSONObject() throws Exception {
        TestRequest request = new TestRequest();
        request.setUri(new HttpURI(TEST_URI_PARAMETERS));
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setSession(new TestSession());
        request.setContentType("application/json");
        
        final String[] ERRORS = {
            "", "   ", "\t ",
            "a string", "{ 'label': value", "'label': 'value'", "[{} {}]",
            "[{} {}]", "['uno', 'due'", "'tre', 'quattro']"
        };
        
        Interpreter i = new Interpreter();
        for (String e: ERRORS) {
            request.setContent(e);
            try {
                BeanShellUtils.setup(i, request, new TestResponse());
                fail("JSONException not thrown for <" + e + ">");
            } catch (IOException x) {
                then(x.getCause()).isNotNull().isInstanceOf(JSONException.class);
            }
        }
    }
    
    @Test
    public void hasJSONObjectApache() throws Exception {
        BasicHttpRequest r = new BasicHttpRequest("index.html");
        then(BeanShellUtils.hasJSONBody(r)).isFalse();
        
        r.addHeader(HTTP.CONTENT_TYPE, "text/plain");
        then(BeanShellUtils.hasJSONBody(r)).isFalse();
        
        r.removeHeaders(HTTP.CONTENT_TYPE);
        r.addHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON);
        then(BeanShellUtils.hasJSONBody(r)).isTrue();
    }
    
    // --------------------------------------------------------- private methods
    
    private BasicHttpConnection getConnection() throws IOException {
        BasicHttpConnection c = new BasicHttpConnection();
        c.bind(new TestSocket());
        
        return c;
    }
}
