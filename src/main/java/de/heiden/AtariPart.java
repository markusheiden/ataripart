package de.heiden;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Atari partition analyzer.
 */
public class AtariPart
{
  /**
   * File with hard disk image.
   */
  private final RandomAccessFile file;

  /**
   * Start this tool.
   *
   * @param args args[0] has to hold the hard disk image file
   */
  public static void main(String[] args) throws IOException
  {
    RandomAccessFile file = new RandomAccessFile(args[0], "r");

    AtariPart atariPart = new AtariPart(file);

//    atariPart.analyze();

    List<RootSector> rootSectors = atariPart.readRootSectors();

    char partitionName = 'C';
    long maxOffset = 0;
    for (RootSector rootSector : rootSectors)
    {
      System.out.println(rootSector);

      for (Partition partition : rootSector.getPartitions())
      {
        if (partition.isBGM())
        {
          if (partition.getAbsoluteEnd() > maxOffset)
          {
            maxOffset = partition.getAbsoluteEnd();
          }
          System.out.println(partition.toString(Character.toString(partitionName++)));
        }
        else if (partition.isXGM())
        {
          System.out.println(partition.toString("container"));
        }
      }
    }

    long size = rootSectors.get(0).getSize();

    // Output backup root sector only if existing and valid
    if (maxOffset < size)
    {
      RootSector backupRootSector = atariPart.readRootSector(0, 0, size - 512);
      if (backupRootSector.hasValidPartitions())
      {
        System.out.println("Last (backup) " + backupRootSector);

        for (Partition backupPartition : backupRootSector.getPartitions())
        {
          if (backupPartition.isValid())
          {
            System.out.println(backupPartition.toString());
          }
        }
      }
    }

    System.out.println("Disk ends at " + size);

    // Generate script which copies all files of all partitions from the disk image into the file system
    System.out.println();
    System.out.println("MTools Script: (make sure you have MTOOLS_SKIP_CHECK=1 set in your .mtoolsrc)");
    partitionName = 'c';
    for (RootSector rootSector : rootSectors)
    {
      for (Partition partition : rootSector.getPartitions())
      {
        if (partition.isBGM())
        {
          String destDir = "atari/" + Character.toString(partitionName++);
          System.out.println("mkdir -p " + destDir);
          System.out.println("mcopy -snmi " + args[0] + "@@" + partition.getAbsoluteStart() + " \"::*\" " + destDir);
        }
      }
    }


    // Generate script which extract the partitions from the disk image
    System.out.println();
    System.out.println("dd Script:");
    partitionName = 'c';
    for (RootSector rootSector : rootSectors)
    {
      for (Partition partition : rootSector.getPartitions())
      {
        if (partition.isBGM())
        {
          System.out.println("dd if=" + args[0] + " bs=512 skip=" + partition.getAbsoluteStart() / 512 + " count=" + partition.getLength() / 512 + " of=atari_" + partitionName++ + ".dsk");
        }
      }
    }
  }

  /**
   * Scan disk image for root sectors.
   */
  public void analyze() throws IOException
  {
    byte[] buffer = new byte[16 * 1024 * 1024];

    long offset = 0;
    for (int num; (num = file.read(buffer)) >=0; offset += num)
    {
      for (int i = 0; i < num; i += 512)
      {
        RootSector rootSector = RootSector.parse(offset, offset, buffer, i);
        if (rootSector.hasValidPartitions())
        {
          System.out.print(offset + i);
          System.out.print(": Possible ");
          System.out.print(rootSector.toString());
        }
      }
    }
  }

  /**
   * Read main root sector and all xgm root sectors.
   */
  public List<RootSector> readRootSectors() throws IOException
  {
    List<RootSector> result = new ArrayList<>();
    readRootSectors(0, 0, result);
    return result;
  }

  /**
   * Read root sector and all xgm root sectors.
   *
   * @param xgmOffset Absolute offset of the (first) xgm root sector
   * @param offset Absolute offset to read the root sector from
   * @param result Resulting list with all root sectors
   */
  private void readRootSectors(long xgmOffset, long offset, List<RootSector> result) throws IOException
  {
    RootSector rootSector = readRootSector(xgmOffset, offset, offset);
    result.add(rootSector);

    for (Partition partition : rootSector.getPartitions())
    {
      if (partition.isValid() && partition.isActive() && partition.isXGM())
      {
        if (xgmOffset == 0)
        {
          // remember the offset of the (first) xgm root sector.
          readRootSectors(partition.getAbsoluteStart(), partition.getAbsoluteStart(), result);
        }
        else
        {
          // the offsets of all following xgm root sectors are relative to the first xgm root sector.
          readRootSectors(xgmOffset, xgmOffset + partition.getStart(), result);
        }

        // only one xgm partition per root sector is allowed
        break;
      }
    }
  }

  /**
   * Read root sector (non-recursively).
   *
   * @param xgmOffset Absolute offset of the (first) xgm root sector
   * @param offset Logical offset in disk image, normally should be set to diskOffset
   * @param diskOffset Offset in disk image to read first root sector from
   */
  private RootSector readRootSector(long xgmOffset, long offset, long diskOffset) throws IOException
  {
    byte[] buffer = new byte[512];
    file.seek(diskOffset);
    file.readFully(buffer);

    return RootSector.parse(xgmOffset, offset, buffer);
  }

  //
  //
  //

  /**
   * Constructor.
   *
   * @param file The hard disk image
   */
  public AtariPart(RandomAccessFile file)
  {
    this.file = file;
  }

  @Override
  protected void finalize() throws Throwable
  {
    try
    {
      close();
    }
    finally
    {
      super.finalize();
    }
  }

  /**
   * Close this tool.
   * This will release the underlying hard disk image file.
   */
  public void close() throws IOException
  {
    file.close();
  }
}
