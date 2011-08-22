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
  private final RandomAccessFile file;

  public static void main(String[] args) throws IOException
  {
    RandomAccessFile file = new RandomAccessFile(args[0], "r");

    AtariPart atariPart = new AtariPart(file);

//    atariPart.analyze();

    List<RootSector> rootSectors = atariPart.readRootSectors();
    for (RootSector rootSector : rootSectors)
    {
      System.out.println(rootSector);
    }
  }

  private void analyze() throws IOException
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

  private List<RootSector> readRootSectors() throws IOException
  {
    List<RootSector> result = new ArrayList<>();
    readRootSectors(0, 0, result);
    return result;
  }

  private void readRootSectors(long previousOffset, long offset, List<RootSector> result) throws IOException
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
        readRootSectors(offset, previousOffset + partition.getStart(), result);
      }
    }

  }

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

  public void close() throws IOException
  {
    file.close();
  }
}
