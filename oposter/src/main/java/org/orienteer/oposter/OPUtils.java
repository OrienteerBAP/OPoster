package org.orienteer.oposter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.wicket.WicketRuntimeException;
import org.orienteer.vuecket.VueSettings;
import org.orienteer.vuecket.util.VuecketUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.experimental.UtilityClass;
import net.coobird.thumbnailator.Thumbnails;

/**
 * Collection of useful functions
 */
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
    
    public static JsonNode toJsonNode(String json) {
    	return VuecketUtils.toJsonNode(json);
	}
    
    public static ObjectMapper getObjectMapper() {
    	return VueSettings.get().getObjectMapper();
    }
    
    public static String firstLine(String data) {
    	if(data==null) return null;
    	int firstBreak = data.indexOf('\n');
    	return firstBreak<0?data:data.substring(0, firstBreak).trim();
    }
}
