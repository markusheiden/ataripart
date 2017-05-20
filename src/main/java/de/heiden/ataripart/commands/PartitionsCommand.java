package de.heiden.ataripart.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import de.heiden.ataripart.AtariPart;
import de.heiden.ataripart.IntUtils;
import de.heiden.ataripart.Partition;
import de.heiden.ataripart.RootSector;

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
@Parameters(commandDescription = "Extract all partitions to a directory.")
public class PartitionsCommand {
    @Parameter(names = {"--convert"}, description = "Convert boot sectors to MS DOS format")
    public Boolean convertBootSectors = false;

    @Parameter(description = "[Hard disk image] [Directory to copy partition contents to]")
    public List<File> images;

    /**
     * Extract all partitions of the hard disk image to a directory.
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
        boolean msdos = convertBootSectors != null && convertBootSectors;
        if (msdos) {
            out.println("Converting boot sectors to MS DOS format.");
        }

        char partitionName = 'c';
        for (RootSector rootSector : rootSectors) {
            for (Partition partition : rootSector.getRealPartitions()) {
                String prefix = "Partition " + Character.toUpperCase(partitionName) + ": ";

                File partitionFile = new File(destinationDir, partitionName + ".img");
                out.println(prefix + "Creating image " + partitionFile.getAbsolutePath());
                extractPartition(partition, msdos, atariPart.getFile(), partitionFile);
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
     * @param hdFile Hard disk image.
     * @param partitionFile Partition image (will be created).
     * @throws IOException In case of IO errors.
     */
    private void extractPartition(Partition partition, boolean msdos, File hdFile, File partitionFile) throws IOException {
        try (RandomAccessFile fromFile = new RandomAccessFile(hdFile, "r"); RandomAccessFile toFile = new RandomAccessFile(partitionFile, "rw")) {
            try (FileChannel hdChannel = fromFile.getChannel(); FileChannel partitionChannel = toFile.getChannel()) {
                long position = partition.getAbsoluteStart();
                long count = partition.getLength();
                if (msdos) {
                    // Skip original boot sector.
                    position += 512;
                    count -= 512;
                    // Write MS DOS boot sector from parsed partition data.
                    partitionChannel.write(msdosBootSector(partition));
                }
                hdChannel.transferTo(position, count, partitionChannel);
            }
        }
    }

    /**
     * Write MS DOS boot sector (instead of original Atari one).
     */
    private ByteBuffer msdosBootSector(Partition partition) {
        ByteBuffer bootSector = ByteBuffer.allocate(512);
        bootSector.order(ByteOrder.LITTLE_ENDIAN);

        IntUtils.setInt32(bootSector, 0x01B8, partition.getBootSector().getSerial());
        // 0x01BE: 0x80 boot, 0x00 not.
        IntUtils.setInt8(bootSector, 0x01BE, 0x00);
        // 0x01BF: CHS first sector: 0/0/0.
        // 0x01C2: Partition type: FAT16 with LBA.
        IntUtils.setInt8(bootSector, 0x01C2, 0x0E);
        // 0x01C3: CHS last sector: 0/0/0.
        // 0x01C6: LBA first sector.
        IntUtils.setInt32(bootSector, 0x01C6, partition.getAbsoluteStart() / 512);
        // 0x01CA: LBA num sectors.
        IntUtils.setInt32(bootSector, 0x01CA, partition.getLength() / 512);
        IntUtils.setInt16(bootSector, 0x01FE, 0x55AA);
        return bootSector;
    }
}
