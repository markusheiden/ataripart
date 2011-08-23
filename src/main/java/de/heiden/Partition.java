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

  private final long offset;

  private final long start;

  private final long length;

  /**
   * Constructor.
   *
   * @param number Number of partition
   * @param flags Partition flags, e.g. bit 0 = active, bit 7 = bootable
   * @param type Type of partition, e.g. "BGM", "XGM" etc.
   * @param offset Absolute offset of containing root sector
   * @param start Start of partition (in bytes) relative to root sector
   * @param length Length of partition (in bytes)
   */
  public Partition(int number, int flags, String type, long offset, long start, long length)
  {
    this.number = number;
    this.flags = flags;
    this.type = type;
    this.offset = offset;
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

  public long getAbsoluteStart()
  {
    return offset + start;
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
    StringBuilder result = new StringBuilder(256);
    result.append("Partition ").append(partitionName).append("\n");
    result.append("Type  : ").append(type);
    result.append(isActive() ? " (active)" : " (inactive)");
    if (isBoot())
    {
      result.append(" (boot)");
    }
    result.append("\n");
    result.append("Start : ").append(getAbsoluteStart()).append(" (").append(getAbsoluteStart()).append(")\n");
    result.append("Length: ").append(length).append("\n");

    return result.toString();
  }

  //
  //
  //

  /**
   * Parse single partition info.
   *
   * @param number Number of partition
   * @param offset Absolute offset of containing root sector
   * @param sector Part of hard disk image
   * @param index Index of partition info in sector
   */
  public static Partition parse(int number, long offset, byte[] sector, int index)
  {
    int flags = sector[index] & 0xff;
    String type = new String(sector, index + 1, 3);
    long start = getInt32(sector, index + 4) * 512;
    long length = getInt32(sector, index + 8) * 512;
    return new Partition(number, flags, type, offset, start, length);
  }
}
