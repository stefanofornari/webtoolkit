/*
 * BeanShell Web
 * Copyright (C) 2015 Stefano Fornari
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
package ste.web.http.api;

import java.io.File;
import java.io.IOException;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Before;
import static ste.web.beanshell.BugFreeBeanShellUtils.*;
import ste.web.http.BasicHttpConnection;
import ste.web.http.HttpSessionContext;
import ste.xtest.net.TestSocket;

/**
 * 
 * @author ste
 */
public abstract class BugFreeApiHandlerBase {

    protected static final String GET = "GET";
    protected final String ROOT;
    
    protected BasicHttpRequest request;
    protected BasicHttpResponse response;
    protected ApiHandler handler;
    protected HttpSessionContext context;
    

    protected BugFreeApiHandlerBase(final String root) {
        request = null;
        response = null;
        handler = null;
        context = null;
        ROOT = root;
    }

    @Before
    public void setup() throws Exception {
        request = request(TEST_URI_PARAMETERS);
        context = new HttpSessionContext();
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, getConnection());
        context.setAttribute(TEST_REQ_ATTR_NAME1, TEST_VALUE1);
        context.setAttribute(TEST_REQ_ATTR_NAME2, TEST_VALUE2);
        context.setAttribute(TEST_REQ_ATTR_NAME3, TEST_VALUE3);
        response = new BasicHttpResponse(
                new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK")
        );
        response.setEntity(new BasicHttpEntity());
        handler = new ApiHandler(new File(ROOT).getAbsolutePath());
    }

    // --------------------------------------------------------- Private methods
            
    protected BasicHttpConnection getConnection() throws IOException {
        BasicHttpConnection c = new BasicHttpConnection();
        c.bind(new TestSocket());

        return c;
    }

    protected BasicHttpRequest request(final String uri) {
        return new BasicHttpRequest(GET, uri(uri));
    }
    
    protected String uri(final String uri) {
        return uri.replace(".bsh", "");
    }
}
