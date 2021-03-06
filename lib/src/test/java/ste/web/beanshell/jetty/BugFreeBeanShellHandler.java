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
package ste.web.beanshell.jetty;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Test;
import static org.junit.Assert.*;
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
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI08;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI09;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI10;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI_PARAMETERS;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_VALUE1;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_VALUE2;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_VALUE3;

import static ste.web.beanshell.Constants.*;
import ste.xtest.jetty.TestRequest;
import ste.xtest.jetty.TestResponse;
import ste.xtest.jetty.TestSession;

/**
 *
 * @author ste
 */
public class BugFreeBeanShellHandler {

    protected TestRequest request;
    protected TestResponse response;
    protected Server server;
    protected BeanShellHandler handler;

    public BugFreeBeanShellHandler() {
        request = null;
        response = null;
        server = null;
        handler = null;
    }

    @Before
    public void setUp() throws Exception {
        request = new TestRequest();
        request.setUri(new HttpURI(TEST_URI_PARAMETERS));
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setAttribute(TEST_REQ_ATTR_NAME2, TEST_VALUE2);
        request.setAttribute(TEST_REQ_ATTR_NAME3, TEST_VALUE3);
        response = new TestResponse();
        handler = new BeanShellHandler();
        server = new Server();
        server.setAttribute(ATTR_APP_ROOT, "src/test/webroot");

        handler.setServer(server);
        simulateStart(handler);
    }

    @Test
    public void interpreterSetUp() throws Exception {
        assertNotNull(handler.getInterpreter());
        assertNull(new BeanShellHandler().getInterpreter());
    }

    @Test
    public void execScriptDefaultDirs() throws Exception {
        handler.handle(TEST_URI01, request, request, response);
        assertFalse(request.isHandled());
        assertNotNull(handler.getInterpreter().get("first"));

        handler.handle(TEST_URI02, request, request, response);
        assertNotNull(handler.getInterpreter().get("second"));
        assertFalse(request.isHandled());
    }

    @Test
    public void execScriptNonDefaultDirs() throws Exception {
        handler.setControllersFolder("controllers");

        handler.handle(TEST_URI03, request, request, response);
        assertNotNull(handler.getInterpreter().get("firstcontroller"));

        handler.handle(TEST_URI04, request, request, response);
        assertNotNull(handler.getInterpreter().get("secondcontroller"));
    }

    @Test
    public void scriptNotFound() throws Exception {
        handler.handle(TEST_URI05, request, request, response);
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
        assertTrue(response.getStatusMessage().indexOf(TEST_URI05)>=0);
    }

    @Test
    public void scriptError() {
        try {
            handler.handle(TEST_URI06, request, request, response);
            fail(TEST_URI06 + " error shall throw a ServletException");
        } catch (ServletException e) {
            //
            // OK
            //
            assertTrue(e.getCause() instanceof EvalError);
        } catch (Exception e) {
            fail(TEST_URI06 + " error shall throw a ServletException instead of " + e);
        }

        try {
            handler.handle(TEST_URI07, request, request, response);
            fail(TEST_URI07 + " error shall throw a ServletException");
        } catch (ServletException e) {
            //
            // OK
            //
            assertTrue(e.getCause() instanceof EvalError);
        } catch (Exception e) {
            fail(TEST_URI07 + " error shall throw a ServletException instead of " + e);
        }
    }

    @Test
    public void execBshOnly() throws Exception {
        handler.handle(TEST_URI08, request, request, response);
        assertFalse(request.isHandled());
    }

    @Test
    public void setMainVariables() throws Exception {
        HttpSession session = new TestSession();

        request.setSession(session);

        handler.handle(TEST_URI01, request, request, response);

        Interpreter i = handler.getInterpreter();
        assertSame(i.get(VAR_REQUEST), request);
        assertSame(i.get(VAR_RESPONSE), response);
        assertSame(i.get(VAR_SESSION), session);
        assertNotNull(i.get(VAR_LOG));
        assertNotNull(i.get(VAR_OUT));
        assertEquals(
            new File((String)server.getAttribute(ATTR_APP_ROOT), TEST_URI01).getAbsolutePath(),
            handler.getInterpreter().get(VAR_SOURCE)
        );
    }

    @Test
    public void returnView() throws Exception {
        handler.handle(TEST_URI01, request, request, response);
        Interpreter i = handler.getInterpreter();
        assertEquals("main.v", i.get(ATTR_VIEW));
    }

    @Test
    public void missingView() throws Exception {
        try {
            handler.handle(TEST_URI10, request, request, response);
            fail(TEST_URI06 + " error shall throw a ServletException");
        } catch (ServletException e) {
            assertTrue(e.getMessage().contains("view not defined"));
        }
    }

    @Test
    public void requestParameters() throws Exception {
        handler.handle(TEST_URI09, request, request, response);
        Interpreter i = handler.getInterpreter();

        assertEquals(
            String.format("%s,%s,%s", TEST_VALUE1, TEST_VALUE2, TEST_VALUE3),
            i.get("parameters")
        );

        //
        // We need to make sure that after the handling of the request,
        // parameters are not valid variable any more so to avoid that next
        // invocations will inherit them
        //
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            assertNull(i.get(params.nextElement()));
        }
    }

    @Test
    public void requestAttributes() throws Exception {
        handler.handle(TEST_URI01, request, request, response);
        Interpreter i = handler.getInterpreter();
        Enumeration<String> attrs = request.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String attr = attrs.nextElement();
            assertEquals(request.getAttribute(attr), i.get(BeanShellUtils.normalizeVariableName(attr)));
        }
    }

    @Test
    public void variablesAttribute() throws Exception {
        handler.handle(TEST_URI01, request, request, response);
        Interpreter i = handler.getInterpreter();
        assertTrue((Boolean)request.getAttribute("first"));
        assertNull(request.getAttribute("something")); // just to make sure it
                                                       // does not always return
                                                       // the same
    }

    // --------------------------------------------------------- Private methods

    private void simulateStart(AbstractHandler h) throws Exception {
        Method m = BeanShellHandler.class.getDeclaredMethod("doStart");
        m.setAccessible(true);
        m.invoke(h);
    }
}
