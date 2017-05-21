package de.heiden.ataripart.image;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Static helpers for string conversions.
 */
public class StringUtils {
    /**
     * Read string from buffer. Uses {@link StandardCharsets#US_ASCII}.
     */
    public static String getString(ByteBuffer buffer, int offset, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = buffer.get(offset + i);
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * Write string tobuffer. Uses {@link StandardCharsets#US_ASCII}.
     */
    public static void setString(ByteBuffer buffer, int offset, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < bytes.length; i++) {
            buffer.put(offset + i, bytes[i]);
        }
    }

    /**
     * Convert byte to character.
     *
     * @param b Byte.
     */
    public static char character(int b) {
        return new String(new byte[] {(byte) b}, StandardCharsets.US_ASCII).charAt(0);
    }
}
