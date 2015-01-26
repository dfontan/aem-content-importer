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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.servlet.UploadContentServlet;
import com.day.cq.commons.jcr.JcrUtil;

public class ZipParser {
	
	private static final int BUFFER_SIZE = 4096;

	private ZipInputStream source;
	private SlingHttpServletRequest request;

	private String src = "";
	
	private static Logger log = LoggerFactory
			.getLogger(ZipParser.class);
	
	public ZipParser(InputStream source, SlingHttpServletRequest request) {
		this.source = new ZipInputStream(source);
		this.request = request;
	}

	/**
	 * unzipAndUploadJCR
	 * @param encoding
	 * @throws Exception
	 */
	public String unzipAndUploadJCR(String encoding) throws Exception {
		log.debug("Unzipping and uploading files to repository");
		String nameConfigFile = "";
		ZipEntry entry;
		entry = source.getNextEntry();
		ByteArrayInputStream configFile = null;
		// First file of zip must to be the config file
		if (entry != null) {
			configFile = extractConfigFile(source,encoding);
			entry = source.getNextEntry();

		}

		
		Resource resources = request.getResourceResolver().getResource(src);
		Session jcrSession = request.getResourceResolver().adaptTo(Session.class);
		Node srcNode = null;
		try {
			srcNode = resources.adaptTo(Node.class);
		} catch (Exception e) {
    		srcNode = JcrUtil.createPath(src, "nt:folder", jcrSession);
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
	private ByteArrayInputStream extractFile(ZipInputStream zipIn)
			throws IOException {
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baout);
		byte[] bytesIn = new byte[ZipParser.BUFFER_SIZE];
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
	private ByteArrayInputStream extractConfigFile(ZipInputStream configFile, String encoding)
			throws IOException {
		// get the factory

		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baout);
		byte[] bytesIn = new byte[ZipParser.BUFFER_SIZE];
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

		src = p.getProperty(XMLTransformerHelper.CONFIG_PARAM_SRC);

		baout = new ByteArrayOutputStream();
		p.storeToXML(baout, null, encoding);

		bytesConfigFile = new ByteArrayInputStream(baout.toByteArray());
		return bytesConfigFile;

	}

	public InputStream getSource() {
		return source;
	}

	public void setSource(ZipInputStream source) {
		this.source = source;
	}


}
