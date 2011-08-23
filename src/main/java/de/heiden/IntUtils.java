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
}
