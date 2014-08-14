/*
 * Copyright (C) 2014 Stefano Fornari.
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Stefano Fornari.  This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * STEFANO FORNARI MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. STEFANO FORNARI SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package ste.web.http;

import java.io.Writer;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.xtest.net.TestSocket;
import static ste.xtest.reflect.PrivateAccess.getInstanceValue;

/**
 *
 * @author ste
 */
public class BugFreeBasicHttpConnection {
    
    @Test
    public void constructors() {
        BasicHttpConnection c = new BasicHttpConnection();
    }
    
    @Test
    public void getWriterCreatesANewWriter() throws Exception {
        BasicHttpConnection c = new BasicHttpConnection();
        then(
            getInstanceValue(c, "writer")
        ).isNull();
        c.bind(new TestSocket());
        
        Writer w = c.getWriter();
        then(w).isNotNull();
        then(getInstanceValue(c, "writer")).isEqualTo(w);
    }
    
    @Test
    public void createANewWriterOnlyOnce() throws Exception {
        BasicHttpConnection c = new BasicHttpConnection();
        c.bind(new TestSocket());
        
        Writer w1 = c.getWriter(), w2 = c.getWriter();
        then(w1).isNotNull().isEqualTo(w2);
        then(getInstanceValue(c, "writer")).isEqualTo(w2);
    }
}
