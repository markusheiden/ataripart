package de.heiden.ataripart.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * The help command displays a help message.
 */
public class HelpOption {
    @Parameter(names = {"--help"}, description = "Display help")
    public Boolean help;

    public boolean isHelp() {
        return help != null && help;
    }

    public void help(JCommander commander) {
        String command = commander.getParsedCommand();
        if (command != null) {
            commander.usage(command);
        } else {
            commander.usage();
        }
    }
}
