package de.heiden.commands;

import com.beust.jcommander.Parameter;
import de.heiden.AtariPart;
import de.heiden.Partition;
import de.heiden.RootSector;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The extract command creates a script which extracts all partitions and their contents.
 */
public class ExtractCommand
{
  @Parameter(description = "Image file to analyze, directory to create partitions", arity = 2)
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
    File file = images.get(0).getCanonicalFile();
    String destinationDir = images.get(1).getAbsolutePath();
    AtariPart atariPart = new AtariPart(file);

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
        String destinationFile = destinationDir + "/" + partitionName + ".img";
        part1.append("dd if=" + file.getAbsolutePath() + " bs=512 skip=" + partition.getAbsoluteStart() / 512 + " count=" + partition.getLength() / 512 + " of=" + destinationFile + "\n");

        String partitionDir = destinationDir + "/" + partitionName;
        part2.append("mkdir " + partitionDir + "\n");
        part2.append("mcopy -snmi " + destinationFile + " \"::*\" " + partitionDir + "\n");

        partitionName++;
      }
    }

    System.out.println(part1);
    System.out.println(part2);
  }
}
