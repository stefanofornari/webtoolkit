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

/**
 *
 * @author ste
 */
public class BugFreeHttpSession {
    
    @Test
    public void newInstanceHasANewId() {
        HttpSession s1 = new HttpSession();
        HttpSession s2 = new HttpSession();
        HttpSession s3 = new HttpSession();
        
        then(s1.getId()).isNotEqualTo(s2.getId()).isNotEqualTo(s3.getId());
        then(s2.getId()).isNotEqualTo(s3.getId());
    }
    
    @Test
    public void idCanNotBeBlank() {
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
    public void setGetRemoveAttributeOK() {
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
    public void attributeNameCanNotBeNullInSetGetRemoveAttribute() {
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
    public void headerWithJSESSIONID() {
        HttpSession s = new HttpSession();
        
        SessionHeader h = s.getHeader();
        HttpCookie cookie = HttpCookie.parse(h.toString()).get(0);
        
        then(cookie.getName()).isEqualTo("JSESSIONID");
        then(cookie.getValue()).isEqualTo(s.getId());
    }
    
    @Test
    public void noAccessAfterExpiration() {
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
}
