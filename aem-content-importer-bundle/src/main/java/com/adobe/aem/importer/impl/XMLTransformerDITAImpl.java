/*******************************************************************************
 * Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
 *
 * Licensed under the Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0
 ******************************************************************************/

package com.adobe.aem.importer.impl;

import com.adobe.aem.importer.XMLTransformer;
import com.adobe.aem.importer.exception.AemImporterException;
import com.adobe.aem.importer.exception.AemImporterException.AEM_IMPORTER_EXCEPTION_TYPE;
import com.adobe.aem.importer.xml.FilterXmlBuilder;
import com.adobe.aem.importer.xml.RejectingEntityResolver;
import com.day.cq.commons.jcr.JcrUtil;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
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
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;


@Component
@Service(value = XMLTransformer.class)
@org.apache.felix.scr.annotations.Properties({
    @Property(name = Constants.SERVICE_DESCRIPTION, value = "Adobe - DITA XSLT Transformer Service"),
    @Property(name = Constants.SERVICE_VENDOR, value = "Adobe")})
public class XMLTransformerDITAImpl implements XMLTransformer {

    private static final Logger log = LoggerFactory.getLogger(XMLTransformerDITAImpl.class);

    @Override
    public void transform(Node srcPath, Properties properties) throws AemImporterException {
        log.info("XMLTransformerDITA transformer starts to check out input parameters");

        String xsltFile = "/apps/aem-importer/resources/dita-to-content.xsl";
        String masterFile = "mcloud.ditamap";
        String[] graphicsFolders = {"images", "graphics", "Graphics"};
        String destPath = "/content/dita-mcloud-import";
        String packageTemplate = "/apps/aem-importer/resources/package-tpl";
        String tmpPath = "/var/aem-importer/tmp";
/*
        if (properties != null) {
            String sourceFormat = properties.getProperty("source-format", "DITA");

            if (sourceFormat.equalsIgnoreCase("DITA")) {
                xsltFile = "/apps/aem-importer/resources/dita-to-content.xsl";
            } else if (sourceFormat.equalsIgnoreCase("DocBook")) {
                xsltFile = "/apps/aem-importer/resources/docbook-to-content.xsl";
            }

            masterFile = properties.getProperty("master-file", "*.ditamap");
            graphicsFolders = properties.getProperty("graphics-folders", "images,graphics,Graphics").split(",");
            destPath = properties.getProperty("destination", "/content/aem-content-importer/imported");
        }

*/
        try {

            // Check Master File
            if (!srcPath.hasNode(masterFile))
                throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.ERROR_PARAMS, "Master File " + masterFile + " not available in the folder " + srcPath.getPath());

            // Check XSLT File
            Session session = srcPath.getSession();
            if (!session.itemExists(xsltFile))
                throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.ERROR_PARAMS, "XSLT Node File not available (" + xsltFile + ")");
            final Node xsltNode = session.getNode(xsltFile);

            // Check Package Template
            if (!session.itemExists(packageTemplate))
                throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.ERROR_PARAMS, "Package Template Node File not available (" + packageTemplate + ")");
            Node packageTplNode = session.getNode(packageTemplate);

            // Create XML Reader
            log.debug("Create XML Reader");
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setEntityResolver(new RejectingEntityResolver());

            // XSLT Transformer init
            URIResolver uriResolver = new XMLTransformerDITAResolver(xsltNode, srcPath, xmlReader);
            TransformerFactory transformerFactory = new TransformerFactoryImpl();
            transformerFactory.setURIResolver(uriResolver);
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(JcrUtils.readFile(xsltNode)));

            // Pass all properties to XSLT transformer
            for (Entry<Object, Object> entry : properties.entrySet()) {
                log.debug("Pass to transformer the property {}: {}", entry.getKey().toString(), entry.getValue());
                transformer.setParameter(entry.getKey().toString(), entry.getValue());
            }

            // Temp Path
            Node tmpPathNode = JcrUtil.createPath(tmpPath, "nt:folder", "nt:folder", srcPath.getSession(), true);
            tmpPathNode = JcrUtil.createUniqueNode(tmpPathNode, srcPath.getName(), "nt:folder", srcPath.getSession());
            srcPath.getSession().save();
            log.debug("Create tmp destination folder {}", tmpPathNode.getPath());

            // Transform
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new SAXSource(xmlReader, new InputSource(JcrUtils.readFile(srcPath.getNode(masterFile)))), new StreamResult(output));
            log.debug("Start transformation process reading master file {}", masterFile);

            // Copy transformed output stream to input
            InputStream stream = new ByteArrayInputStream(output.toByteArray());

            // Prepare package folders and copy transformed content stream
            final Node packageFolderNode = JcrUtil.copy(packageTplNode, tmpPathNode, "package");
            Node contentNode = JcrUtil.createPath(packageFolderNode.getPath() + "/jcr_root" + destPath, "nt:folder", "nt:folder", srcPath.getSession(), true);
            log.debug("Create package folder on {} using the template {}", contentNode.getPath(), packageFolderNode.getPath());
            JcrUtils.putFile(contentNode, ".content.xml", "application/xml", stream);

            // Copy graphic resources
            for (String candidate : graphicsFolders)
                if (srcPath.hasNode(candidate)) {
                    log.debug("Add graphic folder {}", srcPath.getPath() + "/" + candidate);
                    JcrUtil.copy(srcPath.getNode(candidate), contentNode, candidate);
                    JcrUtils.putFile(packageFolderNode.getNode("META-INF/vault/"), "filter.xml", "application/xml", FilterXmlBuilder.fromRoot(destPath + "/").toStream(candidate));
                }

            importArchive(packageFolderNode);

            // Delete tmp folder
            tmpPathNode.remove();
            tmpPathNode.getSession().save();
        } catch (Exception e) {

            if (e instanceof AemImporterException) {
                throw (AemImporterException) e;
            } else {
                throw new AemImporterException(AEM_IMPORTER_EXCEPTION_TYPE.UNEXPECTED, e.getMessage(), e);
            }


        }

        log.info("XMLTransformerDITA transformation is completed");
    }

    /**
     * Create Archive & import contents
     *
     * @param packageNode package node
     * @throws java.io.IOException
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws org.apache.jackrabbit.vault.fs.config.ConfigurationException
     */
    protected void importArchive(Node packageNode) throws IOException, RepositoryException, ConfigurationException {
        // Create Archive
        log.debug("Create the archive on node {}", packageNode.getPath());
        JcrArchive archive = new JcrArchive(packageNode, "/");
        archive.open(true);

        // Run importer
        Importer importer = new Importer();
        importer.getOptions().setImportMode(ImportMode.MERGE);
        importer.getOptions().setAccessControlHandling(AccessControlHandling.MERGE);

        log.debug("Run the archive importer");
        importer.run(archive, packageNode.getSession().getNode("/"));

        // Save all
        packageNode.getSession().save();
    }

    /**
     * Get XML Filter content file
     *
     * @param paths paths
     * @return String
     */
    protected String xmlPackageFilter(List<String> paths) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        sb.append(" <workspaceFilter version=\"1.0\">\r\n");
        for (String path : paths)
            sb.append("  <filter root=\"").append(path).append("\" />\r\n");
        sb.append(" </workspaceFilter>");
        return sb.toString();
    }

    private class XMLTransformerDITAResolver implements URIResolver {
        /* XSLT Node */
        private Node xsltNode;
        /* Source Node */
        private Node srcNode;
        /* XML Reader */
        private XMLReader xmlReader;

        /**
         * Constructor
         *
         * @param xsltNode xslt node
         * @param srcNode src node
         * @param xmlReader xml reader
         */
        public XMLTransformerDITAResolver(Node xsltNode, Node srcNode, XMLReader xmlReader) {
            this.xsltNode = xsltNode;
            this.srcNode = srcNode;
            this.xmlReader = xmlReader;
        }

        /* (non-Javadoc)
         * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
         */
        @Override
        public Source resolve(String href, String base) throws TransformerException {
            try {
                final Node node = (href.endsWith("xsl") ? this.xsltNode.getParent().getNode(href) : this.srcNode.getNode(href));
                XMLTransformerDITAImpl.log.debug("Resolving resource {}", node.getPath());
                return new SAXSource(this.xmlReader, new InputSource(JcrUtils.readFile(node)));
            } catch (RepositoryException e) {
                throw new TransformerException("Cannot resolve " + href + " in either [parent of " + this.xsltNode + " or " + this.srcNode + "]");
            }
        }


    }

}
