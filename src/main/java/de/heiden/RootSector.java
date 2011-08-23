package de.heiden;

import java.util.ArrayList;
import java.util.List;

import static de.heiden.IntUtils.getInt32;

/**
 * Root sector info.
 */
public class RootSector
{
  private final long offset;

  private final long size;

  private final List<Partition> partitions = new ArrayList<>();

  public RootSector(long offset, long size)
  {
    this.offset = offset;
    this.size = size;
  }

  public long getOffset()
  {
    return offset;
  }

  public boolean isXGM()
  {
    return offset > 0;
  }

  public long getSize()
  {
    return size;
  }

  public long getEnd()
  {
    return offset + size;
  }

  public List<Partition> getPartitions()
  {
    return partitions;
  }

  public void add(Partition partition)
  {
    partitions.add(partition);
  }

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

  public String toString()
  {
    StringBuilder result = new StringBuilder(1024);
    result.append("Root sector\n");
    result.append("Start : ").append(offset).append("\n");
    result.append("First : ").append(offset + 512).append("\n");
    if (!isXGM())
    {
      result.append("Size  : ").append(size).append("\n");
      result.append("End   : ").append(getEnd()).append("\n");
    }

    return result.toString();
  }

  //
  //
  //

  public static RootSector parse(long offset, byte[] disk)
  {
    return parse(offset, disk, 0);
  }

  public static RootSector parse(long offset, byte[] disk, int index)
  {
    long size = getInt32(disk, index + 0x1C2) * 512;
    RootSector result = new RootSector(offset, size);

    for (int i = 0; i < 4; i++)
    {
      result.add(Partition.parse(i, offset + index, disk, index + 0x1C6 + i * 12));
    }

    for (int i = 0; i < 8; i++)
    {
      result.add(Partition.parse(i + 4, offset + index, disk, index + 0x156 + i * 12));
    }

    return result;
  }
}
