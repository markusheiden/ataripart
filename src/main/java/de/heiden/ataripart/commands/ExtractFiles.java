package de.heiden.ataripart.commands;

import de.heiden.ataripart.image.ImageReader;
import de.heiden.ataripart.image.Partition;
import de.heiden.ataripart.image.RootSector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.System.out;

/**
 * Extract all files from all partitions.
 */
public class ExtractFiles {
    /**
     * Hard disk image.
     */
    private ImageReader image;

    /**
     * Copy all files from all partitions of the hard disk image to a directory.
     */
    public void extract(File file, File destinationDir) throws Exception {
        image = new ImageReader(file);

        List<RootSector> rootSectors = image.readRootSectors();

        out.println("Using hard disk image " + file.getCanonicalPath());
        out.println("Creating extraction directory " + destinationDir.getCanonicalPath());
        destinationDir.mkdirs();

        char partitionName = 'c';
        for (RootSector rootSector : rootSectors) {
            for (Partition partition : rootSector.getRealPartitions()) {
                String prefix = "Partition " + Character.toUpperCase(partitionName) + ": ";

                File partitionDir = new File(destinationDir, Character.toString(partitionName));
                out.println(prefix + "Creating directory " + partitionDir.getCanonicalPath());
                partitionDir.mkdir();
                out.println(prefix + "Copying contents to " + partitionDir.getCanonicalPath());
                exec("mcopy",
                        "-snmi",
                        // From this image file at the given offset.
                        file.getCanonicalPath() + "@@" + partition.getAbsoluteStart(),
                        // Everything from partition.
                        "::*",
                        // Copy into this directory.
                        partitionDir.getCanonicalPath());

                partitionName++;
            }
        }

        image.close();
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
