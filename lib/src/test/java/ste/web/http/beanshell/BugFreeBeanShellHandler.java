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
package ste.web.http.beanshell;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.File;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpCoreContext;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.junit.Before;
import ste.web.beanshell.BeanShellUtils;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_REQ_ATTR_NAME1;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_REQ_ATTR_NAME2;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_REQ_ATTR_NAME3;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI01;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI02;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI03;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI04;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI05;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI06;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI07;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI10;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI_PARAMETERS;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_VALUE1;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_VALUE2;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_VALUE3;
import static ste.web.beanshell.Constants.ATTR_VIEW;
import static ste.web.beanshell.Constants.VAR_LOG;
import static ste.web.beanshell.Constants.VAR_OUT;
import static ste.web.beanshell.Constants.VAR_REQUEST;
import static ste.web.beanshell.Constants.VAR_RESPONSE;
import static ste.web.beanshell.Constants.VAR_SESSION;
import static ste.web.beanshell.Constants.VAR_SOURCE;
import ste.web.http.BasicHttpConnection;
import ste.web.http.HttpSessionContext;
import ste.xtest.net.TestSocket;


/**
 *
 * @author ste
 */
public class BugFreeBeanShellHandler {
    
    private static final String GET = "GET";
    private static final String ROOT = "src/test/resources";


    protected BasicHttpRequest request;
    protected BasicHttpResponse response;
    protected BeanShellHandler handler;
    protected HttpSessionContext context;

    public BugFreeBeanShellHandler() {
        request = null;
        response = null;
        handler = null;
    }

    @Before
    public void setUp() throws Exception {
        request = new BasicHttpRequest("GET", TEST_URI_PARAMETERS);
        context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        context.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        context.setAttribute(TEST_REQ_ATTR_NAME2, TEST_VALUE2);
        context.setAttribute(TEST_REQ_ATTR_NAME3, TEST_VALUE3);
        response = new BasicHttpResponse(
            new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK")
        );
        handler = new BeanShellHandler(new File(ROOT).getAbsolutePath());
    }

    @Test
    public void interpreterSetUp() throws Exception {
        then(new BeanShellHandler().getInterpreter()).isNotNull();
    }

    @Test
    public void execScriptDefaultDirs() throws Exception {
        handler.handle(get(TEST_URI01), response, context);
        then(handler.getInterpreter().get("first")).isNotNull();

        handler.handle(get(TEST_URI02), response, context);
        then(handler.getInterpreter().get("second")).isNotNull();
    }

    @Test
    public void execScriptNonDefaultDirs() throws Exception {
        handler.setControllersFolder("controllers");

        handler.handle(get(TEST_URI03), response, context);
        then(handler.getInterpreter().get("firstcontroller")).isNotNull();

        handler.handle(get(TEST_URI04), response, context);
        then(handler.getInterpreter().get("secondcontroller")).isNotNull();
    }

    @Test
    public void scriptNotFound() throws Exception {
        handler.handle(get(TEST_URI05), response, context);
        then(HttpStatus.SC_NOT_FOUND).isEqualTo(response.getStatusLine().getStatusCode());
        then(response.getStatusLine().getReasonPhrase()).contains(TEST_URI05);
    }

    @Test
    public void scriptError() throws IOException {
        try {
            handler.handle(get(TEST_URI06), response, context);
            fail(TEST_URI06 + " error shall throw a HttpException");
        } catch (HttpException x) {
            then(x.getCause()).isInstanceOf(EvalError.class);
        }

        try {
            handler.handle(get(TEST_URI07), response, context);
            fail(TEST_URI07 + " error shall throw a HttpException");
        } catch (HttpException x) {
            //
            // OK
            //
            then(x.getCause()).isInstanceOf(EvalError.class);
        } catch (Exception x) {
            fail(TEST_URI07 + " error shall throw a HttpException instead of " + x);
        }
    }

    /**
     * TODO: Not doable with httpcore; do we need it?
     * @throws Exception 
     */
    /*
    @Test
    public void execBshOnly() throws Exception {
        handler.handle(get(TEST_URI08), response, context);
        assertFalse(request.isHandled());
    }
    */
    
    @Test
    public void setMainVariables() throws Exception {
        BasicHttpRequest request = get(TEST_URI01);
        handler.handle(request, response, context);

        Interpreter i = handler.getInterpreter();
        then(i.get(VAR_REQUEST)).isSameAs(request);
        then(i.get(VAR_RESPONSE)).isSameAs(response);
        then(i.get(VAR_SESSION)).isSameAs(context);
        then(i.get(VAR_LOG)).isNotNull();
        then(i.get(VAR_OUT)).isNotNull();
        then(new File(ROOT, TEST_URI01).getAbsolutePath())
            .isEqualTo(handler.getInterpreter().get(VAR_SOURCE));
    }

    @Test
    public void returnView() throws Exception {
        handler.handle(get(TEST_URI01), response, context);
        Interpreter i = handler.getInterpreter();
        then(i.get(ATTR_VIEW)).isEqualTo("main.v");
    }

    @Test
    public void missingView() throws Exception {
        try {
            handler.handle(get(TEST_URI10), response, context);
            fail(TEST_URI06 + " error shall throw a ServletException");
        } catch (HttpException e) {
            then(e.getMessage()).contains("view not defined");
        }
    }

    @Test
    public void requestAttributes() throws Exception {
        handler.handle(get(TEST_URI01), response, context);
        Interpreter i = handler.getInterpreter();
        for(String name: context.keySet()) {
            System.out.println("key: " + name + ", value: " + context.getAttribute(name));
            then(i.get(BeanShellUtils.normalizeVariableName(name))).isEqualTo(context.getAttribute(name));
        }
    }

    @Test
    public void variablesAttribute() throws Exception {
        handler.handle(get(TEST_URI01), response, context);
        Interpreter i = handler.getInterpreter();
        then((boolean)context.getAttribute("first")).isTrue();
        then(context.getAttribute("something")).isNull(); // just to make sure it
                                                          // does not always return
                                                          // the same
    }

    // --------------------------------------------------------- Private methods
    
    private BasicHttpConnection getConnection() throws IOException {
        BasicHttpConnection c = new BasicHttpConnection();
        c.bind(new TestSocket());
        
        return c;
    }
    
    private BasicHttpRequest get(final String uri) {
        return new BasicHttpRequest(GET, uri);
    }
}
