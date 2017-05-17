package de.heiden.ataripart.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import de.heiden.ataripart.AtariPart;
import de.heiden.ataripart.Partition;
import de.heiden.ataripart.RootSector;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;

import static java.lang.System.out;

/**
 * Extract all partitions.
 */
@Parameters(commandDescription = "Extract all partitions to a directory.")
public class PartitionsCommand {
    @Parameter(description = "[Hard disk image] [Directory to copy partition contents to]")
    public List<File> images;

    /**
     * Extracts all partitions from the disk image.
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

                File partitionFile = new File(destinationDir, partitionName + ".img");
                out.println(prefix + "Creating image " + partitionFile.getAbsolutePath());
                extractPartition(partition, atariPart.getFile(), partitionFile);
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
     * @param hdFile Hard disk image.
     * @param partitionFile Partition image (will be created).
     * @throws IOException In case of IO errors.
     */
    private void extractPartition(Partition partition, File hdFile, File partitionFile) throws IOException {
        try (RandomAccessFile fromFile = new RandomAccessFile(hdFile, "r"); RandomAccessFile toFile = new RandomAccessFile(partitionFile, "rw")) {
            try (FileChannel hdChannel = fromFile.getChannel(); FileChannel partitionChannel = toFile.getChannel()) {
                hdChannel.transferTo(partition.getAbsoluteStart(), partition.getLength(), partitionChannel);
            }
        }
    }
}
