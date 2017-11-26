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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeaderElement;

/**
 * 
 */
public class SessionHeader implements Header {
    
    public static final String DEFAULT_SESSION_HEADER = "HTTPSID";
    
    private final String name;
    private final String sessionId;
    
    /**
     * 
     * @param name the name of the session id cookie - NOT BLANK
     * @param sessionId the session id value - MAY BE BLANK
     * 
     * @throws IllegalArgumentException if name is BLANK
     */
    public SessionHeader(final String name, final String sessionId) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("session id name can not be null");
        }
        this.name = name;
        this.sessionId = sessionId;
    }
    
    public SessionHeader(final String sessionId) {
        this(DEFAULT_SESSION_HEADER, sessionId);
    }

    @Override
    public String getName() {
        return "Set-Cookie";
    }

    @Override
    public String getValue() {
        return String.format("%s=%s; Path=/; Secure; HttpOnly", name, sessionId);
    }

    @Override
    public HeaderElement[] getElements() throws ParseException {
        HeaderElement[] elements = new HeaderElement[1];
        
        elements[0] = new BasicHeaderElement(getName(), getValue());
        
        return elements;
                
    }
    
    @Override
    public String toString() {
        return getValue();
    }
}
