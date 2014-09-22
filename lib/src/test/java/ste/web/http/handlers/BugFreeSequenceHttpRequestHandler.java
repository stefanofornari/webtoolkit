/*
 * Web Toolkit Library
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
package ste.web.http.handlers;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeSequenceHttpRequestHandler {
    
    @Test
    public void constructors() {
        SequenceHttpRequestHandler H = new SequenceHttpRequestHandler();
        then(H.getHandlers()).isEmpty();
        
        try {
            new SequenceHttpRequestHandler(null);
            fail("missing not null parameters check");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("handlers").contains("not be null");
        }
        
        DummyHttpRequestHandler h1 = new DummyHttpRequestHandler();
        H = new SequenceHttpRequestHandler(h1);
        then(H.getHandlers()).hasSize(1).contains(h1);
        
        DummyHttpRequestHandler h2 = new DummyHttpRequestHandler();
        H = new SequenceHttpRequestHandler(h2, h1);
        then(H.getHandlers()).containsSequence(h2, h1);
        
        try {
            new SequenceHttpRequestHandler(null, h1);
            fail("missing not null parameters check");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("handlers").contains("contain null");
        }
    }
    
    @Test
    public void executeInSequence() throws Exception {
        DummyHttpRequestHandler h1 = new DummyHttpRequestHandler();
        DummyHttpRequestHandler h2 = new DummyHttpRequestHandler();
        DummyHttpRequestHandler h3 = new DummyHttpRequestHandler();
        
        SequenceHttpRequestHandler H = new SequenceHttpRequestHandler(h1, h2, h3);
        
        H.handle(null, null, null);  // we do not need real values
        
        then(h1.value).isEqualTo(1); 
        then(h2.value).isEqualTo(2);
        then(h3.value).isEqualTo(3);
        
        H = new SequenceHttpRequestHandler(h3, h2, h1);
        
        H.handle(null, null, null);  // we do not need real values
        
        then(h1.value).isEqualTo(6); 
        then(h2.value).isEqualTo(5);
        then(h3.value).isEqualTo(4);
    }
}
