package de.heiden;

import static de.heiden.IntUtils.getInt32;

/**
 * Partition info.
 */
public class Partition
{
  /**
   * Number of partition in its root sector.
   */
  private final int number;

  /**
   * Partition flags.
   * E.g. bit 0 = active, bit 7 = bootable.
   */
  private final int flags;

  /**
   * Type of partition.
   * E.g. "BGM", "XGM" etc.
   */
  private final String type;

  /**
   * Absolute offset in bytes of containing root sector.
   */
  private final long offset;

  /**
   * Start of partition in bytes relative to root sector.
   */
  private final long start;

  /**
   * Length of partition in bytes.
   */
  private final long length;

  /**
   * Constructor.
   *
   * @param number Number of partition in its root sector.
   * @param flags Partition flags, e.g. bit 0 = active, bit 7 = bootable
   * @param type Type of partition, e.g. "BGM", "XGM" etc.
   * @param offset Absolute offset in bytes of containing root sector
   * @param start Start of partition in bytes relative to its root sector
   * @param length Length of partition in bytes
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

  /**
   * Number of partition in its root sector.
   */
  public int getNumber()
  {
    return number;
  }

  /**
   * Is the partition marked active?.
   */
  public boolean isActive()
  {
    return (flags & 0x01) != 0;
  }

  /**
   * Is the partition marked bootable?.
   */
  public boolean isBoot()
  {
    return (flags & 0x80) != 0;
  }

  /**
   * Type of partition.
   * E.g. "BGM", "XGM" etc.
   */
  public String getType()
  {
    return type;
  }

  /**
   * Checks this partition for validity.
   * This check includes a scan for undefined flags and unknown partition types.
   * Currently only BGM and XGM partition are considered valid.
   */
  public boolean isValid()
  {
    return (flags & 0x7E) == 0 &&
      ("BGM".equals(type) || "XGM".equals(type));
//    return type.length() == 3 &&
//      Character.isLetterOrDigit(type.charAt(0)) &&
//      Character.isLetterOrDigit(type.charAt(1)) &&
//      Character.isLetterOrDigit(type.charAt(2));
  }

  /**
   * Is this a BGM partition?.
   */
  public boolean isBGM()
  {
    return "BGM".equals(type);
  }

  /**
   * Is this a XGM (pseudo) partition?.
   */
  public boolean isXGM()
  {
    return "XGM".equals(type);
  }

  /**
   * Start of partition in bytes (absolute).
   */
  public long getAbsoluteStart()
  {
    return offset + start;
  }

  /**
   * Start of partition in bytes relative to its root sector.
   */
  public long getStart()
  {
    return start;
  }

  /**
   * Length of partition in bytes.
   */
  public long getLength()
  {
    return length;
  }

  /**
   * End of partition in bytes (absolute).
   */
  public long getAbsoluteEnd()
  {
    return getAbsoluteStart() + getLength();
  }

  /**
   * End of partition in bytes relative to its root sector.
   */
  public long getEnd()
  {
    return getStart() + getLength();
  }

  @Override
  public String toString()
  {
    return toString(Integer.toString(getNumber()));
  }

  /**
   * toString() variant with customizable partition name.
   *
   * @param partitionName Name of partition, e.g. 0, 1, 2 or C, D, E etc.
   */
  public String toString(String partitionName)
  {
    StringBuilder result = new StringBuilder(256);
    result.append("Partition ").append(partitionName).append("\n");
    result.append("Type  : ").append(getType());
    result.append(isActive() ? " (active)" : " (inactive)");
    if (isBoot())
    {
      result.append(" (boot)");
    }
    result.append("\n");
    result.append("Start : ").append(getAbsoluteStart()).append(" (").append(getStart()).append(")\n");
    result.append("Length: ").append(getLength()).append("\n");
    result.append("End   : ").append(getAbsoluteEnd()).append(" (").append(getEnd()).append(")\n");

    return result.toString();
  }

  //
  // Parsing
  //

  /**
   * Parse single partition info.
   *
   * @param number Number of partition in its containing root sector
   * @param offset Absolute offset in  bytes of containing root sector
   * @param disk Hard disk image part
   * @param index Index of partition info in hard disk image part
   */
  public static Partition parse(int number, long offset, byte[] disk, int index)
  {
    int flags = disk[index] & 0xff;
    String type = new String(disk, index + 1, 3);
    long start = getInt32(disk, index + 4) * 512;
    long length = getInt32(disk, index + 8) * 512;
    return new Partition(number, flags, type, offset, start, length);
  }
}
