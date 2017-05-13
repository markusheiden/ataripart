package de.heiden;

/**
 * Static helpers for conversions.
 */
public class IntUtils {
    /**
     * Calculate checksum of sector.
     * Simply adds up sector data as 16 bit little endian words.
     *
     * @param bytes Sector data
     * @param index Index to start at
     * @param length Number of bytes(!) to add
     */
    public static int checksumInt16LittleEndian(byte[] bytes, int index, int length) {
        int checksum = 0;
        for (int i = 0; i < length; i += 2) {
            checksum += getInt16LittleEndian(bytes, index + i);
        }

        return checksum & 0xFFFF;
    }

    /**
     * Read a 8 bit integer.
     *
     * @param bytes Bytes to read from
     * @param index Index to start at
     */
    public static int getInt8(byte[] bytes, int index) {
        return toByte(bytes[index]);
    }

    /**
     * Read a 16 bit little endian integer.
     *
     * @param bytes Bytes to read from
     * @param index Index to start at
     */
    public static int getInt16LittleEndian(byte[] bytes, int index) {
        return (int) getIntLittleEndian(bytes, index, 2);
    }

    /**
     * Read a 16 bit big endian integer.
     *
     * @param bytes Bytes to read from
     * @param index Index to start at
     */
    public static int getInt16BigEndian(byte[] bytes, int index) {
        return (int) getIntBigEndian(bytes, index, 2);
    }

    /**
     * Read a 32 bit little endian integer.
     *
     * @param bytes Bytes to read from
     * @param index Index to start at
     */
    public static long getInt32LittleEndian(byte[] bytes, int index) {
        return getIntLittleEndian(bytes, index, 4);
    }

    /**
     * Read a 32 bit big endian integer.
     *
     * @param bytes Bytes to read from
     * @param index Index to start at
     */
    public static long getInt32BigEndian(byte[] bytes, int index) {
        return getIntBigEndian(bytes, index, 4);
    }

    /**
     * Read a big endian integer of the given length.
     *
     * @param bytes Bytes to read from
     * @param index Index to start at
     * @param length Number of bytes to read
     */
    private static long getIntBigEndian(byte[] bytes, int index, int length) {
        long result = 0;
        for (int i = length - 1; i >= 0; i--) {
            result = result << 8 | toByte(bytes[index + i]);
        }
        return result;
    }

    /**
     * Read a little endian integer of the given length.
     *
     * @param bytes Bytes to read from
     * @param index Index to start at
     * @param length Number of bytes to read
     */
    private static long getIntLittleEndian(byte[] bytes, int index, int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            result = result << 8 | toByte(bytes[index + i]);
        }
        return result;
    }

    /**
     * Convert byte to an unsigned representation.
     *
     * @param b Byte
     * @return Unsigned byte
     */
    public static int toByte(byte b) {
        return b & 0xFF;
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
     * @param bytes Bytes do dump
     * @param index Index to start at
     * @param length Number of bytes to dump
     */
    public static String hexDump(byte[] bytes, int index, int length) {
        StringBuilder result = new StringBuilder(length * 4);
        StringBuilder text = new StringBuilder(20);
        for (int i = 0; i < length && index + i < bytes.length; ) {
            text.setLength(0);

            result.append(hexPlain(i, 4));
            result.append(" ");
            for (int j = 0; j < 16 && index + i < bytes.length; i++, j++) {
                int b = toByte(bytes[index + i]);
                result.append(hexPlain(b, 2));
                result.append(" ");

                char c = (char) b;
                text.append(Character.isLetterOrDigit(c) ? c : '.');
            }
            result.append(text);
            result.append("\n");
        }

        return result.toString();
    }
}
