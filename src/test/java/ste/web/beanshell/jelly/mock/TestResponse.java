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
package ste.web.beanshell.jelly.mock;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 *
 * @author ste
 */
public class TestResponse extends org.eclipse.jetty.server.Response {
    
    ByteArrayOutputStream buffer;
    PrintWriter out;
    
    public TestResponse() {
        super();
        buffer = new ByteArrayOutputStream();
        out = new PrintWriter(buffer);
    }
    
    @Override
    public PrintWriter getWriter() {
        return out;
    }
    
    @Override
    public void setStatus(final int status) {
        this.status = status;
    }
    
    @Override
    public int getStatus() {
        return status;
    }
    
    public String getResponseAsString() {
        return buffer.toString();
    }
}
