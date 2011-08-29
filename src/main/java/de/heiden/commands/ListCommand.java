package de.heiden.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import de.heiden.AtariPart;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The list command list all root sectors and its partitions, starting with the mbr.
 */
@Parameters(commandDescription = "List all root sectors and their partitions, starting with the mbr")
public class ListCommand
{
  @Parameter(names = {"-b"}, description = "Display backup root sectors, if any")
  private Boolean backup;

  @Parameter(description = "[Hard disk image]")
  private List<File> images;

  public void list() throws IOException
  {
    if (images.isEmpty())
    {
      throw new ParameterException("No hard disk image specified");
    }
    new AtariPart(images.get(0)).list(backup);
  }
}
