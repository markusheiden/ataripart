package de.heiden;

/**
 * Static helpers for conversions.
 */
public class IntUtils
{
  public static long getInt32(byte[] bytes, int index)
  {
    long result = 0;
    for (int i = 0; i < 4; i++)
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
