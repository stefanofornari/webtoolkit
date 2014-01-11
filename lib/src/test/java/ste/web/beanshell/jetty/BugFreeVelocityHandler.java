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

import java.lang.reflect.Method;
import javax.servlet.ServletException;
import org.apache.velocity.exception.ParseErrorException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.MultiMap;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

import static ste.web.beanshell.Constants.*;
import ste.xtest.jetty.TestRequest;
import ste.xtest.jetty.TestResponse;


/**
 *
 * @author ste
 */
public class BugFreeVelocityHandler {

    public static final String TEST_URL_PARAM1 = "p_one";
    public static final String TEST_URL_PARAM2 = "p_two";
    public static final String TEST_URL_PARAM3 = "p_three";

    public static final String TEST_REQ_ATTR_NAME1 = "a_one";
    public static final String TEST_REQ_ATTR_NAME2 = "a_two";
    public static final String TEST_REQ_ATTR_NAME3 = "a_three";

    public static final String TEST_VALUE1 = "uno";
    public static final String TEST_VALUE2 = "due";
    public static final String TEST_VALUE3 = "tre";

    public static final String TEST_VIEW1    = "first.v";
    public static final String TEST_VIEW2    = "second.v";
    public static final String TEST_VIEW3    = "third.v";
    public static final String TEST_VIEW4    = "fourth.v";
    public static final String TEST_VIEW5    = "secondlevelview.v";
    public static final String TEST_NO_VIEW1 = "notexisting.v";
    public static final String TEST_NO_VIEW2 = "invalidview";
    public static final String TEST_NO_VIEW3 = "invalidview.a";

    public static final String TEST_ERROR_VIEW1 = "witherror.v";

    private TestRequest request;
    private TestResponse response;
    private Server server;
    private VelocityHandler handler;

    public BugFreeVelocityHandler() {
        request = null;
        response = null;
        server = null;
        handler = null;
    }

    @Before
    public void startUp() throws Exception {
        request = new TestRequest();
        response = new TestResponse();

        handler = new VelocityHandler();
        server = new Server();
        server.setAttribute(ATTR_APP_ROOT, "src/test/resources");

        handler.setServer(server);
        simulateStart(handler);
    }

    @Test
    public void engineSetUp() throws Exception {
        assertNotNull(handler.getEngine());
        assertNull(new VelocityHandler().getEngine());
        assertEquals(DEFAULT_VIEWS_PREFIX, handler.getViewsFolder());
    }

    @Test
    public void noView() throws Exception {
        handler.handle("", request, request, response);
        assertFalse(request.isHandled());
    }

    @Test
    public void viewDefaultDirs() throws Exception {
        request.setAttribute(ATTR_VIEW, TEST_VIEW1);
        handler.handle("", request, request, response);
        assertEquals(200, response.getStatus());
        assertTrue(request.isHandled());
    }

    @Test
    public void viewNonDefaultDirs() throws Exception {
        handler.setViewsFolder("/views");

        request.setAttribute(ATTR_VIEW, TEST_VIEW3);
        handler.handle("", request, request, response);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertTrue(request.isHandled());

        handler.setViewsFolder("views");

        request.setAttribute(ATTR_VIEW, TEST_VIEW4);
        handler.handle("", request, request, response);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertTrue(request.isHandled());

    }

    @Test
    public void viewInSubDirs() throws Exception {
        handler.setViewsFolder("/views");

        request.setAttribute(ATTR_VIEW, TEST_VIEW5);
        handler.handle(BugFreeBeanShellHandler.TEST_URI04, request, request, response);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertTrue(request.isHandled());
    }

    @Test
    public void viewNotFound() throws Exception {
        request.setAttribute(ATTR_VIEW, TEST_NO_VIEW1);

        handler.handle("", request, request, response);
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
        assertTrue(response.getStatusMessage().indexOf(TEST_NO_VIEW1)>=0);
    }

    /**
     * Velocity views are identified by the .v extension
     *
     * @throws Exception
     */
    @Test
    public void velocityViewOnly() throws Exception {
        request.setAttribute(ATTR_VIEW, TEST_NO_VIEW2);
        handler.handle("", request, request, response);
        assertFalse(request.isHandled());
        request.setAttribute(ATTR_VIEW, TEST_NO_VIEW3);
        handler.handle("", request, request, response);
        assertFalse(request.isHandled());
    }

    @Test
    public void viewError() {
        try {
            request.setAttribute(ATTR_VIEW, TEST_ERROR_VIEW1);
            handler.handle("", request, request, response);
            fail(TEST_ERROR_VIEW1 + " error shall throw a ServletException");
        } catch (ServletException e) {
            //
            // OK
            //
            assertTrue(e.getCause() instanceof ParseErrorException);
        } catch (Exception e) {
            fail(TEST_ERROR_VIEW1 + " error shall throw a ServletException");
        }
    }

    @Test
    public void attributes() throws Exception {
        request.setAttribute(ATTR_VIEW, TEST_VIEW1);
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setAttribute(TEST_REQ_ATTR_NAME2, TEST_VALUE2);
        request.setAttribute(TEST_REQ_ATTR_NAME3, TEST_VALUE3);
        handler.handle("", request, request, response);
        assertEquals(
            String.format("First (%s,%s,%s,%s)", TEST_VIEW1, TEST_VALUE1, TEST_VALUE2, TEST_VALUE3),
            response.getText()
        );
    }

    @Test
    public void parameters() throws Exception {
        MultiMap parameters = new MultiMap();

        parameters.add(TEST_URL_PARAM1, TEST_VALUE1);
        parameters.add(TEST_URL_PARAM2, TEST_VALUE2);
        parameters.add(TEST_URL_PARAM3, TEST_VALUE3);

        request.setParameters(parameters);
        request.setAttribute(ATTR_VIEW, TEST_VIEW2);

        handler.setViewsFolder("/views");
        handler.handle("", request, request, response);
        assertEquals(
            String.format("Second (%s,%s,%s)", TEST_VALUE1, TEST_VALUE2, TEST_VALUE3),
            response.getText()
        );
    }

    // --------------------------------------------------------- Private methods

    private void simulateStart(AbstractHandler h) throws Exception {
        Method m = VelocityHandler.class.getDeclaredMethod("doStart");
        m.setAccessible(true);
        m.invoke(h);
    }
}
