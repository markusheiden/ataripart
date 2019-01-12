package de.heiden.ataripart.commands;

import de.heiden.ataripart.image.ImageReader;
import de.heiden.ataripart.image.Partition;
import de.heiden.ataripart.image.RootSector;
import de.waldheinz.fs.BlockDevice;
import de.waldheinz.fs.FileSystem;
import de.waldheinz.fs.FileSystemFactory;
import de.waldheinz.fs.FsDirectoryEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
     *
     * @param file The file with the hard disk image.
     * @param destinationDir Directory to write extracted files to.
     */
    public void extract(Path file, Path destinationDir) throws Exception {
        image = new ImageReader(file);

        List<RootSector> rootSectors = image.readRootSectors();

        out.println("Using hard disk image " + file.toAbsolutePath());
        out.println("Creating extraction directory " + destinationDir.toAbsolutePath());
        Files.createDirectories(destinationDir);

        char partitionName = 'c';
        for (RootSector rootSector : rootSectors) {
            for (Partition partition : rootSector.getRealPartitions()) {
                String prefix = "Partition " + Character.toUpperCase(partitionName) + ": ";

                Path partitionDir = destinationDir.resolve(Character.toString(partitionName));
                out.println(prefix + "Creating directory " + partitionDir.toAbsolutePath());
                Files.createDirectories(partitionDir);
                out.println(prefix + "Copying contents to " + partitionDir.toAbsolutePath());
                copy(file, partition, destinationDir);
                partitionName++;
            }
        }

        image.close();
    }

    private void copy(Path file, Partition partition, Path destinationDir) throws IOException {
        BlockDevice device = new PartitionDisk(file, partition);
        FileSystem fileSystem = FileSystemFactory.create(device, true);
        for (FsDirectoryEntry entry : fileSystem.getRoot()) {
            out.println(entry.getName());
            // TODO markus 2019-01-12: Recursive copy.
        }
        fileSystem.close();
        device.close();
    }
}
