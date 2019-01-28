package de.heiden.ataripart.commands;

import de.heiden.ataripart.image.*;
import de.heiden.ataripart.image.msdos.MsDosMbr;
import de.heiden.ataripart.image.msdos.MsDosPartition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.System.out;
import static java.nio.file.StandardOpenOption.READ;

/**
 * Extract all partitions.
 */
public class ExtractPartitions {
    /**
     * Hard disk image.
     */
    private ImageReader image;

    /**
     * Extract all partitions of the hard disk image to a directory.
     *
     * @param file The file with the hard disk image.
     * @param convertBootSectors Attempt to convert boot sectors to MS DOS format?.
     * @param destinationDir Directory to write extracted partitions to.
     */
    public void extract(Path file, boolean convertBootSectors, Path destinationDir) throws Exception {
        image = new ImageReader(file);

        List<RootSector> rootSectors = image.readRootSectors();

        out.println("Using hard disk image " + file.toAbsolutePath());
        out.println("Creating extraction directory " + destinationDir.toAbsolutePath());
        Files.createDirectories(destinationDir);
        if (convertBootSectors) {
            out.println("Converting boot sectors to MS DOS format.");
        }

        char partitionName = 'c';
        for (RootSector rootSector : rootSectors) {
            for (Partition partition : rootSector.getRealPartitions()) {
                String prefix = "Partition " + Character.toUpperCase(partitionName) + ": ";

                Path partitionFile = destinationDir.resolve(partitionName + ".img");
                out.println(prefix + "Creating image " + partitionFile.toAbsolutePath());
                extractPartition(partition, convertBootSectors, partitionFile);
                partitionName++;
            }
        }

        image.close();
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
     * @param destination Partition image (will be created).
     * @throws IOException In case of IO errors.
     */
    private void extractPartition(Partition partition, boolean msdos, Path destination) throws IOException {
        if (Files.isRegularFile(destination)) {
            throw new IllegalArgumentException("Destination file "+ destination.toAbsolutePath() + " exists.");
        }

        try (FileChannel destinationFile = FileChannel.open(destination, READ)) {
            if (msdos) {
                destinationFile.write(createMbr(partition));
            }
            long position = partition.getAbsoluteStart();
            long count = partition.getLength();
//            if (msdos) {
//                // Skip original boot sector.
//                position += 512;
//                count -= 512;
//                // Write MS DOS boot sector from parsed partition data.
//                destinationChannel.write(msdosBootSector(partition));
//            }
            image.transferTo(position, count, destinationFile);
        }
    }

    /**
     * Create MS DOS MBR.
     */
    private ByteBuffer createMbr(Partition partition) {
        MsDosPartition partitionEntry = new MsDosPartition(
                false,
                FileSystem.FAT16.getType(),
                1,
                partition.getLength() / 512);
        MsDosMbr mbr = new MsDosMbr(partitionEntry);
        return mbr.createMbr();
    }

    /**
     * Convert boot sector to MS DOS format.
     */
    private ByteBuffer msdosBootSector(Partition partition) throws IOException {
        ByteBuffer bootSector = ByteBuffer.allocateDirect(512);
        bootSector.order(ByteOrder.LITTLE_ENDIAN);
        image.read(partition.getAbsoluteStart(), bootSector);

        // Standard OEM name.
        StringUtils.setString(bootSector, 3, "MSDOS5.0");
        // Drive number.
        IntUtils.setInt8(bootSector, 0x0024, 0x80);
        // Magic bytes.
        IntUtils.setInt8(bootSector, 0x01FE, 0x55);
        IntUtils.setInt8(bootSector, 0x01FF, 0xAA);

//        System.out.println();
//        System.out.println(IntUtils.hexDump(bootSector, 512));
//        System.out.println(BootSector.parse(bootSector));

        return bootSector;
    }
}
