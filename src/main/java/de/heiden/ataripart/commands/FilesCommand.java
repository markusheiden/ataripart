package de.heiden.ataripart.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import de.heiden.ataripart.AtariPart;
import de.heiden.ataripart.Partition;
import de.heiden.ataripart.RootSector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.System.out;

/**
 * Extract all files from all partitions.
 */
@Parameters(commandDescription = "Extract all files from all partitions to a directory. Needs mtools installed.")
public class FilesCommand {
    @Parameter(description = "[Hard disk image] [Directory to copy files to]")
    public List<File> images;

    /**
     * Copy all files from all partitions of the hard disk image to a directory.
     */
    public void extract() throws Exception {
        if (images.isEmpty()) {
            throw new ParameterException("No hard disk image specified");
        }

        AtariPart atariPart = new AtariPart(images.get(0).getCanonicalFile());

        File destinationDir = new File(images.size() >= 2 ?
                images.get(1).getAbsolutePath() : "./atari").getCanonicalFile();

        List<RootSector> rootSectors = atariPart.readRootSectors();

        out.println("Using hard disk image " + atariPart.getFile().getCanonicalPath());
        out.println("Creating extraction directory " + destinationDir.getAbsolutePath());
        destinationDir.mkdirs();

        char partitionName = 'c';
        for (RootSector rootSector : rootSectors) {
            for (Partition partition : rootSector.getRealPartitions()) {
                String prefix = "Partition " + Character.toUpperCase(partitionName) + ": ";

                File partitionDir = new File(destinationDir, Character.toString(partitionName));
                out.println(prefix + "Creating directory " + partitionDir.getAbsolutePath());
                partitionDir.mkdir();
                out.println(prefix + "Copying contents to " + partitionDir.getAbsolutePath());
                exec("mcopy",
                        "-snmi",
                        // From this image file at the given offset.
                        atariPart.getFile().getAbsolutePath() + "@@" + partition.getAbsoluteStart(),
                        // Everything from partition.
                        "::*",
                        // Copy into this directory.
                        partitionDir.getAbsolutePath());

                partitionName++;
            }
        }
    }

    /**
     * Synchronously execute system command.
     *
     * @param command Command
     */
    private void exec(String... command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        // Ignore invalid boot sectors. Atari boot sectors seem to be not compatible with MS DOS.
        builder.environment().put("MTOOLS_SKIP_CHECK", "1");
        builder.inheritIO();
        Process p = builder.start();
        int result = p.waitFor();
        if (result != 0) {
            throw new IOException("Command failed with exit code " + result);
        }
        Thread.sleep(1);
    }
}
