package de.heiden.ataripart;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import de.heiden.ataripart.commands.AnalyzeImage;
import de.heiden.ataripart.commands.ExtractFiles;
import de.heiden.ataripart.commands.ExtractPartitions;
import de.heiden.ataripart.commands.ListPartitions;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Atari partition analyzer.
 */
public class AtariPart {
    /**
     * Start this tool.
     *
     * @param args args[0] has to hold the hard disk image file
     */
    public static void main(String[] args) {
        HelpOption help = new HelpOption();
        AnalyzeCommand analyze = new AnalyzeCommand();
        ListCommand list = new ListCommand();
        PartitionsCommand partitions = new PartitionsCommand();
        FilesCommand files = new FilesCommand();

        JCommander commander = JCommander
                .newBuilder()
                .programName(AtariPart.class.getSimpleName())
                .addObject(help)
                .addCommand(analyze)
                .addCommand(list)
                .addCommand(partitions)
                .addCommand(files)
                .build();

        try {
            commander.parse(args);

            if (help.isHelp()) {
                help.execute(commander);
                return;
            }

            switch (commander.getParsedCommand()) {
                case "analyze":
                    analyze.execute();
                    return;
                case "list":
                    list.execute();
                    return;
                case "partitions":
                    partitions.execute();
                    return;
                case "files":
                    files.execute();
                    return;
            }
        } catch (ParameterException | NullPointerException e) {
            System.err.println(e.getLocalizedMessage());
            help.execute(commander);
            System.exit(-1);
            return;
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
            return;
        }
    }

    public static class HelpOption {
        @Parameter(names = {"--help"}, description = "Display help")
        public Boolean help;

        public boolean isHelp() {
            return help != null && help;
        }

        public void execute(JCommander commander) {
            String command = commander.getParsedCommand();
            if (command != null) {
                commander.usage(command);
            } else {
                commander.usage();
            }
        }
    }

    @Parameters(commandNames = "analyze", commandDescription = "Search a whole disk image for root sectors")
    public static class AnalyzeCommand {
        @Parameter(description = "[Hard disk image]")
        public List<File> files;

        public void execute() throws IOException {
            if (files.isEmpty()) {
                throw new ParameterException("No hard disk image specified");
            }

            File image = files.get(0);
            new AnalyzeImage().analyze(image);
        }
    }

    @Parameters(commandNames = "list", commandDescription = "List all root sectors and their partitions, starting with the mbr")
    public static class ListCommand {
        @Parameter(names = {"-b", "--backup"}, description = "Display backup root sectors, if any")
        private Boolean backup = false;

        @Parameter(description = "[Hard disk image]")
        private List<File> files;

        public void execute() throws IOException {
            if (files.isEmpty()) {
                throw new ParameterException("No hard disk image specified");
            }

            File image = files.get(0);
            new ListPartitions().list(image, backup != null && backup);
        }
    }

    @Parameters(commandNames = "partitions", commandDescription = "Extract all partitions to a directory.")
    public static class PartitionsCommand {
        @Parameter(names = {"-c", "--convert"}, description = "Convert boot sectors to MS DOS format")
        public Boolean convertBootSectors = false;

        @Parameter(description = "[Hard disk image] [Directory to copy partition contents to]")
        public List<File> files;

        /**
         * Extract all partitions of the hard disk image to a directory.
         */
        public void execute() throws Exception {
            if (files.isEmpty()) {
                throw new ParameterException("No hard disk image specified");
            }

            File image = files.get(0);
            File destinationDir = files.size() >= 2 ? files.get(1) : new File("./atari");
            new ExtractPartitions().extract(image, convertBootSectors != null && convertBootSectors, destinationDir);
        }
    }

    @Parameters(commandNames = "files", commandDescription = "Extract all files from all partitions to a directory. Needs mtools installed.")
    public static class FilesCommand {
        @Parameter(description = "[Hard disk image] [Directory to copy files to]")
        public List<File> files;

        /**
         * Copy all files from all partitions of the hard disk image to a directory.
         */
        public void execute() throws Exception {
            if (files.isEmpty()) {
                throw new ParameterException("No hard disk image specified");
            }

            File image = files.get(0);
            File destinationDir = files.size() >= 2 ? files.get(1) : new File("./atari");
            new ExtractFiles().extract(image, destinationDir);
        }
    }
}
