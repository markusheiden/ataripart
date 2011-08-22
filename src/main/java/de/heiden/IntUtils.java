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
      result = result << 8 | (bytes[index + i] & 0xFF);
    }
    return result;
  }
}
