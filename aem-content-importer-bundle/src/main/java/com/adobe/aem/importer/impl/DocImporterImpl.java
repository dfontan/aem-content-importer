/*******************************************************************************
 * Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
 *
 * Licensed under the Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0
 ******************************************************************************/

package com.adobe.aem.importer.impl;

import com.adobe.aem.importer.DocImporter;
import com.adobe.aem.importer.exception.DocImporterException;
import com.adobe.aem.importer.exception.DocImporterException.AEM_IMPORTER_EXCEPTION_TYPE;
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
    @Property(name = Constants.SERVICE_DESCRIPTION, value = "Adobe - XSLT Transformer Service"),
    @Property(name = Constants.SERVICE_VENDOR, value = "Adobe")})
@Service(value = DocImporter.class)
public class DocImporterImpl implements DocImporter {

    private static final Logger log = LoggerFactory.getLogger(DocImporterImpl.class);

    private String xsltFile;
    private String masterFile;
    private String graphicsFolder;
    private String targetPath;

    /*
    public DocImporterImpl(){

        // Default format is DITA therefore the default xsltFile is dita-to-content.xsl
        this.xsltFile = DocImporter.DEFAULT_XSLT_PATH;

        // Default masterFile
        this.masterFile = DocImporter.DEFAULT_MASTER_FILE;

        // Default graphicsFolders
        this.graphicsFolder = DocImporter.DEFAULT_GRAPHICS_FOLDER;

        // Default targetPath
        this.targetPath = DocImporter.DEFAULT_TARGET_PATH;
    }
    */

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



    public void doImport(Node sourcePathNode, Properties properties) throws DocImporterException {

        String sourceFormat = properties.getProperty(DocImporter.CONFIG_PARAM_SOURCE_FORMAT, DocImporter.DEFAULT_SOURCE_FORMAT);
        if (sourceFormat.equalsIgnoreCase(DocImporter.SOURCE_FORMAT_DITA)) {
            this.xsltFile = DocImporter.DITA_XSLT_PATH;
        } else if (sourceFormat.equalsIgnoreCase(DocImporter.SOURCE_FORMAT_DOCBOOK)) {
            this.xsltFile = DocImporter.DOCBOOK_XSLT_PATH;
        }
        this.masterFile = properties.getProperty(DocImporter.CONFIG_PARAM_MASTER_FILE, DocImporter.DEFAULT_SOURCE_FORMAT);
        this.graphicsFolder = properties.getProperty(DocImporter.CONFIG_PARAM_GRAPHICS_FOLDER, DocImporter.DEFAULT_SOURCE_FORMAT);
        this.targetPath = properties.getProperty(DocImporter.CONFIG_PARAM_TARGET_PATH, DocImporter.DEFAULT_SOURCE_FORMAT);

        try {
            // Check that the master file exists
            if (!sourcePathNode.hasNode(masterFile)) throw new DocImporterException(AEM_IMPORTER_EXCEPTION_TYPE.ERROR_PARAMS, "Master File " + masterFile + " not available in the folder " + sourcePathNode.getPath());

            // Get the JCR session
            Session session = sourcePathNode.getSession();

            // Get the XSLT file node
            Node xsltNode = session.getNode(xsltFile);

            // Create the XML reader (the SAX parser)
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();

            // Set the entity resolver to a RejectingEntityResolver.
            // todo: this won't work for docbook, which depends on entity references
            xmlReader.setEntityResolver(new RejectingEntityResolver());

            // Create a custom URIResolver for JCR content
            URIResolver uriResolver = new DocImporterURIResolver(xsltNode, sourcePathNode, xmlReader);

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
            tmpPathNode = JcrUtil.createUniqueNode(tmpPathNode, sourcePathNode.getName(), "nt:folder", session);
            session.save();

            // Run the XSLT transformation
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new SAXSource(xmlReader, new InputSource(JcrUtils.readFile(sourcePathNode.getNode(masterFile)))), new StreamResult(output));

            // Copy transformed output stream to an input stream
            InputStream result = new ByteArrayInputStream(output.toByteArray());

            // Place package template into tmp folder
            Node packageFolderNode = JcrUtil.copy(session.getNode(DocImporter.PACKAGE_TEMPLATE_PATH), tmpPathNode, "package");

            // Create content.xml file containing xslt result
            Node contentXMLNode = JcrUtil.createPath(packageFolderNode.getPath() + "/jcr_root" + targetPath, "nt:folder", "nt:folder", sourcePathNode.getSession(), true);
            JcrUtils.putFile(contentXMLNode, ".content.xml", "application/xml", result);

            // Copy graphic resources
            JcrUtil.copy(sourcePathNode.getNode(graphicsFolder), contentXMLNode, graphicsFolder);
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
        } catch (RepositoryException e) {
            throw new DocImporterException(AEM_IMPORTER_EXCEPTION_TYPE.UNEXPECTED, e.getMessage(), e);
        } catch (TransformerException e) {
            throw new DocImporterException(AEM_IMPORTER_EXCEPTION_TYPE.UNEXPECTED, e.getMessage(), e);
        } catch (SAXException e){
            throw new DocImporterException(AEM_IMPORTER_EXCEPTION_TYPE.UNEXPECTED, e.getMessage(), e);
        } catch (IOException e) {
            throw new DocImporterException(AEM_IMPORTER_EXCEPTION_TYPE.UNEXPECTED, e.getMessage(), e);
        } catch (ConfigurationException e){
            throw new DocImporterException(AEM_IMPORTER_EXCEPTION_TYPE.UNEXPECTED, e.getMessage(), e);
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