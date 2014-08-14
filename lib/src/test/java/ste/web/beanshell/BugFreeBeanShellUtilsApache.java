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
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpCoreContext;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.json.JSONException;
import org.junit.Test;
import static ste.web.beanshell.BeanShellUtils.CONTENT_TYPE_JSON;

import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_REQ_ATTR_NAME1;
import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_URI_PARAMETERS;
import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_VALUE1;
import ste.web.http.BasicHttpConnection;
import ste.web.http.HttpSessionContext;
import ste.web.http.QueryString;

import ste.xtest.net.TestSocket;

/**
 * We add some basic tests since the methods are mostly covered by the client
 * classes' tests.
 *
 * @author ste
 */
public class BugFreeBeanShellUtilsApache extends BugFreeBeanShellUtils {
    
    private static final BasicHttpResponse RESPONSE_OK = 
        new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");

    /**
     * Test of setup method, of class BeanShellUtils.
     */
    @Test
    public void setup() throws Exception {
        BasicHttpRequest request = new BasicHttpRequest("get", TEST_URI_PARAMETERS);
        HttpSessionContext context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        context.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, RESPONSE_OK, context);
        
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(TEST_REQ_ATTR_NAME1, Arrays.asList(TEST_VALUE1));
        
        checkSetup(i, QueryString.parse(request.getRequestLine().getUri()).getMap(), attributes);
    }

    @Test
    public void cleanup() throws Exception {
        BasicHttpRequest request = new BasicHttpRequest("get", TEST_URI_PARAMETERS);
        HttpSessionContext context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        context.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, RESPONSE_OK, context);
        BeanShellUtils.cleanup(i, request);

        //
        // We need to make sure that after the handling of the request,
        // parameters are not valid variable any more so to avoid that next
        // invocations will inherit them
        //
        checkCleanup(i, QueryString.parse(request.getRequestLine().getUri()).getNames());
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

    @Test
    public void bodyAsNotSpecifiedType() throws Exception {
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("get", TEST_URI_PARAMETERS);
        HttpSessionContext context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        request.setEntity(new StringEntity("one=1&two=2"));

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, RESPONSE_OK, context);
        
        checkBodyAsNotSpecifiedType(i);
    }
    
    @Test
    public void bodyAsJSONObject() throws Exception {
        final String TEST_LABEL1 = "label1";
        final String TEST_LABEL2 = "label2";
        final String TEST_VALUE1 = "a first label";
        final String TEST_VALUE2 = "a second label";
        
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("get", TEST_URI_PARAMETERS);
        HttpSessionContext context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        request.addHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON);
        request.setEntity(
            new StringEntity(
                String.format(
                    "{%s:'%s'}", 
                    TEST_LABEL1, TEST_VALUE1
                )
            )
        );

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, RESPONSE_OK, context);
        
        checkJSONObject(i, TEST_LABEL1, TEST_VALUE1);
        
        request.setEntity(
            new StringEntity(
                String.format(
                    "[{%s:'%s'}, {%s:'%s'}]",  
                    TEST_LABEL1, TEST_VALUE1,
                    TEST_LABEL2, TEST_VALUE2
                )
            )
        );
        
        BeanShellUtils.setup(i, request, RESPONSE_OK, context);
        
        checkJSONArray(
            i, 
            new String[] {TEST_LABEL1, TEST_LABEL2},
            new String[] {TEST_VALUE1, TEST_VALUE2}
        );
    }
    
    @Test
    public void bodyAsJSONObjectWithCharset() throws Exception {
        final String TEST_LABEL1 = "label1";
        final String TEST_VALUE1 = "a first label";
        
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("get", TEST_URI_PARAMETERS);
        HttpSessionContext context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        request.addHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON + ";charset=utf-8");
        request.setEntity(
            new StringEntity(
                String.format(
                    "{%s:'%s'}", 
                    TEST_LABEL1, TEST_VALUE1
                )
            )
        );
        
        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, RESPONSE_OK, context);
        
        checkJSONObject(i, TEST_LABEL1, TEST_VALUE1);
    }
    
    @Test
    public void bodyAsCorruptedJSONObject() throws Exception {
        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("get", TEST_URI_PARAMETERS);
        HttpSessionContext context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        request.addHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON);
        
        Interpreter i = new Interpreter();
        for (String s: TEST_JSON_ERRORS) {
            request.setEntity(new StringEntity(s));
            try {
                BeanShellUtils.setup(i, request, RESPONSE_OK, context);
                fail("JSONException not thrown for <" + s + ">");
            } catch (IOException x) {
                then(x.getCause()).isNotNull().isInstanceOf(JSONException.class);
            }
        }
    }
    
    // --------------------------------------------------------- private methods
    
    private BasicHttpConnection getConnection() throws IOException {
        BasicHttpConnection c = new BasicHttpConnection();
        c.bind(new TestSocket());
        
        return c;
    }
}
