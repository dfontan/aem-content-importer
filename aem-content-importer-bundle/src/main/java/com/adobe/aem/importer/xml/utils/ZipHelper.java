/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/
package com.adobe.aem.importer.xml.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.XMLTransformerHelper;
import com.day.cq.commons.jcr.JcrUtil;

public class ZipHelper {
	
	private static final int BUFFER_SIZE = 4096;
	
	private static Logger log = LoggerFactory
			.getLogger(ZipHelper.class);
	
	/*
	 * UNZIP A FOLDER METHODS 
	 */
	
	/**
	 * Unzip file and upload content to the repository
	 * @param encoding
	 * @throws Exception
	 */
	public static String unzipAndUploadJCR(String encoding, SlingHttpServletRequest request, InputStream zipFile) throws Exception {
		log.debug("Unzipping and uploading files to repository");
		
		ZipInputStream source = new ZipInputStream(zipFile);
		
		String nameConfigFile = "";
		ZipEntry entry;
		entry = source.getNextEntry();
		ByteArrayInputStream configFile = null;
		StringBuilder src = new StringBuilder();
		// First file of zip must to be the config file
		if (entry != null) {
			configFile = extractConfigFile(src, source,encoding);
			entry = source.getNextEntry();

		}

		
		Resource resources = request.getResourceResolver().getResource(src.toString());
		Session jcrSession = request.getResourceResolver().adaptTo(Session.class);
		Node srcNode = null;
		try {
			srcNode = resources.adaptTo(Node.class);
		} catch (Exception e) {
    		srcNode = JcrUtil.createPath(src.toString(), "nt:folder", jcrSession);
    		jcrSession.save();
		}

		Session session = srcNode.getSession();
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		while (entry != null) {
			
			String name[] = entry.getName().split("/");
			
			if (name.length > 1) {
				Node n = srcNode;
				for (int i = 0; i <= (name.length - 1); i++) {
					if (i == (name.length - 1)) {
						String mimeType = mimeTypesMap.getContentType(entry.getName());
						JcrUtils.putFile(n, name[i], mimeType,
								extractFile(source));
					} else {
						String path = n.getPath() + "/" + name[i];
						if (!jcrSession.itemExists(path)) {
							n = n.addNode(name[i], "nt:folder");
						} else {
							n = jcrSession.getNode(path);
						}
						jcrSession.save();
					}
				}
			} else {
				if(!entry.getName().endsWith("/")) {
					String mimeType = mimeTypesMap.getContentType(entry.getName());
					JcrUtils.putFile(srcNode, entry.getName(), mimeType,
							extractFile(source));
				} else {
					String path = entry.getName();
					JcrUtil.createPath(path, "nt:folder", jcrSession);
					jcrSession.save();
				}
				
			}
			
			entry = source.getNextEntry();
		}
		
		Node workflowNode = JcrUtil.createPath(XMLTransformerHelper.DEFAULT_CONFIG_PARAM_SRC, "nt:folder", request.getResourceResolver().adaptTo(Session.class));
		
		nameConfigFile = System.currentTimeMillis()+".xml";
		JcrUtils.putFile(workflowNode, nameConfigFile, "text/xml",
				configFile);

		session.save();

		source.close();
		
		return workflowNode.getPath() + "/" +nameConfigFile;
	}
	
	
	
	/**
	 * extractFile
	 * 
	 * @param zipIn
	 * @return
	 * @throws IOException
	 */
	private static ByteArrayInputStream extractFile(ZipInputStream zipIn)
			throws IOException {
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baout);
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
		zipIn.closeEntry();

		return new ByteArrayInputStream(baout.toByteArray());
	}
	
	
	/**
	 * extractConfigFile
	 * 
	 * @param configFile
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	private static ByteArrayInputStream extractConfigFile(StringBuilder src, ZipInputStream configFile, String encoding)
			throws IOException {
		// get the factory

		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baout);
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = configFile.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
		configFile.closeEntry();

		ByteArrayInputStream bytesConfigFile = new ByteArrayInputStream(
				baout.toByteArray());

		Properties p = new Properties();

		p.loadFromXML(bytesConfigFile);

		src.append(p.getProperty(XMLTransformerHelper.CONFIG_PARAM_SRC));

		baout = new ByteArrayOutputStream();
		p.storeToXML(baout, null, encoding);

		bytesConfigFile = new ByteArrayInputStream(baout.toByteArray());
		return bytesConfigFile;

	}
	
	
	/*
	 * ZIP A FOLDER METHODS 
	 */
	
	/**
	 * Compress a folder in a zip file
	 * @param dirName
	 * @param nameZipFile
	 * @throws IOException
	 */
	public static void zipDir(String dirName, String nameZipFile) throws IOException {
		ZipOutputStream zip = null;
		FileOutputStream fW = null;
		fW = new FileOutputStream(nameZipFile);
		zip = new ZipOutputStream(fW);
		addFolderToZip("", dirName, zip);
		zip.close();
		fW.close();
	}

	/**
	 * Add folder to zip
	 * @param path
	 * @param srcFolder
	 * @param zip
	 * @throws IOException
	 */
	private static void addFolderToZip(String path, String srcFolder,
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

	
	/**
	 * Add file to a zip
	 * @param path
	 * @param srcFile
	 * @param zip
	 * @param flag
	 * @throws IOException
	 */
	private static void addFileToZip(String path, String srcFile, ZipOutputStream zip,
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
