/*
 * Cip&Ciop
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
package ste.web.beanshell.console;

import org.eclipse.jetty.server.Response;
import static org.junit.Assert.*;
import        org.junit.Test;

import org.eclipse.jetty.server.session.HashSessionManager;
import ste.web.beanshell.Constants;
import ste.web.beanshell.jelly.test.TestRequest;
import ste.web.beanshell.test.BeanShellTest;

/**
 *
 * @author ste
 */
public class ConsoleControllerTest extends BeanShellTest 
                               implements Constants {
    
    public static final String TEST_VALUE1 = "Hello world";
    public static final String TEST_SCRIPT1 = "s = \"%s\";";
    public static final String TEST_SCRIPT2 = "print(\"%s\");";
    public static final String TEST_SCRIPT_ERROR = "an error";
    public static final String TEST_VIEW = "main.v";
    
    public ConsoleControllerTest() throws Exception {
        setCommandsDirectory("src/main/webapp/WEB-INF/commands");
        setBshFileName("src/main/webapp/console/c/exec.bsh");
    }
    
    @Override
    protected void beanshellSetup() throws Exception {
        TestRequest r = new TestRequest();
        r.setSessionManager(new HashSessionManager());
        
        beanshell.set("request", r);
        beanshell.set("source", "src/main/webapp/console/c/exec.bsh");
        beanshell.set("response", new Response());
        //beanshell.set("session", r.getSession());
    }
    
    @Test
    public void noScript() throws Exception {
        //
        // Nothing for now
        //
    }
    
    @Test
    public void execScript() throws Exception {
        beanshell.set("script", String.format(TEST_SCRIPT1, TEST_VALUE1));
        
        exec();
        
        TestRequest r = (TestRequest)beanshell.get("request");
        assertEquals(TEST_VALUE1, beanshell.get("s"));
        assertEquals(TEST_VIEW, (String)r.getAttribute(ATTR_VIEW));
    }
    
    @Test
    public void captureStdout() throws Exception {
        beanshell.set("script", String.format(TEST_SCRIPT2, TEST_VALUE1));
        
        exec();
        
        assertEquals(TEST_VALUE1+"\n", beanshell.get("result"));
    
    }
    
    @Test
    public void scriptError() throws Exception {
        beanshell.set("script", TEST_SCRIPT_ERROR);
        
        exec();
        
        String result =(String)beanshell.get("result");
        assertTrue(result.startsWith("Error"));
    }
}
