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

import java.util.HashMap;
import javax.servlet.http.HttpSession;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author ste
 */
public class HttpSessionContext 
    extends HashMap<String, Object>
    implements HttpContext {
    
    private HttpSession session;

    @Override
    public Object getAttribute(final String id) {
        return get(id);
    }

    @Override
    public void setAttribute(final String id, final Object obj) {
        put(id, obj);
    }

    @Override
    public Object removeAttribute(final String id) {
        return remove(id);
    }
    
    public HttpSession getSession() {
        return session;
    }
    
    public void setSession(final HttpSession session) {
        this.session = session;
    }
    
}
