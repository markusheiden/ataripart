package de.heiden.ataripart.image;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Static helpers for string conversions.
 */
class StringUtils {
    /**
     * Read string from buffer. Uses {@link StandardCharsets#US_ASCII}.
     */
    public static String string(ByteBuffer buffer, int offset, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = buffer.get(offset + i);
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }
}
