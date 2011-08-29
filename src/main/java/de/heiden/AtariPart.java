package de.heiden;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import de.heiden.commands.AnalyzeCommand;
import de.heiden.commands.ExtractCommand;
import de.heiden.commands.HelpOption;
import de.heiden.commands.ListCommand;

import java.io.File;
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
  private final File file;

  /**
   * File with hard disk image.
   */
  private final RandomAccessFile image;

  /**
   * Start this tool.
   *
   * @param args args[0] has to hold the hard disk image file
   */
  public static void main(String[] args) throws IOException
  {
    JCommander commander = new JCommander();
    commander.setProgramName(AtariPart.class.getSimpleName());
    HelpOption help = new HelpOption();
    commander.addObject(help);
    AnalyzeCommand analyze = new AnalyzeCommand();
    commander.addCommand("analyze", analyze);
    ListCommand list = new ListCommand();
    commander.addCommand("list", list);
    ExtractCommand extract = new ExtractCommand();
    commander.addCommand("extract", extract);

    try
    {
      commander.parse(args);

      if (help.isHelp())
      {
        help.help(commander);
        return;
      }

      switch (commander.getParsedCommand())
      {
        case "analyze": analyze.analyze(); return;
        case "list": list.list(); return;
        case "extract": extract.createScript(); return;
      }
    }
    catch (ParameterException e)
    {
      System.err.println(e.getLocalizedMessage());
      help.help(commander);
      System.exit(-1);
      return;
    }
  }

  /**
   * Display all detected valid partitions.
   *
   * @param backup Display backup root sectors?
   */
  public void list(boolean backup) throws IOException
  {
    List<RootSector> rootSectors = readRootSectors();
    if (rootSectors.isEmpty())
    {
      System.out.println("No valid root sectors found");
      return;
    }
    RootSector masterRootSector = rootSectors.get(0);

    long maxOffset = displayPartitions(rootSectors);

    System.out.println("Disk ends at " + masterRootSector.getSize());

    if (backup)
    {
      System.out.println();
      displayFirstBackupRootSector(masterRootSector);
      displayLastBackupRootSector(masterRootSector, maxOffset);
    }
  }

  /**
   * Display all detected valid partitions.
   *
   * @param rootSectors Detected root sectors
   * @return Maximum offset, that is used by any detected partition
   */
  public long displayPartitions(List<RootSector> rootSectors)
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
  public void displayFirstBackupRootSector(RootSector masterRootSector) throws IOException
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
  public void displayLastBackupRootSector(RootSector masterRootSector, long maxOffset) throws IOException
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
   * Scan disk image for root sectors.
   * Does NOT evaluate partition information to follow xgm partitions.
   */
  public void analyze() throws IOException
  {
    byte[] buffer = new byte[16 * 1024 * 1024];

    long offset = 0;
    for (int num; (num = image.read(buffer)) >=0; offset += num)
    {
      for (int i = 0; i < num; i += 512)
      {
        RootSector rootSector = RootSector.parse(offset, offset, buffer, i);
        if (rootSector.hasValidPartitions())
        {
          System.out.print(offset + i);
          System.out.print(": Possible ");
          System.out.println(rootSector.toString());

          for (Partition partition : rootSector.getPartitions())
          {
            System.out.println(partition.toString());
          }
        }
      }
    }
  }

  //
  //
  //

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
    image.seek(diskOffset);
    image.readFully(buffer);

    // Read root sector with partitions
    RootSector result = RootSector.parse(xgmOffset, offset, buffer);

    // Read BIOS parameter blocks for real partitions
    for (Partition partition : result.getRealPartitions())
    {
      if (partition.getAbsoluteStart() + 512 <= image.length())
      {
        image.seek(partition.getAbsoluteStart());
        image.readFully(buffer);

        partition.setBootSector(BootSector.parse(buffer, 0));
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
   * @param file The file with the hard disk image
   */
  public AtariPart(File file) throws IOException
  {
    this.file = file;
    this.image = new RandomAccessFile(file.getCanonicalFile(), "r");
  }

  /**
   * File with the hard disk image.
   */
  public File getFile()
  {
    return file;
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
    image.close();
  }
}
