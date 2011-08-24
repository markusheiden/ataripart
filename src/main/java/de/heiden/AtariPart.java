package de.heiden;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
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
    try
    {
      atariPart.start();
    }
    finally
    {
      atariPart.close();
    }
  }

  private void start() throws IOException
  {
//    analyze();

    List<RootSector> rootSectors = readRootSectors();

    long maxOffset = displayPartitions(rootSectors);

    RootSector masterRootSector = rootSectors.get(0);
    System.out.println("Disk ends at " + masterRootSector.getSize());

    displayFirstBackupRootSector(masterRootSector);

    displayLastBackupRootSector(masterRootSector, maxOffset);

//    createMtoolsScript(args[0], rootSectors);

//    createDDScript(args[0], rootSectors);
  }

  /**
   * Display all detected valid partitions.
   *
   * @param rootSectors Detected root sectors
   * @return Maximum offset, that is used by any detected partition
   */
  private long displayPartitions(List<RootSector> rootSectors)
  {
    char partitionName = 'C';
    long maxOffset = 0;
    for (RootSector rootSector : rootSectors)
    {
      System.out.println(rootSector);

      for (Partition partition : rootSector.getAllPartitions())
      {
        if (!partition.isValid())
        {
          continue;
        }

        if (partition.isXGM())
        {
          System.out.println(partition.toString("container"));
        }
        else
        {
          if (partition.getAbsoluteEnd() > maxOffset)
          {
            maxOffset = partition.getAbsoluteEnd();
          }
          System.out.println(partition.toString(Character.toString(partitionName++)));
        }
      }
    }
    return maxOffset;
  }

  /**
   * Output first backup root sector only if existing and valid.
   *
   * @param masterRootSector First (master) root sector
   */
  private void displayFirstBackupRootSector(RootSector masterRootSector) throws IOException
  {
    long offset = masterRootSector.getOffset() + 512;
    if (!masterRootSector.getRealPartitions().isEmpty() && offset < masterRootSector.getRealPartitions().get(0).getAbsoluteStart())
    {
      RootSector backupRootSector = readRootSector(0, 0, offset);
      if (backupRootSector.hasValidPartitions())
      {
        System.out.println("First (backup) " + backupRootSector);

        for (Partition backupPartition : backupRootSector.getAllPartitions())
        {
          if (backupPartition.isValid())
          {
            System.out.println(backupPartition.toString());
          }
        }
      }
    }
  }

  /**
   * Output last backup root sector only if existing and valid.
   *
   * @param masterRootSector First (master) root sector
   * @param maxOffset Maximum offset that it used by any partition
   */
  private void displayLastBackupRootSector(RootSector masterRootSector, long maxOffset) throws IOException
  {
    long size = masterRootSector.getSize();
    if (maxOffset < size)
    {
      RootSector backupRootSector = readRootSector(0, 0, size - 512);
      if (backupRootSector.hasValidPartitions())
      {
        System.out.println("Last (backup) " + backupRootSector);

        for (Partition backupPartition : backupRootSector.getAllPartitions())
        {
          if (backupPartition.isValid())
          {
            System.out.println(backupPartition.toString());
          }
        }
      }
    }
  }

  /**
   * Generate script which copies all files of all partitions from the disk image into the file system.
   *
   * @param filename Name of disk image file
   * @param rootSectors Detected root sectors
   */
  private void createMtoolsScript(String filename, List<RootSector> rootSectors)
  {
    char partitionName;
    System.out.println();
    System.out.println("MTools Script: (make sure you have MTOOLS_SKIP_CHECK=1 set in your .mtoolsrc)");
    partitionName = 'c';
    for (RootSector rootSector : rootSectors)
    {
      for (Partition partition : rootSector.getRealPartitions())
      {
        String destinationDir = "atari/" + Character.toString(partitionName++);
        System.out.println("mkdir -p " + destinationDir);
        System.out.println("mcopy -snmi " + filename + "@@" + partition.getAbsoluteStart() + " \"::*\" " + destinationDir);
      }
    }
  }

  /**
   * Generate script which extracts the partitions from the disk image.
   *
   * @param filename Name of disk image file
   * @param rootSectors Detected root sectors
   */
  private void createDDScript(String filename, List<RootSector> rootSectors)
  {
    System.out.println();
    System.out.println("dd Script:");
    char partitionName = 'c';
    for (RootSector rootSector : rootSectors)
    {
      for (Partition partition : rootSector.getRealPartitions())
      {
        System.out.println("dd if=" + filename + " bs=512 skip=" + partition.getAbsoluteStart() / 512 + " count=" + partition.getLength() / 512 + " of=atari_" + partitionName++ + ".dsk");
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
      if (partition.isXGM())
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

    // Read root sector with partitions
    RootSector result = RootSector.parse(xgmOffset, offset, buffer);

    // Read BIOS parameter blocks for real partitions
    for (Partition partition : result.getRealPartitions())
    {
      if (partition.getAbsoluteStart() + 512 <= file.length())
      {
        file.seek(partition.getAbsoluteStart());
        file.readFully(buffer);

        partition.setBiosParameterBlock(BiosParameterBlock.parse(buffer, 0));
      }
    }

    return result;
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
