package de.heiden.ataripart.commands;

import de.heiden.ataripart.image.ImageReader;
import de.heiden.ataripart.image.Partition;
import de.heiden.ataripart.image.RootSector;
import de.waldheinz.fs.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static java.lang.System.out;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Extract all files from all partitions.
 */
public class ExtractFiles {
    /**
     * Buffer for copy operations.
     */
    private final ByteBuffer buffer = ByteBuffer.allocate(4096);

    /**
     * Copy all files from all partitions of the hard disk image to a directory.
     *
     * @param file The file with the hard disk image.
     * @param destinationDir Directory to write extracted files to.
     */
    public void extract(Path file, Path destinationDir) throws Exception {
        try (ImageReader image = new ImageReader(file)) {
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
                    copy(file, partition, partitionDir);
                    partitionName++;
                }
            }
        }
    }

    /**
     * Copy all files of the partition to the given destination directory.
     *
     * @param file The file with the hard disk image.
     * @param partition Partition.
     * @param destinationDir Directory to write extracted files to.
     */
    private void copy(Path file, Partition partition, Path destinationDir) throws IOException {
        BlockDevice device = new PartitionDisk(file, partition);
        FileSystem fileSystem = FileSystemFactory.create(device, true);
        try {
            copy(fileSystem.getRoot(), destinationDir);
        } finally {
            fileSystem.close();
            device.close();
        }
    }

    /**
     * Copy all files recursively.
     *
     * @param source Source directory.
     * @param destinationDir Destination directory.
     */
    private void copy(Iterable<FsDirectoryEntry> source, Path destinationDir) throws IOException {
        for (FsDirectoryEntry entry : source) {
            Path destination = destinationDir.resolve(entry.getName());
            long lastModified = entry.getLastModified();
            if (entry.isDirectory()) {
                copyDirectory(entry.getDirectory(), destination, lastModified);
            } else if (entry.isFile()) {
                copyFile(entry.getFile(), destination, lastModified);
            }
        }
    }

    /**
     * Copy source directory to destination directory.
     *
     * @param source Source directory.
     * @param destination Destination directory.
     * @param lastModified Last modified time.
     */
    private void copyDirectory(FsDirectory source, Path destination, long lastModified) throws IOException {
        out.println("Copying directory " + destination);
        Files.createDirectory(destination);
        Files.setLastModifiedTime(destination, FileTime.fromMillis(lastModified));
        copy(source, destination);
    }

    /**
     * Copy source file to destination file.
     *
     * @param source Source file.
     * @param destination Destination file.
     * @param lastModified Last modified time.
     */
    private void copyFile(FsFile source, Path destination, long lastModified) throws IOException {
        out.println("Copying file " + destination);
        try (var destinationChannel = FileChannel.open(destination, CREATE_NEW, WRITE)) {
            source.read(0, buffer);
            for (long offset = 0, length = source.getLength(); offset < length;) {
                offset += buffer.position();
                buffer.flip();
                destinationChannel.write(buffer);
                buffer.clear();
                buffer.limit((int) Math.min(buffer.capacity(), length - offset));
                source.read(offset, buffer);
            }
            Files.setLastModifiedTime(destination, FileTime.fromMillis(lastModified));
        }
    }
}
