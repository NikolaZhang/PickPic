package com.nikolazhang.util;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileDownload {
	
	
	public static void saveImageToDisk(InputStream inputStream, String filepath) {
		byte[] data = new byte[1024];
		int len = 0;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(filepath);
			while ((len = inputStream.read(data)) != -1) {
				fileOutputStream.write(data, 0, len);
 
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
 
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
	}
	
	public static void saveImageToDisk(String url, String filepath) {
		try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
		FileOutputStream fileOutputStream = new FileOutputStream(filepath)) {
		byte dataBuffer[] = new byte[1024];
		int bytesRead;
		while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
		    fileOutputStream.write(dataBuffer, 0, bytesRead);
		}
		} catch (IOException e) {
		    // handle exception
		}
	}
}
