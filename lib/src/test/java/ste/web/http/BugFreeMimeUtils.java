/*
 * Copyright (C) 2012 Stefano Fornari.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.junit.After;
import org.junit.Test;

/**
 * MimeUtils registers mime types from two sources:
 *
 * - content-types.properties in the classpath (usually in libone jar
 * - from a properties file provided in loadMimeTable
 *
 * The latter adds/overwrites the existing map entries
 *
 * @author ste
 */
public class BugFreeMimeUtils {
    
    @After
    public void tearDown() throws Exception {
        //
        // reinitialize MimeUtils to the default table
        //
        Method m = MimeUtils.class.getDeclaredMethod("loadDefaultMap");
        m.setAccessible(true);
        m.invoke(MimeUtils.getInstance());
    }
    
    @Test
    public void isImage() {
        
        //
        // With strings
        //
        String[] TEST_FILES = new String[] {
            "image1.jpg", "image2.png", "text.txt", "document.doc", null, "", " "
        };
        boolean[] RESULTS = new boolean[] {
            true, true, false, false, false, false, false
        };
        
        MimeUtils m = MimeUtils.getInstance();
        int i=0;
        for (String file: TEST_FILES) {
            assertThat(m.isImage(file)).isEqualTo(RESULTS[i++]);
        }
        
        //
        // With files
        //
        i=0;
        for (String file: TEST_FILES) {
            assertThat(m.isImage((file != null) ? new File(file) : null)).isEqualTo(RESULTS[i++]);
        }
    }
    
    @Test
    public void singletone() throws Exception {
        MimeUtils m = MimeUtils.getInstance();
        assertThat(m).isNotNull();
        assertThat(m).isSameAs(MimeUtils.getInstance());
    }

    @Test
    public void defaultMimeTable() throws Exception {
        assertThat(MimeUtils.getInstance().getMimeType(new File("test.jpg"))).isEqualTo("image/jpeg");
    }

    @Test
    public void notExistingDefaultMimeTable() throws Exception {
        //
        // Lets' force the table to be reloaded
        //

        assertThat(new MimeUtils2().getMimeType(new File("test.jpg"))).isEqualTo(MimeUtils.MIME_UNKNOWN);
    }


    @Test
    public void loadMimeTable() throws IOException {
        MimeUtils m = MimeUtils.getInstance();

        try {
            m.loadMimeTable(null);
            fail("Check for null parameters!");
        } catch (IllegalArgumentException x) {
            //
            // OK
            //
        }

        m.loadMimeTable("src/test/properties/content-types.properties");
        assertThat(m.getMimeType(new File("test.jpg"))).isEqualTo("image/jpeg"); // existing mime type
        assertThat(m.getMimeType(new File("test.new"))).isEqualTo("mime/new"); // a new one
        assertThat(m.getMimeType(new File("test.odt"))).isEqualTo("text/odt"); // replaced

        try {
            m.loadMimeTable("src/test/serverone-1/etc/notexist.properties");
            fail("if the file is not found, throw FileNotFoundException!");
        } catch (FileNotFoundException x) {
            assertThat(x.getMessage()).contains("notexist.properties");
        } catch (Exception e) {
            fail("if the file is not found, throw FileNotFoundException!");
        }
    }

    @Test
    public void getMimeType() throws IOException {
        MimeUtils m = MimeUtils.getInstance();
        m.loadMimeTable("src/test/properties/content-types.properties");

        //
        // We try just some, including some that are usually not in the JDK
        // distribuition (e.g. MS Office files)
        //
        assertThat(m.getMimeType(new File("test.txt"))).isEqualTo("text/plain");
        assertThat(m.getMimeType(new File("test.jpg"))).isEqualTo("image/jpeg");
        assertThat(m.getMimeType(new File("test.jpeg"))).isEqualTo("image/jpeg");
        assertThat(m.getMimeType(new File("test.doc"))).isEqualTo("application/msword");
        assertThat(m.getMimeType(new File("test.xls"))).isEqualTo("application/vnd.ms-excel");
        assertThat(m.getMimeType(new File("test.unk"))).isEqualTo(MimeUtils.MIME_UNKNOWN);
        assertThat(m.getMimeType((File)null)).isEqualTo(MimeUtils.MIME_UNKNOWN);
        
        //
        // string parameter version
        //
        assertThat(m.getMimeType("test.txt")).isEqualTo("text/plain");
        assertThat(m.getMimeType("test.jpg")).isEqualTo("image/jpeg");
        assertThat(m.getMimeType("test.jpeg")).isEqualTo("image/jpeg");
        assertThat(m.getMimeType((String)null)).isEqualTo(MimeUtils.MIME_UNKNOWN);
    }

    //
    // Given we return correct mime types, let's make sure wwe do it in a case
    // insensitive manner.
    //
    @Test
    public void getCaseInsensitiveMimeType() throws IOException {
        MimeUtils m = MimeUtils.getInstance();
        m.loadMimeTable("src/test/properties/content-types.properties");

        final String[] files = {
            "file.txt", "FILE.TXT", "file.TXT", "File.txt", "file.tXt", "file.Txt"
        };

        for (String f: files) {
            System.out.println("Checking " + f);
            assertThat(m.getMimeType(new File(f))).isEqualTo("text/plain");
        }

    }

    //
    // The mime type of a folder is not well defined. IANA defines text/directory
    // but it is deprecated (http://www.iana.org/assignments/media-types/media-types.xhtml,
    // http://tools.ietf.org/html/rfc2425; Ubuntu uses inode/directory. None of
    // the two are particularly appropriated, but we take inode/directory because
    // a directory is definitely not a text
    //
    @Test
    public void getDirectoryMimeType() {
        assertThat(
            MimeUtils.getInstance().getMimeType(new File("."))
        );
    }
}