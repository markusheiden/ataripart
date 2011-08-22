package de.heiden;

import java.util.ArrayList;
import java.util.List;

import static de.heiden.IntUtils.getInt32;

/**
 * Root sector info.
 */
public class RootSector
{
  private final long size;

  private final List<Partition> partitions = new ArrayList<>();

  public RootSector(long size)
  {
    this.size = size;
  }

  public long getSize()
  {
    return size;
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
    result.append("-----------\n");
    result.append("\n");

    for (Partition partition : partitions)
    {
      if (partition.isValid())
      {
        result.append(partition.toString());
        result.append("\n");
      }
    }

    return result.toString();
  }

  //
  //
  //

  public static RootSector parse(byte[] disk)
  {
    return parse(disk, 0);
  }

  public static RootSector parse(byte[] disk, int offset)
  {
    long size = getInt32(disk, offset + 0x152) * 512;
    RootSector result = new RootSector(size);

    for (int i = 0; i < 4; i++)
    {
      result.add(Partition.parse(i, disk, offset + 0x1C6 + i * 12));
    }

    for (int i = 0; i < 8; i++)
    {
      result.add(Partition.parse(i + 4, disk, offset + 0x156 + i * 12));
    }

    return result;
  }
}
