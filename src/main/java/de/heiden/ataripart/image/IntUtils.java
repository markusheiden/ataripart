package de.heiden.ataripart.image;

import java.nio.ByteBuffer;

/**
 * Static helpers for integer conversions.
 */
public class IntUtils {
    /**
     * Calculate checksum of sector.
     * Simply adds up sector data as 16 bit words.
     *
     * @param bytes Sector data
     * @param index Index to start at
     * @param length Number of bytes(!) to add
     */
    public static int checksumInt16(ByteBuffer bytes, int index, int length) {
        int checksum = 0;
        for (int i = 0; i < length; i += 2) {
            checksum += getInt16(bytes, index + i);
        }

        return checksum & 0xFFFF;
    }

    /**
     * Read a 8 bit "unsigned" integer.
     *
     * @param bytes Bytes to read from.
     * @param index Index to start at.
     */
    public static int getInt8(ByteBuffer bytes, int index) {
        return bytes.get(index) & 0xFF;
    }

    /**
     * Read a 16 bit "unsigned" integer.
     *
     * @param bytes Bytes to read from.
     * @param index Index to start at.
     */
    public static int getInt16(ByteBuffer bytes, int index) {
        return bytes.getShort(index) & 0xFFFF;
    }

    /**
     * Read a 32 bit "unsigned" integer.
     *
     * @param bytes Bytes to read from.
     * @param index Index to start at.
     */
    public static long getInt32(ByteBuffer bytes, int index) {
        return bytes.getInt(index) & 0xFFFFFFFF;
    }

    /**
     * Write a 8 bit integer.
     *
     * @param bytes Bytes to write to.
     * @param index Index to start at.
     * @param value "Unsigned" integer to write.
     */
    public static void setInt8(ByteBuffer bytes, int index, int value) {
        bytes.put(index, (byte) value);
    }

    /**
     * Write a 16 bit integer.
     *
     * @param bytes Bytes to write to.
     * @param index Index to start at.
     * @param value "Unsigned" integer to write.
     */
    public static void setInt16(ByteBuffer bytes, int index, int value) {
        bytes.putShort(index, (short) value);
    }

    /**
     * Write a 32 bit integer.
     *
     * @param bytes Bytes to write to.
     * @param index Index to start at.
     * @param value "Unsigned" integer to write.
     */
    public static void setInt32(ByteBuffer bytes, int index, long value) {
        bytes.putInt(index, (int) value);
    }

    /**
     * Output number as a hex string of the given length mit "$" prefix.
     * The resulting hex string may be longer than length chars, if the number is too big.
     *
     * @param number Number
     * @param length Length of output
     */
    public static String hex(long number, int length) {
        return "$" + hexPlain(number, length);
    }

    /**
     * Output number as a hex string of the given length.
     * The resulting hex string may be longer than length chars, if the number is too big.
     *
     * @param number Number
     * @param length Length of output
     */
    public static String hexPlain(long number, int length) {
        String hex = Long.toHexString(number).toUpperCase();
        StringBuilder result = new StringBuilder(length);
        for (int i = hex.length(); i < length; i++) {
            result.append("0");
        }
        result.append(hex);

        return result.toString();
    }

    /**
     * Produces hex dump like a debugger.
     * Displays 16 Bytes per line.
     * For debugging purposes only.
     *
     * @param bytes Bytes do dump. The buffer position has to be set to the start of the bytes to dump.
     * @param length Number of bytes to dump.
     */
    public static String hexDump(ByteBuffer bytes, int length) {
        StringBuilder result = new StringBuilder(length * 4);
        StringBuilder text = new StringBuilder(20);
        for (int i = 0; i < length && i < bytes.capacity(); ) {
            text.setLength(0);

            result.append(hexPlain(i, 4));
            result.append(" ");
            for (int j = 0; j < 16 && i < bytes.capacity(); i++, j++) {
                int b = getInt8(bytes, i);
                result.append(hexPlain(b, 2));
                result.append(" ");

                char c = StringUtils.character(b);
                text.append(Character.isLetterOrDigit(c) ? c : '.');
            }
            result.append(text);
            result.append("\n");
        }

        return result.toString();
    }
}
