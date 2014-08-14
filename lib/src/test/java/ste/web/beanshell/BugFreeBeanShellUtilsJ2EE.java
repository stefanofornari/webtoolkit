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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.protocol.HTTP;
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
import ste.web.http.BasicHttpRequest;

import ste.xtest.jetty.TestRequest;
import ste.xtest.jetty.TestResponse;
import ste.xtest.jetty.TestSession;

/**
 * We add some basic tests since the methods are mostly covered by the client
 * classes' tests.
 * 
 * TODO: handle JSON errors when content is JSON
 *
 * @author ste
 */
public class BugFreeBeanShellUtilsJ2EE extends BugFreeBeanShellUtils {

    /**
     * Test of setup method, of class BeanShellUtils.
     */
    @Test
    public void setup() throws Exception {
        
        TestRequest request = new TestRequest();
        request.setUri(new HttpURI(TEST_URI_PARAMETERS));
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setSession(new TestSession());
        HttpServletResponse response = new TestResponse();

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, response);
        
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(TEST_REQ_ATTR_NAME1, Arrays.asList(TEST_VALUE1));
        
        //
        // We need to make sure that after the handling of the request,
        // parameters are not valid variable any more so to avoid that next
        // invocations will inherit them
        //
        checkSetup(i, request.getParameters(), attributes);
    }

    @Test
    public void cleanup() throws Exception {
        Interpreter i = new Interpreter();
        TestRequest request = new TestRequest();
        request.setUri(new HttpURI(TEST_URI_PARAMETERS));
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setSession(new TestSession());
        HttpServletResponse response = new TestResponse();

        BeanShellUtils.setup(i, request, response);
        BeanShellUtils.cleanup(i, request);

        //
        // We need to make sure that after the handling of the request,
        // parameters are not valid variable any more so to avoid that next
        // invocations will inherit them
        //
        checkCleanup(i, request.getParameters().keySet());
    }

    @Test
    public void setRequestAttributes() throws Exception {
        Interpreter i = new Interpreter();
        TestRequest r = new TestRequest();

        try {
            BeanShellUtils.setVariablesAttributes(null, (TestRequest)null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }

        try {
            BeanShellUtils.setVariablesAttributes(null, r);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }

        try {
            BeanShellUtils.setVariablesAttributes(i, (TestRequest)null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }

        i.eval("one=1; two=2;");
        BeanShellUtils.setVariablesAttributes(i, r);
        then(r.getAttribute("one")).isEqualTo(1);
        then(r.getAttribute("two")).isEqualTo(2);
        then(r.getAttribute("three")).isNull(); // just to make sure it
                                                // does not always return
                                                // the same
    }
    
    @Test
    public void bodyAsNotSpecifiedType() throws Exception {
        TestRequest request = new TestRequest();
        request.setUri(new HttpURI(TEST_URI_PARAMETERS));
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setSession(new TestSession());
        request.setContent("one=1&two=2");

        Interpreter i = new Interpreter();
        BeanShellUtils.setup(i, request, new TestResponse());
        
        checkBodyAsNotSpecifiedType(i);
    }
    
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
}
