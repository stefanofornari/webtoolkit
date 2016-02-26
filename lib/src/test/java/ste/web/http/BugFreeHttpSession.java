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

import java.net.HttpCookie;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import static ste.xtest.Constants.BLANKS;

/**
 *
 * @author ste
 */
public class BugFreeHttpSession {
    
    @Test
    public void new_instance_has_a_new_id() {
        HttpSession s1 = new HttpSession();
        HttpSession s2 = new HttpSession();
        HttpSession s3 = new HttpSession();
        
        then(s1.getId()).isNotEqualTo(s2.getId()).isNotEqualTo(s3.getId());
        then(s2.getId()).isNotEqualTo(s3.getId());
    }
    
    @Test
    public void id_can_not_be_blank() {
        HttpSession s = new HttpSession();
        
        final String[] BLANKS = new String[] { null, "", " ", "\t" };
        for (String blank: BLANKS) {
            try {
               s.setId(blank);
               fail("missing illegal parameter check");
            } catch (IllegalArgumentException x) {
                then(x.getMessage()).contains("id can not be blank");
            }
        }
    }
    
    @Test
    public void set_get_remove_attribute_ok() {
        HttpSession s = new HttpSession();
        s.setAttribute("string", "hello world");
        s.setAttribute("integer", new Integer(111));
        s.setAttribute("null", null);
        
        then(s.getAttribute("string")).isEqualTo("hello world");
        then(s.getAttribute("integer")).isEqualTo(111);
        then(s.getAttribute("null")).isNull();
        
        s.setAttribute("string", "new value");
        then(s.getAttribute("string")).isEqualTo("new value");
        
        s.removeAttribute("string");
        then(s.getAttribute("string")).isNull();
    }
    
    @Test
    public void attribute_name_can_not_be_null_in_XXXRemoveAttribute() {
        HttpSession s = new HttpSession();
        
        try {
           s.setAttribute(null, "hello world");
           fail("missing illegal parameter check");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("name can not be null");
        }
        
        try {
           s.getAttribute(null);
           fail("missing illegal parameter check");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("name can not be null");
        }
        
        try {
           s.removeAttribute(null);
           fail("missing illegal parameter check");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("name can not be null");
        }
    }
    
    @Test
    public void header_with_JSESSIONID() {
        HttpSession s = new HttpSession();
        
        SessionHeader h = s.getHeader();
        HttpCookie cookie = HttpCookie.parse(h.toString()).get(0);
        
        then(cookie.getName()).isEqualTo("JSESSIONID");
        then(cookie.getValue()).isEqualTo(s.getId());
    }
    
    @Test
    public void no_access_after_expiration() {
        HttpSession s = new HttpSession();
        s.expire();
        
        //
        // getId() is ok, all other methods shall be blocked
        //
        
        then(s.getId()).isNotNull();
        
        try {
            s.setAttribute("test", null);
            fail("session should not be accessible after expiration!");
        } catch (IllegalStateException x) {
            then(x.getMessage()).contains(s.getId()).contains("expired");
        }
        
        try {
            s.getAttribute("test");
            fail("session should not be accessible after expiration!");
        } catch (IllegalStateException x) {
            then(x.getMessage()).contains(s.getId()).contains("expired");
        }
        
        try {
            s.removeAttribute("test");
            fail("session should not be accessible after expiration!");
        } catch (IllegalStateException x) {
            then(x.getMessage()).contains(s.getId()).contains("expired");
        }
        
        try {
            s.setId("123456");
            fail("session should not be accessible after expiration!");
        } catch (IllegalStateException x) {
            then(x.getMessage()).contains(s.getId()).contains("expired");
        }
        
        try {
            s.getHeader();
            fail("session should not be accessible after expiration!");
        } catch (IllegalStateException x) {
            then(x.getMessage()).contains(s.getId()).contains("expired");
        }
    }
    
    @Test
    public void set_id() {
        HttpSession s = new HttpSession();
        try {
            for (String BLANK: BLANKS) {
                s.setId(BLANK);
                fail("missing check for not blankable parameters");
            }
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("id").contains("can not be blank");
        }
        
        final String ID = "123456";
        s.setId(ID);
        then(s.getId()).isEqualTo(ID);
    }
}
