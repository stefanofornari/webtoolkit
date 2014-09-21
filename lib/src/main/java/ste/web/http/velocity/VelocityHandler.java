/*
 * BeanShell Web
 * Copyright (C) 2012 Stefano Fornari
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
package ste.web.http.velocity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import static ste.web.beanshell.Constants.*;
import ste.web.http.HttpSessionContext;
import ste.web.http.QueryString;

/**
 *
 * @author ste
 */
public class VelocityHandler implements HttpRequestHandler  {

    // --------------------------------------------------------------- Constants
    
    // ------------------------------------------------------------ Private data
    private VelocityEngine engine;
    private String viewsFolder;

    // ------------------------------------------------------------ Constructors
    
    public VelocityHandler(final String webroot) {
        if (webroot == null) {
            throw new IllegalArgumentException("webroot can not be null");
        }
        
        engine = new VelocityEngine();
        
        engine.setProperty("file.resource.loader.path", webroot);
        engine.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        engine.setProperty( "resource.loader", "file" );

        engine.init();
        
        setViewsFolder(null);
    }
    
    public VelocityHandler(final String webroot, final String viewsFolder) {
        this(webroot);
        setViewsFolder(viewsFolder);
    }

    // ---------------------------------------------------------- Public methods
    /**
     * @return the viewsFolder
     *
     */
    public String getViewsFolder() {
        return viewsFolder;
    }

    /**
     * Sets the folder where views are located. If the the given value is null,
     * it defaults to DEFAULT_VIEWS_PREFIX.
     *
     * @param viewsFolder the viewsFolder to set - NULL
     */
    public void setViewsFolder(final String viewsFolder) {
        if (viewsFolder == null) {
            this.viewsFolder = DEFAULT_VIEWS_PREFIX;
        } else {
            //
            // let's fix a common mistake :)
            //
            this.viewsFolder = (!viewsFolder.startsWith("/"))
                             ? ('/' + viewsFolder)
                             : viewsFolder
                             ;
        }
    }

    @Override
    public void handle(HttpRequest  request,
                       HttpResponse response,
                       HttpContext  context) throws HttpException, IOException {
        String view = (String)context.getAttribute(ATTR_VIEW);
        if (view == null) {
            return;
        }
        
        view = getViewPath(request.getRequestLine().getUri(), view);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer out = new OutputStreamWriter(baos);
        try {
            Template t = engine.getTemplate(view);
            t.merge(buildContext(request, (HttpSessionContext)context), out); out.flush();
        } catch (ResourceNotFoundException e) {
            response.setStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "View " + view + " not found.");
            return;
        } catch (ParseErrorException e) {
            throw new HttpException("Parse error evaluating " + view + ": " + e, e);
        } catch (MethodInvocationException e) {
            throw new HttpException("Method invocation error evaluating " + view + ": " + e, e);
        }
        
        BasicHttpEntity body = (BasicHttpEntity)response.getEntity();
        body.setContentLength(baos.size());
        body.setContent(new ByteArrayInputStream(baos.toByteArray()));
        if ((body.getContentType() == null) || StringUtils.isBlank(body.getContentType().getValue())) {
            body.setContentType(ContentType.TEXT_HTML.getMimeType());
        }
    }

    /**
     * @return the engine
     */
    public VelocityEngine getEngine() {
        return engine;
    }

    // --------------------------------------------------------- Private methods

    /**
     * Creates a velocity context filling it with all request parameters and
     * attributes (the former overwrite the latter).
     *
     * @param request the request to create the context upon
     *
     * @return the newly created context
     */
    private VelocityContext buildContext(HttpRequest request, HttpSessionContext httpContext) {
        VelocityContext context = new VelocityContext();

        for (String name: httpContext.keySet()) {
            context.put(name, httpContext.getAttribute(name));
        }

        try {
            URI uri = new URI(request.getRequestLine().getUri());
            QueryString qs = QueryString.parse(uri.getQuery());
            for (String name: qs.getNames()) {
                context.put(name, qs.get(name));
            }
        } catch (URISyntaxException x) {
            //
            // if the URL is marformed, there is nothing to do here...
            //
        }

        return context;
    }

    private String getViewPath(final String uri, final String view) {
        File uriFile = new File(uri);
        File viewFile = new File(
                            uriFile.getParent(),
                            new File(
                                viewsFolder,
                                view
                            ).getPath()
                        );

        return viewFile.getPath();
    }
}
