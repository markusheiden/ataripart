package de.heiden;

import java.util.ArrayList;
import java.util.List;

import static de.heiden.IntUtils.getInt32;

/**
 * Root sector info.
 */
public class RootSector
{
  /**
   * Absolute offset in bytes of root sector in disk.
   */
  private final long offset;

  /**
   * Size of disk in bytes.
   */
  private final long size;

  /**
   * Partitions defined by this root sector.
   */
  private final List<Partition> partitions = new ArrayList<>();

  /**
   * Constructor.
   *
   * @param offset Absolute offset in bytes of root sector in disk
   * @param size Size of disk in bytes
   */
  public RootSector(long offset, long size)
  {
    this.offset = offset;
    this.size = size;
  }

  /**
   * Absolute offset in bytes of root sector in disk.
   */
  public long getOffset()
  {
    return offset;
  }

  /**
   * Is this root sector an xgm root sector?.
   * Currently it is assumed that it is an xgm root sector, if the offset is greater than 0.
   */
  public boolean isXGM()
  {
    return offset > 0;
  }

  /**
   * Size of disk in bytes.
   */
  public long getSize()
  {
    return size;
  }

  /**
   * Absolute offset in bytes where the disk ends.
   */
  public long getEnd()
  {
    return offset + size;
  }

  /**
   * All partitions defined by this root sector.
   */
  public List<Partition> getPartitions()
  {
    return partitions;
  }

  /**
   * Add a partition.
   *
   * @param partition Partition
   */
  protected void add(Partition partition)
  {
    partitions.add(partition);
  }

  /**
   * Check, if this root sectors contains at least one valid active partition.
   * @see Partition#isValid()
   * @see Partition#isActive()
   */
  public boolean hasValidPartitions()
  {
    for (Partition partition : partitions)
    {
      if (partition.isValid() && partition.isActive())
      {
        return true;
      }
    }

    return false;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder(1024);
    if (isXGM())
    {
      result.append("XGM ");
    }
    result.append("Root sector\n");
    result.append("Start   : ").append(getOffset()).append("\n");
    result.append("First   : ").append(getOffset() + 512).append("\n");
    if (!isXGM())
    {
      result.append("Size    : ").append(getSize()).append("\n");
      result.append("End     : ").append(getEnd()).append("\n");
    }

    return result.toString();
  }

  //
  // Parsing
  //

  /**
   * Parse sector as root sector.
   *
   * @param xgmOffset Absolute offset of the (first) xgm root sector
   * @param offset Absolute offset in bytes of sector
   * @param sector Sector image
   */
  public static RootSector parse(long xgmOffset, long offset, byte[] sector)
  {
    return parse(xgmOffset, offset, sector, 0);
  }

  /**
   * Parse a given sector as root sector.
   *
   * @param xgmOffset Absolute offset of the (first) xgm root sector
   * @param offset Absolute offset in bytes of disk image part
   * @param disk Disk image part
   * @param index Index of root sector in disk image part
   */
  public static RootSector parse(long xgmOffset, long offset, byte[] disk, int index)
  {
    long size = getInt32(disk, index + 0x1C2) * 512;
    RootSector result = new RootSector(offset, size);

    for (int i = 0; i < 4; i++)
    {
      Partition partition = Partition.parse(i, disk, index + 0x1C6 + i * 12);
      partition.setOffset(partition.isXGM()? xgmOffset + index : offset + index);
      result.add(partition);
    }

    for (int i = 0; i < 8; i++)
    {
      Partition partition = Partition.parse(i + 4, disk, index + 0x156 + i * 12);
      partition.setOffset(partition.isXGM()? xgmOffset + index : offset + index);
      result.add(partition);
    }

    return result;
  }
}
