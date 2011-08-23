package de.heiden;

import static de.heiden.IntUtils.getInt32;

/**
 * Partition info.
 */
public class Partition
{
  private final int number;

  private final int flags;

  private final String type;

  private final long start;

  private final long length;

  public Partition(int number, int flags, String type, long start, long length)
  {
    this.number = number;
    this.flags = flags;
    this.type = type;
    this.start = start;
    this.length = length;
  }

  public int getNumber()
  {
    return number;
  }

  public boolean isActive()
  {
    return (flags & 0x01) != 0;
  }

  public boolean isBoot()
  {
    return (flags & 0x80) != 0;
  }

  public String getType()
  {
    return type;
  }

  public boolean isValid()
  {
    return (flags & 0x7E) == 0 &&
      ("BGM".equals(type) || "XGM".equals(type));
//    return type.length() == 3 &&
//      Character.isLetterOrDigit(type.charAt(0)) &&
//      Character.isLetterOrDigit(type.charAt(1)) &&
//      Character.isLetterOrDigit(type.charAt(2));
  }

  public boolean isBGM()
  {
    return "BGM".equals(type);
  }

  public boolean isXGM()
  {
    return "XGM".equals(type);
  }

  public long getStart()
  {
    return start;
  }

  public long getLength()
  {
    return length;
  }

  public String toString()
  {
    return toString(Integer.toString(number));
  }

  public String toString(String partitionName)
  {
    StringBuilder result = new StringBuilder(64);
    result.append("Partition ").append(partitionName).append("\n");
    result.append("Type  : ").append(type);
    result.append(isActive() ? " (active)" : " (inactive)");
    if (isBoot())
    {
      result.append(" (boot)");
    }
    result.append("\n");
    result.append("Start : ").append(start).append("\n");
    result.append("Length: ").append(length).append("\n");

    return result.toString();
  }

  //
  //
  //

  public static Partition parse(int number, byte[] sector, int offset)
  {
    int flags = sector[offset] & 0xff;
    String type = new String(sector, offset + 1, 3);
    long start = getInt32(sector, offset + 4) * 512;
    long length = getInt32(sector, offset + 8) * 512;
    return new Partition(number, flags, type, start, length);
  }
}
