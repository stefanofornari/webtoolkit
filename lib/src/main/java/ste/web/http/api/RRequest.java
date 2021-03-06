/*
 * BeanShell Web
 * Copyright (C) 2015 Stefano Fornari
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
package ste.web.http.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicRequestLine;
import org.json.JSONObject;

/**
 *
 */
public class RRequest {
    
    private URI uri;
    private final String handler;
    private final String application;
    private final String action;
    private final String[] resource;
    private final JSONObject body;
    
    /**
     * 
     * @param request - NOT NULL
     * @param body - MAY BE NULL
     * 
     * @throws URISyntaxException if the given request has an invalid URI
     * @throws IllegalArgumentException if request is null
     * 
     */
    public RRequest(final RequestLine request, final JSONObject body) throws URISyntaxException {
        this((request == null) ? null: request.getUri(), body);
    }
    
    /**
     * Same as RRequest(request, null)
     * 
     * @param request - NOT NULL
     * 
     * @throws URISyntaxException if the given request has an invalid URI
     * @throws IllegalArgumentException if request is null
     * 
     */
    public RRequest(final RequestLine request) throws URISyntaxException {
        this(request, null);
    }
    
    /**
     * 
     * @param request - NOT NULL
     * @param body - MAY BE NULL
     * 
     * @throws URISyntaxException if the given request has an invalid URI
     * @throws IllegalArgumentException if request is null
     * 
     */
    public RRequest(final String request, final JSONObject body) throws URISyntaxException {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null");
        }
        
        //uri = new URI(request.getUri());
        uri = new URI(request);
        String[] elements = StringUtils.split(uri.getPath(),'/');
        
        if (elements.length < 3) {
            throw new URISyntaxException(
                uri.toString(),
                "invalid rest request; a valid rest url shall follow the syntax /<apicontext>/<application>/<action>/<resource>"
            );
        }
        application = elements[0];
             action = elements[1];
            handler = elements[2];
        
        resource = Arrays.copyOfRange(elements, 2, elements.length);
        
        this.body = body;
    }
    
    /**
     * Same as RRequest(request, null)
     * 
     * @param request - NOT NULL
     * 
     * @throws URISyntaxException if the given request has an invalid URI
     * @throws IllegalArgumentException if request is null
     * 
     */
    public RRequest(final String request) throws URISyntaxException {
        this(request, null);
    }
    
    public String getPath() {
        return uri.getPath();
        
    }
    
    
    public String getApplication() {
        return application;
    }
    
    public String getAction() {
        return action;
    }
    
    public String[] getResource() {
        return resource;
    }
    
    public String getHandler() {
        return handler;
    }
    
    public JSONObject getBody() {
        return body;
    }
}
