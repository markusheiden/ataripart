package de.heiden.ataripart;

import de.heiden.ataripart.commands.AnalyzeImage;
import de.heiden.ataripart.commands.ExtractFiles;
import de.heiden.ataripart.commands.ExtractPartitions;
import de.heiden.ataripart.commands.ListPartitions;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Atari partition analyzer.
 */
@Command(name = "ataripart",
        description = "Atari partition analyzer",
        mixinStandardHelpOptions = true, usageHelpWidth = 120, version = "1.0.0",
        subcommands = {
        HelpCommand.class,
        AtariPart.AnalyzeCommand.class,
        AtariPart.ListCommand.class,
        AtariPart.PartitionsCommand.class,
        AtariPart.FilesCommand.class
})
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

    @Command(name = "analyze", description = "Search a whole hard disk image for root sectors.")
    public static class AnalyzeCommand implements Callable<Void> {
        @Parameters(arity = "1", paramLabel = "image", description = "Hard disk image")
        private File image;

        @Override
        public Void call() throws Exception {
            new AnalyzeImage().analyze(image);
            return null;
        }
    }

    @Command(name = "list", description = "List all root sectors and their partitions, starting with the MBR.")
    public static class ListCommand implements Callable<Void> {
        @Option(names = {"-b", "--backup"}, description = "Display backup root sectors, if any")
        private boolean backup = false;

        @Parameters(arity = "1", paramLabel = "image", description = "Hard disk image")
        private File image;

        @Override
        public Void call() throws Exception {
            new ListPartitions().list(image, backup);
            return null;
        }
    }

    @Command(name = "partitions", description = "Extract all partitions to a directory.")
    public static class PartitionsCommand implements Callable<Void> {
        @Option(names = {"-c", "--convert"}, description = "Convert boot sectors to MS DOS format")
        private boolean convertBootSectors = false;

        @Parameters(index = "0", paramLabel = "image", description = "Hard disk image")
        private File image;

        @Parameters(index = "1", paramLabel = "destination", description = "Directory to copy partition contents to", defaultValue = "./atari")
        private File destinationDir;

        /**
         * Extract all partitions of the hard disk image to a directory.
         */
        @Override
        public Void call() throws Exception {
            new ExtractPartitions().extract(image, convertBootSectors, destinationDir);
            return null;
        }
    }

    @Command(name = "files", description = "Extract all files from all partitions to a directory. Needs mtools installed.")
    public static class FilesCommand implements Callable<Void> {
        @Parameters(index = "0", paramLabel = "image", description = "Hard disk image")
        private File image;

        @Parameters(index = "1", paramLabel = "destination", description = "Directory to copy files to", defaultValue = "./atari")
        private File destinationDir;

        /**
         * Copy all files from all partitions of the hard disk image to a directory.
         */
        @Override
        public Void call() throws Exception {
            new ExtractFiles().extract(image, destinationDir);
            return null;
        }
    }
}
