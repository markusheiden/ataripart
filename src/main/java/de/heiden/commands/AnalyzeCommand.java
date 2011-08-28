package de.heiden.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.heiden.AtariPart;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The analyze command searches a whole disk image for root sectors.
 */
@Parameters(commandDescription = "Search a whole disk image for root sectors")
public class AnalyzeCommand
{
  @Parameter(description = "[Hard disk image]")
  public List<File> images;

  public void analyze() throws IOException
  {
    new AtariPart(images.get(0)).analyze();
  }
}
