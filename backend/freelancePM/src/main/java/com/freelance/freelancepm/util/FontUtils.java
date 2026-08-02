package com.freelance.freelancepm.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FontUtils {

    /**
     * Reads a TrueType font input stream and dynamically patches the OS/2 table
     * fsType
     * flags in memory to 0x0000. This bypasses restrictive embedding flags without
     * editing the font file on disk.
     */
    public static InputStream patchFont(InputStream is) throws IOException {
        if (is == null)
            return null;

        byte[] data = is.readAllBytes();

        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int numTables = buffer.getShort(4);

            for (int i = 0; i < numTables; i++) {
                int offset = 12 + (i * 16);
                byte[] tagBytes = new byte[4];
                buffer.position(offset);
                buffer.get(tagBytes);
                String tag = new String(tagBytes, StandardCharsets.US_ASCII);

                if ("OS/2".equals(tag)) {
                    int tableOffset = buffer.getInt(offset + 8);
                    int fsTypeOffset = tableOffset + 8;
                    // Clear the fsType restrictive flags
                    data[fsTypeOffset] = 0;
                    data[fsTypeOffset + 1] = 0;
                    break;
                }
            }
        } catch (Exception e) {
            // Proceed with unpatched data if parsing structurally fails
        }

        return new ByteArrayInputStream(data);
    }
}
