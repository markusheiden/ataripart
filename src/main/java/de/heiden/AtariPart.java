package de.heiden;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static de.heiden.IntUtils.hexDump;

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

//    for (RootSector rootSector : rootSectors)
//    {
//      System.out.println(rootSector);
//    }

    char partitionName = 'C';
    for (RootSector rootSector : rootSectors)
    {
      System.out.println(rootSector);

      for (Partition partition : rootSector.getPartitions())
      {
        if (partition.isBGM())
        {
          System.out.println(partition.toString(Character.toString(partitionName++)));
        }
      }
    }

    System.out.println("Disk ends at " + rootSectors.get(0).getSize());
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
        RootSector rootSector = RootSector.parse(offset, buffer, i);
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
   * @param offset Offset in disk image to read first root  sector from
   * @param xgmOffset Offset of the (first) xgm root sector
   * @param result Resulting list with all root sectors
   */
  private void readRootSectors(long xgmOffset, long offset, List<RootSector> result) throws IOException
  {
    byte[] buffer = new byte[512];
    file.seek(offset);
    file.readFully(buffer);

    RootSector rootSector = RootSector.parse(offset, buffer);
    result.add(rootSector);

    for (Partition partition : rootSector.getPartitions())
    {
      if (partition.isValid() && partition.isActive() && partition.isXGM())
      {
        if (xgmOffset == 0)
        {
          // remember the offset of the (first) xgm root sector.
          // the offsets of all following xgm root sectors are relative to the first xgm root sector.
          xgmOffset = offset;
        }
        readRootSectors(xgmOffset, xgmOffset + partition.getStart(), result);
      }
    }
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
