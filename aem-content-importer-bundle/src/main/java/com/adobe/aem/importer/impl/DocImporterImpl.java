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

    @Reference
    private SlingRepository slingRepository;

    private String xsltFile;
    private String masterFile;
    private String graphicsFolder;
    private String targetPath;

    @Activate
    protected final void activate(final Map<String, String> properties) throws Exception {

        // Default format is DITA therefore the default xsltFile is dita-to-content.xsl
        this.xsltFile = DocImporter.DEFAULT_XSLT_PATH;

        // Default masterFile
        this.masterFile = DocImporter.DEFAULT_MASTER_FILE;

        // Default graphicsFolders
        this.graphicsFolder = DocImporter.DEFAULT_GRAPHICS_FOLDER;

        // Default targetPath
        this.targetPath = DocImporter.DEFAULT_TARGET_PATH;
    }

    @Deactivate
    protected final void deactivate(final Map<String, String> properties) {
        // Remove method is not used
    }

    public void doImport() {
        try {
            log.info("Starting doImport()...");

            // Get a session
            Session session = slingRepository.loginAdministrative(null);
            log.info("Session: " + session.toString());

            // Get the source doc folder node
            if(!session.nodeExists(DocImporter.SOURCE_DOC_PATH)) {
                log.info("No source folder");
                return;
            }
            Node sourceDocPathNode = session.getNode(DocImporter.SOURCE_DOC_PATH);

            // Get the configuration properties
            String configJcrContentPath = DocImporter.CONFIG_FILE_NAME + "/jcr:content";
            if(!sourceDocPathNode.hasNode(configJcrContentPath)) {
                log.info("No config file");
                return;
            }
            Node configJcrContentNode = sourceDocPathNode.getNode(configJcrContentPath);
            log.info("configNode: " + configJcrContentNode.getPath());
            Properties properties = new Properties();
            properties.loadFromXML(JcrUtils.readFile(configJcrContentNode));

            // Set the master file
            this.masterFile = properties.getProperty(DocImporter.CONFIG_PARAM_MASTER_FILE, DocImporter.DEFAULT_SOURCE_FORMAT);
            log.info("masterFile: " + this.masterFile);
            if (!sourceDocPathNode.hasNode(masterFile)) {
                log.info("Master File " + masterFile + " not available in the folder " + sourceDocPathNode.getPath());
                return;
            }

            // Set the graphics folder
            this.graphicsFolder = properties.getProperty(DocImporter.CONFIG_PARAM_GRAPHICS_FOLDER, DocImporter.DEFAULT_SOURCE_FORMAT);
            log.info("graphicsFolder: " + this.graphicsFolder);

            // Set the target path
            this.targetPath = properties.getProperty(DocImporter.CONFIG_PARAM_TARGET_PATH, DocImporter.DEFAULT_SOURCE_FORMAT);
            log.info("targetPath: " + this.targetPath);

            // Set the XSLT file
            String sourceFormat = properties.getProperty(DocImporter.CONFIG_PARAM_SOURCE_FORMAT, DocImporter.DEFAULT_SOURCE_FORMAT);
            log.info("sourceFormat: " + sourceFormat);
            if (sourceFormat.equalsIgnoreCase(DocImporter.SOURCE_FORMAT_DOCBOOK)) {
                this.xsltFile = DocImporter.DOCBOOK_XSLT_PATH;
            } else {
                this.xsltFile = DocImporter.DITA_XSLT_PATH;
            }
            log.info("xsltFile: " + this.xsltFile);

            // Get the XSLT file node
            Node xsltNode = session.getNode(xsltFile);

            // Create the XML reader (the SAX parser)
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();

            // Set the entity resolver to a RejectingEntityResolver.
            // todo: this won't work for docbook, which depends on entity references
            xmlReader.setEntityResolver(new RejectingEntityResolver());

            // Create a custom URIResolver for JCR content
            URIResolver uriResolver = new DocImporterURIResolver(xsltNode, sourceDocPathNode, xmlReader);

            // Create the XSLT transformer
            TransformerFactory transformerFactory = new TransformerFactoryImpl();
            transformerFactory.setURIResolver(uriResolver);
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(JcrUtils.readFile(xsltNode)));

            // Pass all properties to XSLT transformer
            for (Entry<Object, Object> entry : properties.entrySet()) {
                transformer.setParameter(entry.getKey().toString(), entry.getValue());
            }

            // Pass own XSLT path to the XSLT
            transformer.setParameter("xsltFile", this.xsltFile);

            // Create a temporary location
            Node tmpPathNode = JcrUtil.createPath(DocImporter.SOURCE_DOC_PATH, "nt:folder", "nt:folder", session, true);
            tmpPathNode = JcrUtil.createUniqueNode(tmpPathNode, sourceDocPathNode.getName(), "nt:folder", session);
            session.save();

            // Run the XSLT transformation
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new SAXSource(xmlReader, new InputSource(JcrUtils.readFile(sourceDocPathNode.getNode(masterFile)))), new StreamResult(output));

            // Copy transformed output stream to an input stream
            InputStream result = new ByteArrayInputStream(output.toByteArray());

            // Place package template into tmp folder
            Node packageFolderNode = JcrUtil.copy(session.getNode(DocImporter.PACKAGE_TEMPLATE_PATH), tmpPathNode, "package");

            // Create content.xml file containing xslt result
            Node contentXMLNode = JcrUtil.createPath(packageFolderNode.getPath() + "/jcr_root" + targetPath, "nt:folder", "nt:folder", sourceDocPathNode.getSession(), true);
            JcrUtils.putFile(contentXMLNode, ".content.xml", "application/xml", result);

            // Copy graphic resources
            JcrUtil.copy(sourceDocPathNode.getNode(graphicsFolder), contentXMLNode, graphicsFolder);
            JcrUtils.putFile(packageFolderNode.getNode("META-INF/vault/"), "filter.xml", "application/xml", FilterXmlBuilder.fromRoot(targetPath + "/").toStream(graphicsFolder));

            // Import the prepared content package into 'real' content using FileVault
            JcrArchive archive = new JcrArchive(packageFolderNode, "/");
            archive.open(true);
            Importer importer = new Importer();
            importer.getOptions().setImportMode(ImportMode.MERGE);
            importer.getOptions().setAccessControlHandling(AccessControlHandling.MERGE);
            importer.run(archive, packageFolderNode.getSession().getNode("/"));

            // Delete the tmp folder
            tmpPathNode.remove();

            // Save all
            session.save();
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
