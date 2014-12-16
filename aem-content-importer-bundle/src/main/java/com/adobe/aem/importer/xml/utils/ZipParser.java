package com.adobe.aem.importer.xml.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.constant.Constant;
import com.adobe.aem.importer.xml.Config;

public class ZipParser {

	private static final Logger log = LoggerFactory.getLogger(ZipParser.class);

	private ZipInputStream source;
	private SlingHttpServletRequest request;

	private String src = "";
	private String transformer = "";
	private String target = "";
	private String masterFile = "";

	public ZipParser(InputStream source, SlingHttpServletRequest request) {
		this.source = new ZipInputStream(source);
		this.request = request;
	}

	public void unzipAndUploadJCR() throws Exception {
		ZipEntry entry;
		entry = source.getNextEntry();
		ByteArrayInputStream configFile = null;
		// First file of zip must to be the config file
		if (entry != null) {
			configFile = extractConfigFile(source);
			entry = source.getNextEntry();

		}

		Resource resources = request.getResourceResolver().getResource(src);

		Node srcNode = resources.adaptTo(Node.class);

		Session session = srcNode.getSession();

		while (entry != null) {
			JcrUtils.putFile(srcNode, entry.getName(), "text/xml",
					extractFile(source));
			entry = source.getNextEntry();
		}

		resources = request.getResourceResolver().getResource(
				Constant.DEFAULT_FOLDER_SRC);
		Node workflowNode = resources.adaptTo(Node.class);

		JcrUtils.putFile(workflowNode, Constant.CONFIG_PARAMS_NAME, "text/xml",
				configFile);

		session.save();

		source.close();
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
		byte[] bytesIn = new byte[Constant.BUFFER_SIZE];
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
	 * @return
	 * @throws IOException
	 */
	private ByteArrayInputStream extractConfigFile(ZipInputStream configFile)
			throws IOException {
		// get the factory

		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baout);
		byte[] bytesIn = new byte[Constant.BUFFER_SIZE];
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

		src = p.getProperty(Constant.SRC);
		transformer = p.getProperty(Constant.TRANSFORMER);
		masterFile = p.getProperty(Constant.MASTER_FILE);
		target = p.getProperty(Constant.TARGET);

		baout = new ByteArrayOutputStream();
		p.storeToXML(baout, null, Constant.ENCODING);

		bytesConfigFile = new ByteArrayInputStream(baout.toByteArray());
		return bytesConfigFile;

	}

	public InputStream getSource() {
		return source;
	}

	public void setSource(ZipInputStream source) {
		this.source = source;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getTransformer() {
		return transformer;
	}

	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getMasterFile() {
		return masterFile;
	}

	public void setMasterFile(String masterFile) {
		this.masterFile = masterFile;
	}

}
