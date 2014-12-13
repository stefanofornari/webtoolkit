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
package ste.web.beanshell.httpcore;

import bsh.Interpreter;
import bsh.NameSpace;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeBeanShallMultithreading {
    Interpreter i = new Interpreter();
    
    String script = "Thread.sleep(new Random().nextInt(500));\n" 
                  + "t = Thread.currentThread().name;";
    
    @Test
    public void runningMultipleThreadInDifferentContexts() throws Exception {
        final NameSpace NS1 = new NameSpace(NameSpace.JAVACODE, "thread1");
        final NameSpace NS2 = new NameSpace(NameSpace.JAVACODE, "thread2");
        
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    i.eval(script, NS1);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        });
        
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    i.eval(script, NS2);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        });
        
        t1.start(); t2.start();
        
        t1.join(); t2.join();
        
        then(NS1.getVariable("t")).isEqualTo(t1.getName());
        
        then(NS2.getVariable("t")).isEqualTo(t2.getName());
    }
}
