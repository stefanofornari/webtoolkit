/*
 * BeanShell Web
 * Copyright (C) 2013 Stefano Fornari
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

import java.io.File;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI01;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI02;
import static ste.web.beanshell.BugFreeBeanShellUtils.TEST_URI06;

import static ste.web.beanshell.Constants.*;
import ste.xtest.logging.ListLogHandler;

/**
 *
 * @author ste
 */
public class BugFreeBeanShellHandlerLogging extends BugFreeBeanShellHandler {

    private static final Logger LOG = Logger.getLogger(LOG_NAME);
    private static ListLogHandler H = null;

    public BugFreeBeanShellHandlerLogging()  {
        super();
    }

    @BeforeClass
    public static void setUpClass() {
        for (Handler h: LOG.getHandlers()) {
            LOG.removeHandler(h);
        }
        LOG.addHandler(H = new ListLogHandler());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        //
        // remove existing records if any
        //
        LOG.getHandlers()[0].flush();
    }

    @Test
    public void logAtFineForServedURIAndScript() throws Exception {
        LOG.setLevel(Level.ALL);

        handler.handle(TEST_URI01, request, request, response);

        List<String> messages = H.getMessages();
        System.out.println("messages: " + messages);
        assertTrue(messages.contains("serving " + TEST_URI01));
        assertTrue(messages.contains("script path: " + new File("src/test/resources", TEST_URI01).getAbsolutePath()));
        assertTrue(messages.contains("view: main.v"));

        handler.handle(TEST_URI02, request, request, response);
        messages = H.getMessages();
        assertTrue(messages.contains("serving " + TEST_URI02));
        assertTrue(messages.contains("script path: " + new File("src/test/resources", TEST_URI02).getAbsolutePath()));
        assertTrue(messages.contains("view: secondlevelmain.v"));
    }

    @Test
    public void logAtSevereScriptErrors() throws Exception {
        LOG.setLevel(Level.INFO);

        try {
            handler.handle(TEST_URI06, request, request, response);
        } catch (Exception x) {
            //
            // of course...
            //
        }

        List<String> messages = H.getMessages();
        String msg = messages.get(0);
        assertEquals(Level.SEVERE, H.getRecords().get(0).getLevel());
        assertTrue(msg.contains(TEST_URI06));
        assertTrue(msg.contains("Encountered \"error\" at line 1, column 20"));
    }
}
