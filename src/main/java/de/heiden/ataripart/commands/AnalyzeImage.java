package de.heiden.ataripart.commands;

import de.heiden.ataripart.image.ImageReader;
import de.heiden.ataripart.image.Partition;
import de.heiden.ataripart.image.RootSector;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static java.lang.System.out;

/**
 * The analyze command searches a whole disk image for root sectors.
 */
public class AnalyzeImage {
    /**
     * Hard disk image.
     */
    private ImageReader image;

    /**
     * Scan disk image for root sectors.
     *
     * Does NOT evaluate partition information to follow XGM partitions.
     *
     * @param file The file with the hard disk image.
     */
    public void analyze(File file) throws IOException {
        image = new ImageReader(file);

        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024 * 1024);

        long diskOffset = 0;
        for (int num; (num = image.read(diskOffset, buffer)) >= 0;) {
            for (int bufferOffset = 0; bufferOffset + 512 <= num; bufferOffset += 512, diskOffset += 512) {
                buffer.position(bufferOffset);
                RootSector rootSector = RootSector.parse(diskOffset, diskOffset, buffer);
                if (rootSector.hasValidPartitions()) {
                    out.print(diskOffset);
                    out.print(": Possible ");
                    out.println(rootSector.toString());

                    for (Partition partition : rootSector.getPartitions()) {
                        out.println(partition.toString());
                    }
                }
            }
            buffer.clear();
        }

        image.close();
    }
}
