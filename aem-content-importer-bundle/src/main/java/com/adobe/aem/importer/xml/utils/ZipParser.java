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
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

import com.adobe.aem.importer.DITATransformerHelper;
import com.day.cq.commons.jcr.JcrUtil;

public class ZipParser {
	
	private static final int BUFFER_SIZE = 4096;

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

	/**
	 * unzipAndUploadJCR
	 * @param encoding
	 * @throws Exception
	 */
	public void unzipAndUploadJCR(String encoding) throws Exception {
		ZipEntry entry;
		entry = source.getNextEntry();
		ByteArrayInputStream configFile = null;
		// First file of zip must to be the config file
		if (entry != null) {
			configFile = extractConfigFile(source,encoding);
			entry = source.getNextEntry();

		}

		
		Resource resources = request.getResourceResolver().getResource(src);

		Node srcNode = null;
		try {
			srcNode = resources.adaptTo(Node.class);
		} catch (Exception e) {
			Session jcrSession = request.getResourceResolver().adaptTo(Session.class);
    		srcNode = JcrUtil.createPath(src, "nt:folder", jcrSession);
    		jcrSession.save();
		}

		Session session = srcNode.getSession();

		while (entry != null) {
			JcrUtils.putFile(srcNode, entry.getName(), "text/xml",
					extractFile(source));
			entry = source.getNextEntry();
		}

		resources = request.getResourceResolver().getResource(DITATransformerHelper.DEFAULT_CONFIG_PARAM_SRC);
		Node workflowNode = resources.adaptTo(Node.class);
		JcrUtils.putFile(workflowNode, DITATransformerHelper.CONFIG_FILENAME, "text/xml",
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

		src = p.getProperty(DITATransformerHelper.CONFIG_PARAM_SRC);
		transformer = p.getProperty(DITATransformerHelper.CONFIG_PARAM_TRANSFORMER);
		masterFile = p.getProperty(DITATransformerHelper.CONFIG_PARAM_MASTER_FILE);
		target = p.getProperty(DITATransformerHelper.CONFIG_PARAM_TARGET);

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
