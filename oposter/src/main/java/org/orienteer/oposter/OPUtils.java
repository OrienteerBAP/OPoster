package org.orienteer.oposter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.experimental.UtilityClass;
import net.coobird.thumbnailator.Thumbnails;

@UtilityClass
public class OPUtils {
	private static final int MAX_IMAGE_SIZE = 5_000_000;


    public byte[] resizeImage(byte[] image) throws IOException {
        byte [] data = image;

        while (data.length > MAX_IMAGE_SIZE) {
            ByteArrayOutputStream thumbnailOS = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(data))
                    .scale(0.8)
                    .toOutputStream(thumbnailOS);
            data = thumbnailOS.toByteArray();
        }

        return data;
    }
}
