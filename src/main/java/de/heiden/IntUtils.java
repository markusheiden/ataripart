package de.heiden;

/**
 * Static helpers for conversions.
 */
public class IntUtils
{
  public static int getInt8(byte[] bytes, int index)
  {
    return toByte(bytes[index]);
  }

  public static int getInt16BigEndian(byte[] bytes, int index)
  {
    return (int) getIntBigEndian(bytes, index, 2);
  }

  public static long getInt32LittleEndian(byte[] bytes, int index)
  {
    return getIntLittleEndian(bytes, index, 4);
  }

  private static long getIntBigEndian(byte[] bytes, int index, int length)
  {
    long result = 0;
    for (int i = length - 1; i >= 0; i--)
    {
      result = result << 8 | toByte(bytes[index + i]);
    }
    return result;
  }

  private static long getIntLittleEndian(byte[] bytes, int index, int length)
  {
    long result = 0;
    for (int i = 0; i < length; i++)
    {
      result = result << 8 | toByte(bytes[index + i]);
    }
    return result;
  }

  public static int toByte(byte b)
  {
    return b & 0xFF;
  }

  public static String hexPlain(int number, int length)
  {
    String hex = Integer.toHexString(number).toUpperCase();
    StringBuilder result = new StringBuilder(length);
    for (int i = hex.length(); i < length; i++)
    {
      result.append("0");
    }
    result.append(hex);

    return result.toString();
  }

  public static String hexDump(byte[] bytes, int index, int length)
  {
    StringBuilder result = new StringBuilder(length * 4);
    for (int i = 0; i < length && index + i < bytes.length;)
    {
      result.append(hexPlain(i, 4));
      result.append(" ");
      for (int j = 0; j < 16 && index + i < bytes.length; i++, j++)
      {
        result.append(hexPlain(toByte(bytes[index + i]), 2));
        result.append(" ");
      }
      result.append("\n");
    }

    return result.toString();
  }
}
