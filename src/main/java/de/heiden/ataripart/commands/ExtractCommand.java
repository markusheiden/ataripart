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
 * The extract command creates a script which extracts all partitions and their contents.
 */
@Parameters(commandDescription = "Extract all partitions and their contents to a directory. Needs dd and mtools installed.")
public class ExtractCommand {
    @Parameter(description = "[Hard disk image] [Directory to copy partition contents to]")
    public List<File> images;

    /**
     * First extracts the partitions from the disk image and afterwards
     * copies all files from the partitions to the local file system.
     * <p>
     * These two steps are need, because file copy via mtools does not always succeed,
     * if done with offset from the complete disk image.
     */
    public void createScript() throws IOException {
        if (images.isEmpty()) {
            throw new ParameterException("No hard disk image specified");
        }

        AtariPart atariPart = new AtariPart(images.get(0).getCanonicalFile());

        File destinationDir = new File(images.size() >= 2 ?
                images.get(1).getAbsolutePath() : "./atari").getCanonicalFile();

        List<RootSector> rootSectors = atariPart.readRootSectors();

        try {
            out.println("Using hard disk image " + atariPart.getFile().getCanonicalPath());
            out.println("Creating extraction directory " + destinationDir.getAbsolutePath());
            destinationDir.mkdirs();

            char partitionName = 'c';
            for (RootSector rootSector : rootSectors) {
                for (Partition partition : rootSector.getRealPartitions()) {
                    String prefix = "Partition " + Character.toUpperCase(partitionName) + ": ";

                    File destinationFile = new File(destinationDir, partitionName + ".img");
                    out.println(prefix + "Creating image " + destinationFile.getAbsolutePath());
                    exec("dd",
                            "if=" + atariPart.getFile().getAbsolutePath(),
                            "bs=512",
                            "skip=" + partition.getAbsoluteStart() / 512,
                            "count=" + partition.getLength() / 512,
                            "of=" + destinationFile.getAbsolutePath());

                    File partitionDir = new File(destinationDir, Character.toString(partitionName));
                    out.println(prefix + "Creating directory " + partitionDir.getAbsolutePath());
                    partitionDir.mkdir();
                    out.println(prefix + "Copying contents to " + partitionDir.getAbsolutePath());
                    exec("mcopy",
                            "-snmi", destinationFile.getAbsolutePath(),
                            "::*", partitionDir.getAbsolutePath());

                    partitionName++;
                }
            }
        } catch (InterruptedException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    /**
     * Synchronously execute system command.
     *
     * @param command Command
     */
    private void exec(String... command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
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
