/*******************************************************************************
 * Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
 *
 * Licensed under the Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0
 ******************************************************************************/

package com.adobe.aem.importer.impl;

import com.adobe.aem.importer.DocImporter;
import com.adobe.aem.importer.xml.FilterXmlBuilder;
import com.adobe.aem.importer.xml.RejectingEntityResolver;
import com.day.cq.commons.jcr.JcrUtil;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.felix.scr.annotations.*;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.vault.fs.api.ImportMode;
import org.apache.jackrabbit.vault.fs.config.ConfigurationException;
import org.apache.jackrabbit.vault.fs.io.AccessControlHandling;
import org.apache.jackrabbit.vault.fs.io.Importer;
import org.apache.jackrabbit.vault.fs.io.JcrArchive;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

@Component
@org.apache.felix.scr.annotations.Properties({
    @Property(name = Constants.SERVICE_DESCRIPTION, value = "AEM Content Importer"),
    @Property(name = Constants.SERVICE_VENDOR, value = "Adobe")})
@Service(value = DocImporter.class)
public class DocImporterImpl implements DocImporter {
    private static final Logger log = LoggerFactory.getLogger(DocImporterImpl.class);
    private Session session;

    private String xsltFilePath;
    private String masterFileName;
    private String graphicsFolderName;
    private String targetPath;

    private Node importRootNode;
    private Node sourceFolderNode;
    private Properties properties;

    @Reference
    private SlingRepository slingRepository;

    @Activate
    protected final void activate(final Map<String, String> properties) throws Exception {}

    @Deactivate
    protected final void deactivate(final Map<String, String> properties) {}

    private boolean initImport(String importRootPath){
        log.info("importRootPath:" + importRootPath);
        try {
            this.session = slingRepository.loginAdministrative(null);
            log.info("this.session:" + this.session);

            if (!this.session.nodeExists(importRootPath)){
                log.info("importRootPath " + importRootPath + " not found!");
                return false;
            }
            this.importRootNode = this.session.getNode(importRootPath);
            log.info("this.importRootNode: " + this.importRootNode);

            if (!this.importRootNode.hasNode(DocImporter.CONFIG_FILE_NAME))
            {
                log.info("config file " + DocImporter.CONFIG_FILE_NAME + " not found!");
                return false;
            }

            this.properties = new Properties();
            this.properties.loadFromXML(JcrUtils.readFile(this.importRootNode.getNode(DocImporter.CONFIG_FILE_NAME)));
            log.info("this.properties: " + Arrays.deepToString(this.properties.values().toArray()));

            String sourceFolder = properties.getProperty(DocImporter.CONFIG_PARAM_SOURCE_FOLDER, DocImporter.DEFAULT_SOURCE_FOLDER);
            log.info("sourceFolder: " + sourceFolder);

            if (!this.importRootNode.hasNode(sourceFolder)) {
                log.info("sourceFolder " + sourceFolder + " not found!");
                return false;
            }
            this.sourceFolderNode = importRootNode.getNode(sourceFolder);
            log.info("this.sourceFolderNode: " + this.sourceFolderNode);

            this.masterFileName = properties.getProperty(DocImporter.CONFIG_PARAM_MASTER_FILE, DocImporter.DEFAULT_MASTER_FILE);
            log.info("this.masterFileName: " + this.masterFileName);

            if (!this.sourceFolderNode.hasNode(this.masterFileName)){
                log.info("masterFileName " + this.masterFileName + " not found!");
                return false;
            }

            this.graphicsFolderName = properties.getProperty(DocImporter.CONFIG_PARAM_GRAPHICS_FOLDER, DocImporter.DEFAULT_GRAPHICS_FOLDER);
            log.info("this.graphicsFolderName: " + this.graphicsFolderName);

            this.targetPath = properties.getProperty(DocImporter.CONFIG_PARAM_TARGET_PATH, DocImporter.DEFAULT_TARGET_PATH);
            log.info("this.targetPath: " + this.targetPath);

            String sourceFormat = this.properties.getProperty(DocImporter.CONFIG_PARAM_SOURCE_FORMAT, DocImporter.DEFAULT_SOURCE_FORMAT);
            log.info("sourceFormat: " + sourceFormat);

            if (sourceFormat.equalsIgnoreCase(DocImporter.SOURCE_FORMAT_DOCBOOK)) {
                this.xsltFilePath = DocImporter.DOCBOOK_XSLT_PATH;
            } else {
                this.xsltFilePath = DocImporter.DITA_XSLT_PATH;
            }
            log.info("this.xsltFilePath: " + this.xsltFilePath);

        } catch(RepositoryException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
        return true;
    }

    public void doImport(String importRootPath) {
        try {
            if (!initImport(importRootPath)){
                log.info("initImport failed!");
                return;
            }

            Node xsltNode = this.session.getNode(xsltFilePath);
            log.info("xsltNode: " + xsltNode);

            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            log.info("xmlReader: " + xmlReader);

            xmlReader.setEntityResolver(new RejectingEntityResolver());
            URIResolver uriResolver = new DocImporterURIResolver(xsltNode, this.sourceFolderNode, xmlReader);
            log.info("uriResolver: " + uriResolver);

            TransformerFactory transformerFactory = new TransformerFactoryImpl();
            log.info("transformerFactory: " + transformerFactory);

            transformerFactory.setURIResolver(uriResolver);
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(JcrUtils.readFile(xsltNode)));
            log.info("transformer: " + transformer);

            for (Entry<Object, Object> entry : properties.entrySet()) {
                transformer.setParameter(entry.getKey().toString(), entry.getValue());
                log.info("transformer.setParameter: " + entry.getKey().toString() + " = " + entry.getValue());
            }
            transformer.setParameter("xsltFilePath", this.xsltFilePath);
            log.info("transformer.setParameter: xsltFilePath = " + this.xsltFilePath);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new SAXSource(xmlReader, new InputSource(JcrUtils.readFile(this.sourceFolderNode.getNode(masterFileName)))), new StreamResult(output));
            InputStream result = new ByteArrayInputStream(output.toByteArray());
            log.info("result: " + result);

            if (this.session.itemExists(DocImporter.CONTENT_PACKAGE_PATH)){
                this.session.removeItem(DocImporter.CONTENT_PACKAGE_PATH);
                this.session.save();
                log.info("old package removed");
            }
            Node contentPackageNode = JcrUtils.getOrCreateByPath(DocImporter.CONTENT_PACKAGE_PATH, "nt:folder", "nt:folder", this.session, true);
            this.session.getWorkspace().copy(DocImporter.CONTENT_PACKAGE_TEMPLATE_PATH + "/META-INF", contentPackageNode.getPath() + "/META-INF");
            log.info("new package created");

            Node vaultNode = contentPackageNode.getNode("META-INF/vault");
            Node contentXMLNode = JcrUtil.createPath(contentPackageNode.getPath() + "/jcr_root" + targetPath, "nt:folder", "nt:folder", this.session, true);
            JcrUtils.putFile(contentXMLNode, ".content.xml", "application/xml", result);
            log.info("content.xml written");

            if (this.graphicsFolderName != null && this.sourceFolderNode.hasNode(this.graphicsFolderName)) {
                JcrUtil.copy(this.sourceFolderNode.getNode(graphicsFolderName), contentXMLNode, this.graphicsFolderName);
            }
            JcrUtils.putFile(vaultNode, "filter.xml", "application/xml", FilterXmlBuilder.fromRoot(this.targetPath + "/").toStream(this.graphicsFolderName));
            log.info("filter.xml written");

            JcrArchive archive = new JcrArchive(contentPackageNode, "/");
            archive.open(true);
            Importer importer = new Importer();
            importer.getOptions().setImportMode(ImportMode.MERGE);
            importer.getOptions().setAccessControlHandling(AccessControlHandling.MERGE);
            importer.run(archive, contentPackageNode.getSession().getNode("/"));
            log.info("content.xml imported");

            //contentPackageNode.remove();
            this.session.save();
            log.info("session saved.");

        } catch(RepositoryException e) {
            log.error(e.toString());
        } catch (TransformerException e) {
            log.error(e.toString());
        } catch (SAXException e){
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        } catch (ConfigurationException e){
            log.error(e.toString());
        }
    }

    private class DocImporterURIResolver implements URIResolver {
        private Node xsltNode;
        private Node srcNode;
        private XMLReader xmlReader;

        public DocImporterURIResolver(Node xsltNode, Node srcNode, XMLReader xmlReader) {
            this.xsltNode = xsltNode;
            this.srcNode = srcNode;
            this.xmlReader = xmlReader;
        }

        @Override
        public Source resolve(String href, String base) throws TransformerException {
            try {
                final Node node = (href.endsWith("xsl") ? this.xsltNode.getParent().getNode(href) : this.srcNode.getNode(href));
                DocImporterImpl.log.debug("Resolving resource {}", node.getPath());
                return new SAXSource(this.xmlReader, new InputSource(JcrUtils.readFile(node)));
            } catch (RepositoryException e) {
                throw new TransformerException("Cannot resolve " + href + " in either [parent of " + this.xsltNode + " or " + this.srcNode + "]");
            }
        }
    }
}
