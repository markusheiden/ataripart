package de.heiden.ataripart;

import de.heiden.ataripart.commands.AnalyzeImage;
import de.heiden.ataripart.commands.ExtractFiles;
import de.heiden.ataripart.commands.ExtractPartitions;
import de.heiden.ataripart.commands.ListPartitions;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;

/**
 * Atari partition analyzer.
 */
@Command(name = "java -jar ataripart.jar",
        description = "Atari partition analyzer",
        mixinStandardHelpOptions = true, usageHelpWidth = 120,
        versionProvider = VersionProvicer.class,
        subcommands = { HelpCommand.class })
public class AtariPart implements Runnable {
    /**
     * Start this tool.
     *
     * @param args args[0] has to hold the hard disk image file
     */
    public static void main(String[] args) {
        try {
            CommandLine cl = new CommandLine(new AtariPart());
            cl.parseWithHandler(new RunFirst(), args);

        } catch (ExecutionException e) {
            System.err.println(e.getCause().getLocalizedMessage());
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        // Nothing to do.
    }

    @Command(description = "Search a whole hard disk image for root sectors.")
    private void analyze(
            @Parameters(index = "0", paramLabel = "image", description = "Hard disk image") File image)
            throws Exception {

        new AnalyzeImage().analyze(image);
    }

    @Command(description = "List all root sectors and their partitions, starting with the MBR.")
    private void list(
            @Option(names = {"-b", "--backup"}, description = "Display backup root sectors, if any") boolean backup,
            @Parameters(index = "0", paramLabel = "image", description = "Hard disk image") File image)
            throws Exception {

        new ListPartitions().list(image, backup);
    }

    /**
     * Extract all partitions of the hard disk image to a directory.
     */
    @Command(description = "Extract all partitions to a directory.")
    private void partitions(
            @Option(names = {"-c", "--convert"}, description = "Convert boot sectors to MS DOS format") boolean convertBootSectors,
            @Parameters(index = "0", paramLabel = "image", description = "Hard disk image") File image,
            @Parameters(index = "1", paramLabel = "destination", description = "Directory to copy partition contents to", defaultValue = "./atari") File destinationDir)
            throws Exception {

        new ExtractPartitions().extract(image, convertBootSectors, destinationDir);
    }

    /**
     * Copy all files from all partitions of the hard disk image to a directory.
     */
    @Command(description = "Extract all files from all partitions to a directory. Needs mtools installed.")
    private void files(
            @Parameters(index = "0", paramLabel = "image", description = "Hard disk image") File image,
            @Parameters(index = "1", paramLabel = "destination", description = "Directory to copy files to", defaultValue = "./atari") File destinationDir)
            throws Exception {

        new ExtractFiles().extract(image, destinationDir);
    }
}
