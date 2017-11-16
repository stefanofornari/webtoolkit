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

import java.io.File;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Test;
import org.junit.Before;
import ste.web.beanshell.BeanShellError;
import ste.web.beanshell.BeanShellUtils;
import static ste.web.beanshell.BugFreeBeanShellUtils.*;
import static ste.web.beanshell.Constants.*;
import ste.web.http.BasicHttpConnection;
import ste.web.http.HttpSessionContext;
import ste.xtest.net.TestSocket;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

/**
 *
 * @author ste
 */
public class BugFreeBeanShellHandler {

    private static final String GET = "GET";
    private static final String ROOT = "src/test/webroot";

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
    public void before() throws Exception {
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
    public void constructors() throws Exception {
        try {
            new BeanShellHandler(null);
            fail("missing check for null parameters");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("webroot").contains("not be null");
        }

        BeanShellHandler h = new BeanShellHandler(new File(ROOT).getAbsolutePath());
        then(h.getControllersFolder()).isNull();

        h = new BeanShellHandler(new File(ROOT).getAbsolutePath(), "/c");
        then(h.getControllersFolder()).isEqualTo("/c");

        h = new BeanShellHandler(new File(ROOT).getAbsolutePath(), null);
        then(h.getControllersFolder()).isNull();

        h = new BeanShellHandler(new File(ROOT).getAbsolutePath(), "/a");
        then(h.getControllersFolder()).isEqualTo("/a");
    }

    @Test
    public void exec_script_default_dirs() throws Exception {
        handler.handle(get(TEST_URI01), response, context);
        then(context.get("first")).isNotNull();

        handler.handle(get(TEST_URI02), response, context);
        then(context.get("second")).isNotNull();
    }

    @Test
    public void exec_script_non_default_dirs() throws Exception {
        handler.setControllersFolder("controllers");

        handler.handle(get(TEST_URI03), response, context);
        then(context.get("firstcontroller")).isNotNull();

        handler.handle(get(TEST_URI04), response, context);
        then(context.get("secondcontroller")).isNotNull();
    }

    @Test
    public void script_not_found() throws Exception {
        handler.handle(get(TEST_URI05), response, context);
        then(HttpStatus.SC_NOT_FOUND).isEqualTo(response.getStatusLine().getStatusCode());
        then(response.getStatusLine().getReasonPhrase()).contains(TEST_URI05);
    }

    @Test
    public void script_error() throws Exception {
        try {
            handler.handle(get(TEST_URI06), response, context);
            fail(TEST_URI06 + " error shall throw a HttpException");
        } catch (HttpException x) {
            then(x.getCause()).isInstanceOf(BeanShellError.class);
        }

        try {
            handler.handle(get(TEST_URI07), response, context);
            fail(TEST_URI07 + " error shall throw a HttpException");
        } catch (HttpException x) {
            //
            // OK
            //
            then(x.getCause()).isInstanceOf(BeanShellError.class);
            BeanShellError cause = (BeanShellError)x.getCause();
            then(cause.getCause()).isInstanceOf(IOException.class);
        }
    }

    @Test
    public void set_main_variables() throws Exception {
        BasicHttpRequest request = get(TEST_URI01);
        handler.handle(request, response, context);

        then(context.get(VAR_REQUEST)).isSameAs(request);
        then(context.get(VAR_RESPONSE)).isSameAs(response);
        then(context.get(VAR_SESSION)).isSameAs(context);
        then(context.get(VAR_LOG)).isNotNull();
        then(context.get(VAR_OUT)).isNotNull();
        then(new File(ROOT, TEST_URI01).getAbsolutePath())
                .isEqualTo(context.get(VAR_SOURCE));
    }

    @Test
    public void return_view() throws Exception {
        handler.handle(get(TEST_URI01), response, context);
        then(context.get(ATTR_VIEW)).isEqualTo("main.v");
    }

    @Test
    public void missing_view() throws Exception {
        try {
            handler.handle(get(TEST_URI10), response, context);
            fail(TEST_URI06 + " error shall throw a ServletException");
        } catch (HttpException e) {
            then(e.getMessage()).contains("view not defined");
        }
    }

    @Test
    public void request_attributes() throws Exception {
        handler.handle(get(TEST_URI01), response, context);
        for (String name : context.keySet()) {
            then(context.get(BeanShellUtils.normalizeVariableName(name))).isEqualTo(context.getAttribute(name));
        }
    }

    @Test
    public void variables_attribute() throws Exception {
        handler.handle(get(TEST_URI01), response, context);
        then((boolean) context.getAttribute("first")).isTrue();
        then(context.getAttribute("something")).isNull(); // just to make sure it
        // does not always return
        // the same
    }

    @Test
    public void variables_parameters() throws Exception {
        handler.handle(get(TEST_URI_PARAMETERS), response, context);
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
                    handler.handle(get("/multithreading.bsh"), response, CTX1);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.handle(get("/multithreading.bsh"), response, CTX2);
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

    // --------------------------------------------------------- private methods
    
    private BasicHttpConnection getConnection() throws IOException {
        BasicHttpConnection c = new BasicHttpConnection();
        c.bind(new TestSocket());

        return c;
    }

    private BasicHttpRequest get(final String uri) {
        return new BasicHttpRequest(GET, uri);
    }
}
