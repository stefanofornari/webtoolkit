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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import static org.junit.Assert.*;
import ste.web.beanshell.jelly.mock.TestRequest;
import ste.web.beanshell.jelly.mock.TestResponse;

import static ste.web.beanshell.Constants.*;

/**
 * We add some basic tests since the methods are mostly covered by the client
 * classes' tests. 
 * 
 * @author ste
 */
public class BeanShellUtilsTest {
    
    public BeanShellUtilsTest() {
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
        assertTrue(
            BeanShellUtils.getScript(
                new File("src/test/resources/firstlevelscript.bsh")
            ).indexOf("first = true;") >= 0
        );
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
    public void testSetup() throws Exception {
        Interpreter i = new Interpreter();
        HttpServletRequest request = new TestRequest();
        HttpServletResponse response = new TestResponse();
        BeanShellUtils.setup(i, request, response);
        
        assertNotNull(i.get(VAR_REQUEST));
        assertNotNull(i.get(VAR_RESPONSE));
        assertNotNull(i.get(VAR_OUT));
        assertNotNull(i.get(VAR_LOG));
    }
}
