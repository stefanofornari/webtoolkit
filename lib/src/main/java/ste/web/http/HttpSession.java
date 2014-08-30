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
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.apache.http.protocol.HttpContext;

/**
 *
 */
public class HttpSession
implements HttpContext {
    
    private Map<String, Object> data;
    private String id;
    private boolean expired;

    public HttpSession() {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.expired = false;
        this.data = new HashMap<>();
    }
    
    /**
     * Returns the HTTP header to keep this session object
     * 
     * @return the HTTP header to keep this session object
     */
    public SessionHeader getHeader() {
        checkExpired();
        return new SessionHeader(id);
    }
    
    /**
     * Sets this session object's id
     * 
     * @param id - the session id - NOT BLANK
     * 
     * @throws IllegalArgumentException if id is blank
     */
    public void setId(final String id) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("id can not be blank");
        }
        checkExpired();
        this.id = id;
    }
    
    /**
     * Returns this session object's id
     * 
     * @return 
     */
    public String getId() {
        return id;
    }
    
    public Object getAttribute(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        checkExpired();
        return data.get(name);
    }
    
    @Override
    public void setAttribute(final String name, final Object value) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        checkExpired();
        data.put(name, value);
    }
    
    @Override
    public Object removeAttribute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        checkExpired();
        return data.remove(name);
    }
    
    public void expire() {
        expired = true;
        System.out.printf("hash: %d, id: %s expired\n", hashCode(), getId());
    }
    
    // --------------------------------------------------------- private methods
    
    private void checkExpired() throws IllegalStateException {
        if (expired) {
            throw new IllegalStateException("session " + id + " is expired");
        }
    }
}
