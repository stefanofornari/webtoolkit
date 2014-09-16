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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 *
 * @author ste
 */
public class SequenceHttpRequestHandler implements HttpRequestHandler {
    
    private List<HttpRequestHandler> handlers;
    
    /**
     * An handler that runs multiple handlers in sequence
     * 
     * @param handlers the handlers to add to this sequence - NOT NULL (ANY)
     * 
     * @throws IllegalArgumentException in case any of the given handlers is null
     */
    public SequenceHttpRequestHandler(HttpRequestHandler... handlers) {     
        if (handlers == null) {
            throw new IllegalArgumentException("handlers can not be null");
        }
        
        this.handlers = new ArrayList<>(handlers.length);
        
        for(HttpRequestHandler h: handlers) {
            if (h == null) {
                throw new IllegalArgumentException("handlers can not contain null elements");
            } else {
                this.handlers.add(h);
            }
        }
    }
    
    public List<HttpRequestHandler> getHandlers() {
        return handlers;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) 
    throws HttpException, IOException {
        for(HttpRequestHandler h: handlers) {
            h.handle(request, response, context);
        }
    }
}
