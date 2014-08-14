/*
 * BeanShell Web
 * Copyright (C) 2012 Stefano Fornari
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * classes' tests. Note that specific behaviour for different types of containers
 * are specified in dedicated bug free classes.
 * 
 * TODO: handle JSON errors when content is JSON
 *
 * @author ste
 */
public class BugFreeBeanShellUtils {

    @Test
    public void getScriptNull() throws Exception {
        try {
            BeanShellUtils.getScript(null);
            fail("getScript cannot be invoched with null");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }
    }

    @Test
    public void getScript() throws Exception {
        then(
            BeanShellUtils.getScript(
                new File("src/test/resources/firstlevelscript.bsh")
            )
        ).contains("first = true;");
    }

    @Test
    public void getNotExistingScript() throws Exception {
        try {
            BeanShellUtils.getScript(new File("src/test/resources/notexistingscript.bsh"));
            fail("file not found exception expected");
        } catch (FileNotFoundException e) {
            //
            // OK
            //
        }
    }
    
    // ------------------------------------------------------- protected methods

    /**
     * 
     * @param i
     * @param parameters
     * @param attributes
     * 
     * @throws java.lang.Exception
     */
    protected void checkSetup(
        Interpreter i, 
        Map<String, List<String>> parameters,
        Map<String, List<String>> attributes
    ) throws Exception {
        then(i.get(VAR_REQUEST)).isNotNull();
        then(i.get(VAR_RESPONSE)).isNotNull();
        then(i.get(VAR_SESSION)).isNotNull();
        then(i.get(VAR_OUT)).isNotNull();
        then(i.get(VAR_LOG)).isNotNull();

        for (String name: parameters.keySet()) {
            then(i.get(name)).isEqualTo(parameters.get(name).get(0));
        }
        
        for (String name: attributes.keySet()) {
            then(i.get(name)).isEqualTo(attributes.get(name).get(0));
        }
    }

    protected void checkCleanup(
        Interpreter i, 
        Set<String> parameters
    ) throws Exception {
        //
        // We need to make sure that after the handling of the request,
        // parameters are not valid variable any more so to avoid that next
        // invocations will inherit them
        //
        for (String name: parameters) {
            then(i.get(name)).isNull();
        }

        //
        // Make sure we do not unset too much :)
        //
        then(i.get(VAR_REQUEST)).isNotNull();
        then(i.get(VAR_RESPONSE)).isNotNull();
        then(i.get(VAR_SESSION)).isNotNull();
        then(i.get(VAR_OUT)).isNotNull();
        then(i.get(VAR_LOG)).isNotNull();
    }
    
    protected void checkBodyAsNotSpecifiedType(final Interpreter i) throws Exception {
        then(i.get(VAR_BODY)).isNull();
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
