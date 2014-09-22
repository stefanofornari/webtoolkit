/*
 * Copyright (C) 2013 Stefano Fornari.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author ste
 *
 * TODO: complete BFD of loadMimeTable
 */
public class MimeUtils {

    public static final String MIME_UNKNOWN = "content/unknown";
    public static final String MIME_DIRECTORY = "inode/directory";

    public static final String DEFAULT_MIME_MAP = "/content-types.properties";

    private static final MimeUtils instance;

    //
    // TODO: releoad it if the content of the file changes
    //
    private Map<String, String> mimeMap;

    static {
        instance = new MimeUtils();
    }

    protected MimeUtils() {
        mimeMap = null;
    }

    public static MimeUtils getInstance() {
        return instance;
    }
    
    public boolean isImage(String f) {
        return isImage((f != null) ? new File(f) : null);
    }

    public boolean isImage(File f) {
        String type = getMimeType(f);

        return ((type != null) && (type.startsWith("image/")));
    }

    /**
     * Loads the mime map from the given file name. Note that the mime types
     * defined in the file table will replace or be added to the default table.
     *
     * @param mimeTable the file name of the map to loadMimeTable - NOT NULL
     *
     * @throws IOException in case of IO errors
     * @throws IllegalArgumentException if mimeTable is null
     */
    public void loadMimeTable(String mimeTable) throws IOException {
        if (mimeTable == null) {
            throw new IllegalArgumentException("mimeTable cannot be null!");
        }

        if (mimeMap == null) {
            loadDefaultMap();
        }

        InputStream is = null;
        try {
            is = new FileInputStream(mimeTable);

            if (is == null) {
                throw new FileNotFoundException("File " + mimeTable + " not found");
            }
            mimeMap.putAll(load(is));
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Returns the mime type associated to the given file looking at the
     * extension. If the extension is unknown or file is null, MIME_UNKNOWN is
     * returned.
     *
     * @param f the file of which the mime type shall be returned - NULL ALLOWED
     *
     * @return the detected mime type
     */
    public String getMimeType(File f) {
        if (f == null) {
            return MIME_UNKNOWN;
        }

        if (f.isDirectory()) {
            return MIME_DIRECTORY;
        }

        if (mimeMap == null) {
            loadDefaultMap();
        }

        String mimeTypeFull =
            mimeMap.get(FilenameUtils.getExtension(f.getName()).toLowerCase());

        if (mimeTypeFull == null) {
            return MIME_UNKNOWN;
        }

        String[] mimeTypeParts = mimeTypeFull.split(";");

        return mimeTypeParts[0].trim();
    }
    
    /**
     * @see getMimeType(File f)
     *
     * @param f a file name
     * 
     * @return the mime type associate to the given file name (by extension)
     */
    public String getMimeType(String f) {
        return getMimeType((f != null) ? new File(f) : null);
    }

    // ------------------------------------------------------- Protected methods

    protected Map<String, String> load(InputStream is) throws IOException {
        Properties p = new Properties();

        p.load(is);

        return convertToMap(p);
    }


    // --------------------------------------------------------- Private methods

    private void loadDefaultMap() {
        try {
            InputStream is = MimeUtils.class.getResourceAsStream(DEFAULT_MIME_MAP);
            if (is == null) {
                throw new FileNotFoundException("Resource " + DEFAULT_MIME_MAP + " not found in default classpath");
            }
            mimeMap = load(is);
        } catch (IOException x) {
            x.printStackTrace();
        } finally {
            if (mimeMap == null) {
                mimeMap = new HashMap<String, String>();
            }
        }
    }

    private Map<String, String> convertToMap(Properties p) {
        Map<String, String> map = new HashMap<String, String>();
        for(Enumeration e = p.keys(); e.hasMoreElements();) {
            String extensions = ((String)e.nextElement());
            for (String extension: extensions.split(",")) {
                String mimeTypeFull = p.getProperty(extensions);
                map.put(extension, mimeTypeFull);
            }
        }
        return map;
    }

}
