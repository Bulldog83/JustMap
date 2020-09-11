package ru.bulldog.justmap.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import ru.bulldog.justmap.JustMap;

public class CompressionUtil {
	public static String decompress(String data) {
		byte[] dataArray = Base64.decodeBase64(data);
		try (GZIPInputStream inZip = new GZIPInputStream(new ByteArrayInputStream(dataArray));) {
			return IOUtils.toString(inZip, StandardCharsets.UTF_8);
		} catch (Exception ex) {
			JustMap.LOGGER.warning("Can't decompress data!", data, ex);
			return null;
		}
	}
	
	public static String compress(byte[] data) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			GZIPOutputStream outZip = new GZIPOutputStream(outStream);
			outZip.write(data);
			IOUtils.closeQuietly(outZip);
			byte[] bytes = outStream.toByteArray();
			return Base64.encodeBase64String(bytes);
		} catch (Exception ex) {
			JustMap.LOGGER.warning("Can't compress data!", data, ex);
			return null;
		}
	}
}
