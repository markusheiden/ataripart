package de.heiden.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.heiden.AtariPart;
import de.heiden.RootSector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * The list command list all root sectors and its partitions, starting with the mbr.
 */
@Parameters(commandDescription = "List all root sectors and its partitions, starting with the mbr")
public class ListCommand
{
  @Parameter(names = {"-b"}, description = "Display backup boot sectors, if any")
  private boolean backup;

  @Parameter(description = "Image file to analyze", arity = 1)
  private List<File> images;

  public void list() throws IOException
  {
    AtariPart atariPart = new AtariPart(images.get(0).getAbsolutePath(), "");

    List<RootSector> rootSectors = atariPart.readRootSectors();
    if (rootSectors.isEmpty())
    {
      System.out.println("No valid root sectors found");
      return;
    }
    RootSector masterRootSector = rootSectors.get(0);

    long maxOffset = atariPart.displayPartitions(rootSectors);

    System.out.println("Disk ends at " + masterRootSector.getSize());

    if (backup)
    {
      System.out.println();
      atariPart.displayFirstBackupRootSector(masterRootSector);
      atariPart.displayLastBackupRootSector(masterRootSector, maxOffset);
    }
  }
}
