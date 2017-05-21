package de.heiden.ataripart.commands;

import de.heiden.ataripart.image.IntUtils;
import de.heiden.ataripart.image.Partition;
import de.heiden.ataripart.image.RootSector;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;

import static java.lang.System.out;

/**
 * Extract all partitions.
 */
public class ExtractPartitions extends AbstractCommand {
    /**
     * Extract all partitions of the hard disk image to a directory.
     */
    public void extract(File file, boolean convertBootSectors, File destinationDir) throws Exception {
        init(file);

        List<RootSector> rootSectors = readRootSectors();

        out.println("Using hard disk image " + file.getCanonicalPath());
        out.println("Creating extraction directory " + destinationDir.getCanonicalPath());
        destinationDir.mkdirs();
        if (convertBootSectors) {
            out.println("Converting boot sectors to MS DOS format.");
        }

        char partitionName = 'c';
        for (RootSector rootSector : rootSectors) {
            for (Partition partition : rootSector.getRealPartitions()) {
                String prefix = "Partition " + Character.toUpperCase(partitionName) + ": ";

                File partitionFile = new File(destinationDir, partitionName + ".img");
                out.println(prefix + "Creating image " + partitionFile.getCanonicalPath());
                extractPartition(partition, convertBootSectors, partitionFile);
                partitionName++;
            }
        }
    }

    /**
     * Copy partition from hard disk image to partition image.
     * <br/>
     * On the command line you can use dd to achieve the same:
     * <pre>
     * dd if=hdFile bs=512 skip=partition.getAbsoluteStart()/512 count=partition.getLength()/512 of=partitionFile
     * </pre>
     *
     * @param partition Partition definition.
     * @param msdos Convert boot sector to MS DOS format?.
     * @param partitionFile Partition image (will be created).
     * @throws IOException In case of IO errors.
     */
    private void extractPartition(Partition partition, boolean msdos, File partitionFile) throws IOException {
        try (RandomAccessFile toFile = new RandomAccessFile(partitionFile, "rw")) {
            try (FileChannel imageChannel = image.getChannel(); FileChannel partitionChannel = toFile.getChannel()) {
                long position = partition.getAbsoluteStart();
                long count = partition.getLength();
                if (msdos) {
                    // Skip original boot sector.
                    position += 512;
                    count -= 512;
                    // Write MS DOS boot sector from parsed partition data.
                    partitionChannel.write(msdosBootSector(partition, imageChannel));
                }
                imageChannel.transferTo(position, count, partitionChannel);
            }
        }
    }

    /**
     * Convert boot sector to MS DOS format.
     */
    private ByteBuffer msdosBootSector(Partition partition, FileChannel disk) throws IOException {
        ByteBuffer bootSector = ByteBuffer.allocateDirect(512);
        bootSector.order(ByteOrder.LITTLE_ENDIAN);
        disk.position(partition.getAbsoluteStart()).read(bootSector);
        bootSector.position(0);

        IntUtils.setInt8(bootSector, 0x01FE, 0x55);
        IntUtils.setInt8(bootSector, 0x01FF, 0xAA);
        return bootSector;
    }
}