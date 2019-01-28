package de.heiden.ataripart;

import de.heiden.ataripart.commands.AnalyzeImage;
import de.heiden.ataripart.commands.ExtractFiles;
import de.heiden.ataripart.commands.ExtractPartitions;
import de.heiden.ataripart.commands.ListPartitions;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Atari partition analyzer.
 */
@Command(name = "ataripart",
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
            cl.parseWithHandler(new RunLast(), args);

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoSuchFileException) {
                System.err.println("File " + cause.getMessage() + " not found.");
            } else if (cause instanceof FileAlreadyExistsException) {
                System.err.println("File " + cause.getMessage() + " already exists.");
            } else {
                System.err.println(cause.getLocalizedMessage());
            }
            // e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        // Nothing to do.
    }

    @Command(description = "Search a whole hard disk image for root sectors.")
    private void analyze(
            @Parameters(index = "0", paramLabel = "image", description = "Hard disk image") Path image)
            throws Exception {

        new AnalyzeImage().analyze(image);
    }

    @Command(description = "List all root sectors and their partitions, starting with the MBR.")
    private void list(
            @Option(names = {"-b", "--backup"}, description = "Display backup root sectors, if any") boolean backup,
            @Parameters(index = "0", paramLabel = "image", description = "Hard disk image") Path image)
            throws Exception {

        new ListPartitions().list(image, backup);
    }

    /**
     * Extract all partitions of the hard disk image to a directory.
     */
    @Command(description = "Extract all partitions to a directory.")
    private void partitions(
            @Option(names = {"-c", "--convert"}, description = "Convert boot sectors to MS DOS format") boolean convertBootSectors,
            @Parameters(index = "0", paramLabel = "image", description = "Hard disk image") Path image,
            @Parameters(index = "1", paramLabel = "destination", description = "Directory to copy partition contents to", defaultValue = "./atari") Path destinationDir)
            throws Exception {

        new ExtractPartitions().extract(image, convertBootSectors, destinationDir);
    }

    /**
     * Copy all files from all partitions of the hard disk image to a directory.
     */
    @Command(description = "Extract all files from all partitions to a directory.")
    private void files(
            @Parameters(index = "0", paramLabel = "image", description = "Hard disk image") Path image,
            @Parameters(index = "1", paramLabel = "destination", description = "Directory to copy files to", defaultValue = "./atari") Path destinationDir)
            throws Exception {

        new ExtractFiles().extract(image, destinationDir);
    }
}
