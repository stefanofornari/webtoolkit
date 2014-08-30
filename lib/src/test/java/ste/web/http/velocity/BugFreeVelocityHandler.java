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
package ste.web.http.velocity;

import java.net.URI;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicRequestLine;
import org.apache.velocity.exception.ParseErrorException;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.junit.Before;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI04;

import static ste.web.beanshell.Constants.*;
import ste.web.http.HttpSessionContext;
import ste.web.http.HttpUtils;
import ste.web.http.QueryString;


/**
 *
 * @author ste
 */
public class BugFreeVelocityHandler {
    
    public static final String ROOT = "src/test/resources";

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

    private BasicHttpRequest request;
    private BasicHttpResponse response;
    private HttpSessionContext context;
    private VelocityHandler handler;

    public BugFreeVelocityHandler() {
        request = null;
        response = null;
        context = null;
        handler = null;
    }

    @Before
    public void startUp() throws Exception {
        request = new BasicHttpRequest("GET", "index.v");
        response = HttpUtils.getBasicResponse(true);
        context = new HttpSessionContext();
        handler = new VelocityHandler(ROOT);
    }

    @Test
    public void engineSetUpAndNoView() throws Exception {
        then(handler).isNotNull();
        then(handler.getViewsFolder()).isEqualTo(DEFAULT_VIEWS_PREFIX);
        then(handler.getEngine()).isNotNull();
    }

    
    @Test
    public void viewDefaultDirs() throws Exception {
        context.setAttribute(ATTR_VIEW, TEST_VIEW1);
        handler.handle(request, response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void viewNonDefaultDirs() throws Exception {
        handler.setViewsFolder("/views");

        context.setAttribute(ATTR_VIEW, TEST_VIEW3);
        handler.handle(request, response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        handler.setViewsFolder("views");

        context.setAttribute(ATTR_VIEW, TEST_VIEW4);
        handler.handle(request, response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void viewInSubDirs() throws Exception {
        handler.setViewsFolder("/views");

        context.setAttribute(ATTR_VIEW, TEST_VIEW5);
        handler.handle(new BasicHttpRequest("GET", TEST_URI04), response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void viewNotFound() throws Exception {
        context.setAttribute(ATTR_VIEW, TEST_NO_VIEW1);

        handler.handle(request, response, context);
        then(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
        then(response.getStatusLine().getReasonPhrase()).contains(TEST_NO_VIEW1);
    }
    
    /**
     * Velocity views are identified by the .v extension. We replace here 
     * <cde>request.isHandled()</vode> available in jetty with a check on 
     * the content length, but we may need to revisit this.
     *
     * @throws Exception
     */
    @Test
    public void velocityViewOnly() throws Exception {
        context.setAttribute(ATTR_VIEW, TEST_NO_VIEW2);
        handler.handle(request, response, context);
        then(response.getEntity().getContentLength()).isEqualTo(-1);
        
        context.setAttribute(ATTR_VIEW, TEST_NO_VIEW3);
        handler.handle(request, response, context);
        then(response.getEntity().getContentLength()).isEqualTo(-1);
    }

    @Test
    public void viewError() {
        try {
            context.setAttribute(ATTR_VIEW, TEST_ERROR_VIEW1);
            handler.handle(request, response, context);
            fail(TEST_ERROR_VIEW1 + " error shall throw a HttpException");
        } catch (Exception x) {
            //
            // OK
            //
            then(x.getCause()).isInstanceOf(ParseErrorException.class);
        }
    }
    
    @Test
    public void noViewProvided() throws Exception {
        handler.handle(request, response, context);
        then(response.getEntity().getContentLength()).isEqualTo(-1);
    }

    @Test
    public void attributes() throws Exception {
        context.setAttribute(ATTR_VIEW, TEST_VIEW1);
        context.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        context.setAttribute(TEST_REQ_ATTR_NAME2, TEST_VALUE2);
        context.setAttribute(TEST_REQ_ATTR_NAME3, TEST_VALUE3);
        handler.handle(request, response, context);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        then(baos.toString()).isEqualTo(
            String.format("First (%s,%s,%s,%s)", TEST_VIEW1, TEST_VALUE1, TEST_VALUE2, TEST_VALUE3)
        );
    }

    @Test
    public void parameters() throws Exception {
        
        QueryString qs = QueryString.create();
        qs.set(TEST_URL_PARAM1, TEST_VALUE1);
        qs.set(TEST_URL_PARAM2, TEST_VALUE2);
        qs.set(TEST_URL_PARAM3, TEST_VALUE3);
        
        BasicRequestLine req = new BasicRequestLine("GET", qs.apply(new URI("index.v")).toString(), HttpVersion.HTTP_1_1);
        
        context.setAttribute(ATTR_VIEW, TEST_VIEW2);

        handler.setViewsFolder("/views");
        handler.handle(new BasicHttpRequest(req), response, context);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        then(baos.toString()).isEqualTo(
            String.format("Second (%s,%s,%s)", TEST_VALUE1, TEST_VALUE2, TEST_VALUE3)
        );
    }
    // --------------------------------------------------------- Private methods
}
