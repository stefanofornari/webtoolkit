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
import java.util.Enumeration;
import javax.servlet.http.HttpServletResponse;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.eclipse.jetty.http.HttpURI;
import org.junit.Test;

import static ste.web.beanshell.Constants.*;
import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_REQ_ATTR_NAME1;
import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_URI_PARAMETERS;
import static ste.web.beanshell.jetty.BugFreeBeanShellHandler.TEST_VALUE1;

import ste.xtest.jetty.TestRequest;
import ste.xtest.jetty.TestResponse;
import ste.xtest.jetty.TestSession;

/**
 * We add some basic tests since the methods are mostly covered by the client
 * classes' tests.
 *
 * @author ste
 */
public class BugFreeBeanShellUtils {

    public BugFreeBeanShellUtils() {
    }

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

    /**
     * Test of setup method, of class BeanShellUtils.
     */
    @Test
    public void setup() throws Exception {
        Interpreter i = new Interpreter();
        TestRequest request = new TestRequest();
        request.setUri(new HttpURI(TEST_URI_PARAMETERS));
        request.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        request.setSession(new TestSession());
        HttpServletResponse response = new TestResponse();

        BeanShellUtils.setup(i, request, response);

        then(i.get(VAR_REQUEST)).isNotNull();
        then(i.get(VAR_RESPONSE)).isNotNull();
        then(i.get(VAR_SESSION)).isNotNull();
        then(i.get(VAR_OUT)).isNotNull();
        then(i.get(VAR_LOG)).isNotNull();

        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            then(i.get(param)).isEqualTo(request.getParameter(param));
        }
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
        BeanShellUtils.cleanup(i, request, response);

        //
        // We need to make sure that after the handling of the request,
        // parameters are not valid variable any more so to avoid that next
        // invocations will inherit them
        //
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            then(i.get(params.nextElement())).isNull();
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

    @Test
    public void setRequestAttributes() throws Exception {
        Interpreter i = new Interpreter();
        TestRequest r = new TestRequest();

        try {
            BeanShellUtils.setVariablesAttributes(null, null);
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
            BeanShellUtils.setVariablesAttributes(i, null);
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
}
