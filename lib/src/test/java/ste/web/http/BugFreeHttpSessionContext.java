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

import static org.assertj.core.api.BDDAssertions.then;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeHttpSessionContext {
    
    @Test
    public void setAndGetSession() {
        HttpSessionContext ctx = new HttpSessionContext();
        HttpSession s1 = new HttpSession(), s2 = new HttpSession();
        
        then(ctx.getSession()).isNull();
        
        ctx.setSession(s1); then(ctx.getSession()).isSameAs(s1);
        ctx.setSession(s2); then(ctx.getSession()).isSameAs(s2);
        ctx.setSession(null); then(ctx.getSession()).isNull();
    }
    
    @Test
    public void attributeOperations() {
        HttpSessionContext ctx = new HttpSessionContext();
        
        ctx.setAttribute("test1", "value1");
        ctx.setAttribute("test2", "value2");
        
        then(ctx).hasSize(2);
        then(ctx.getAttribute("test1")).isEqualTo("value1");
        then(ctx.getAttribute("test2")).isEqualTo("value2");
        then(ctx).contains(MapEntry.entry("test1", "value1"));
        then(ctx).contains(MapEntry.entry("test2", "value2"));
        
        ctx.removeAttribute("test2");
        then(ctx).hasSize(1);
        then(ctx).contains(MapEntry.entry("test1", "value1"));
        
        ctx.removeAttribute("test1");
        then(ctx).isEmpty();
    }
}
