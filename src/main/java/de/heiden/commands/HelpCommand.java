package de.heiden.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.heiden.AtariPart;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The help command displays a help message.
 */
@Parameters(commandDescription = "Help")
public class HelpCommand
{
  public void help(JCommander commander) throws IOException
  {
    commander.usage();
  }
}
