package de.heiden.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import de.heiden.AtariPart;
import de.heiden.Partition;
import de.heiden.RootSector;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The extract command creates a script which extracts all partitions and their contents.
 */
@Parameters(commandDescription = "Create script which extracts all partitions and their contents to a directory")
public class ExtractCommand
{
  @Parameter(description = "[Hard disk image] [Directory to copy partition contents to]")
  public List<File> images;

  /**
   * Generate script which first extracts the partitions from the disk image
   * and afterwards copies all files from the partitions to the local file system.
   *
   * These two steps are need, because file copy via mtools does not always succeed,
   * if done with offset from the complete disk image.
   */
  public void createScript() throws IOException
  {
    if (images.isEmpty())
    {
      throw new ParameterException("No hard disk image specified");
    }

    AtariPart atariPart = new AtariPart(images.get(0).getCanonicalFile());

    File destinationDir = new File(images.size() >= 2?
      images.get(1).getAbsolutePath() : "./atari").getCanonicalFile();

    List<RootSector> rootSectors = atariPart.readRootSectors();

    StringBuilder part1 = new StringBuilder(1024);
    StringBuilder part2 = new StringBuilder(1024);
    part2.append("export MTOOLS_SKIP_CHECK=1\n\n");

    char partitionName = 'c';

    part1.append("mkdir " + destinationDir + "\n");
    for (RootSector rootSector : rootSectors)
    {
      for (Partition partition : rootSector.getRealPartitions())
      {
        File destinationFile = new File(destinationDir, partitionName + ".img");
        part1.append("dd if=" + atariPart.getFile().getAbsolutePath() + " bs=512 skip=" + partition.getAbsoluteStart() / 512 + " count=" + partition.getLength() / 512 + " of=" + destinationFile.getAbsolutePath() + "\n");

        File partitionDir = new File(destinationDir, Character.toString(partitionName));
        part2.append("mkdir " + partitionDir.getAbsolutePath() + "\n");
        part2.append("mcopy -snmi " + destinationFile.getAbsolutePath() + " \"::*\" " + partitionDir.getAbsolutePath() + "\n");

        partitionName++;
      }
    }

    System.out.println(part1);
    System.out.println(part2);
  }
}
