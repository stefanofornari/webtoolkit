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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;

/**
 * This is an extension of DefaultBHttpServerConnection which provides a
 * <code>getWriter()</code> around the socket output stream.
 * 
 * @author ste
 */
public class BasicHttpConnection extends org.apache.http.impl.DefaultBHttpServerConnection {
    
    private Writer writer;

    public BasicHttpConnection() {
        super(ConnectionConfig.DEFAULT.getBufferSize());
        writer = null;
    }

    BasicHttpConnection(int bufferSize, int fragmentSizeHint, CharsetDecoder createDecoder, CharsetEncoder createEncoder, MessageConstraints messageConstraints, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, HttpMessageParserFactory<HttpRequest> requestParserFactory, HttpMessageWriterFactory<HttpResponse> responseWriterFactory) {
        super(
            bufferSize, fragmentSizeHint, createDecoder, createEncoder, 
            messageConstraints, incomingContentStrategy, outgoingContentStrategy, 
            requestParserFactory, responseWriterFactory
        );
        writer = null;
    }
    
    @Override
    public void shutdown() throws IOException {
        final Socket socket = getSocket();
        if (socket != null) {
            // force abortive close (RST)
            try {
                socket.setSoLinger(true, 1);
            } catch (final IOException ex) {
            } finally {
                socket.close();
            }
        }
    }
    
    public Writer getWriter() throws IOException {
        if (writer == null) {
            writer = new OutputStreamWriter(getSocketOutputStream(getSocket()));
        }
        return writer;
    }
    
}
