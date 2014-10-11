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
package ste.web.http.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 * TODO: we are including code from the apache project, therefore we do not have
 * here all 
 */
public class BugFreeBasicStatusLine {
    
    @Test
    public void constructor() {
        final StatusLine statusline = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        then(statusline.getProtocolVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        then(statusline.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        then(statusline.getReasonPhrase()).isEqualTo("OK");
    }

    @Test
    public void constructorInvalidInput() {
        try {
            new BasicStatusLine(null, HttpStatus.SC_OK, "OK");
            fail("missing input sanity check");
        } catch (final IllegalArgumentException x) {
            then(x.getMessage()).contains("protocol").contains("can not be null");
        }
        try {
            new BasicStatusLine(HttpVersion.HTTP_1_1, -1, "OK");
            fail("missing input sanity check");
        } catch (final IllegalArgumentException x) {
            then(x.getMessage()).contains("status").contains("can not be negative");
        }
    }

    @Test
    public void toStringValue() throws Exception {
        StatusLine statusline = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        then(statusline.toString()).isEqualTo("HTTP/1.1 200 OK");
        statusline = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null);
        // toString uses default formatting, hence the trailing space
        then(statusline.toString()).isEqualTo("HTTP/1.1 200 ");
    }

    @Test
    public void cloning() throws Exception {
        final BasicStatusLine orig = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        final BasicStatusLine clone = (BasicStatusLine) orig.clone();
        then(clone.getReasonPhrase()).isEqualTo(orig.getReasonPhrase());
        then(clone.getStatusCode()).isEqualTo(orig.getStatusCode());
        then(clone.getProtocolVersion()).isEqualTo(orig.getProtocolVersion());
    }

    @Test
    public void serialization() throws Exception {
        final BasicStatusLine orig = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        final ByteArrayOutputStream outbuffer = new ByteArrayOutputStream();
        final ObjectOutputStream outstream = new ObjectOutputStream(outbuffer);
        outstream.writeObject(orig);
        outstream.close();
        final byte[] raw = outbuffer.toByteArray();
        final ByteArrayInputStream inbuffer = new ByteArrayInputStream(raw);
        final ObjectInputStream instream = new ObjectInputStream(inbuffer);
        final BasicStatusLine clone = (BasicStatusLine) instream.readObject();
        then(clone.getReasonPhrase()).isEqualTo(orig.getReasonPhrase());
        then(clone.getStatusCode()).isEqualTo(orig.getStatusCode());
        then(clone.getProtocolVersion()).isEqualTo(orig.getProtocolVersion());
    }
    
    @Test
    public void setStatusCodeOK() throws Exception {
        final BasicStatusLine TEST = 
            new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        
        TEST.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        then(TEST.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
        
        TEST.setStatusCode(HttpStatus.SC_FORBIDDEN);
        then(TEST.getStatusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN);
    }
    
    @Test
    public void setStatusCodeKO() throws Exception {
        final BasicStatusLine TEST = 
            new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        
        try {
            TEST.setStatusCode(-1);
            fail("missing input sanity check");
        } catch (final IllegalArgumentException x) {
            then(x.getMessage()).contains("status").contains("can not be negative");
        }
    }
}
