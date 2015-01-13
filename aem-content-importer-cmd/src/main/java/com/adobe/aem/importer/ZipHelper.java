package com.adobe.aem.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHelper {
	public void zipDir(String dirName, String nameZipFile) throws IOException {
		ZipOutputStream zip = null;
		FileOutputStream fW = null;
		fW = new FileOutputStream(nameZipFile);
		zip = new ZipOutputStream(fW);
		addFolderToZip("", dirName, zip);
		zip.close();
		fW.close();
	}

	private void addFolderToZip(String path, String srcFolder,
			ZipOutputStream zip) throws IOException {
		File folder = new File(srcFolder);
		if (folder.list().length == 0) {
			addFileToZip(path, srcFolder, zip, true);
		} else {
			for (String fileName : folder.list()) {
				if (path.equals("")) {
					addFileToZip("", srcFolder + "/" + fileName,
							zip, false);
				} else {
					addFileToZip(path, srcFolder + "/"
							+ fileName, zip, false);
				}
			}
		}
	}

	private void addFileToZip(String path, String srcFile, ZipOutputStream zip,
			boolean flag) throws IOException {
		File resource = new File(srcFile);
		if (flag) {
			zip.putNextEntry(new ZipEntry(path + "/" + resource.getName() + "/"));
		} else {
			if (resource.isDirectory()) {
				if (path.equals("")) {
					addFolderToZip(resource.getName(), srcFile, zip);
				} else {
					addFolderToZip(path + "/" + resource.getName(), srcFile, zip);
				}
				
			} else {
				byte[] buf = new byte[1024];
				int len;
				FileInputStream in = new FileInputStream(srcFile);
				
				if (path.equals("")) {
					zip.putNextEntry(new ZipEntry(resource.getName()));
				} else {
					zip.putNextEntry(new ZipEntry(path + "/" + resource.getName()));
				}
				
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			}
		}
	}
}
