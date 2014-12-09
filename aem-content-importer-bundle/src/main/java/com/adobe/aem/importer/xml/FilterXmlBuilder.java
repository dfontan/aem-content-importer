/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 * Copyright 2014 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package com.adobe.aem.importer.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by colligno on 14/11/2014.
 */
public class FilterXmlBuilder {

   private String root;

    private FilterXmlBuilder(String root) {
        this.root = root;
    }

    public String toXml(String nodename) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("<workspaceFilter version=\"1.0\">")
        .append("<filter root=\"").append(root).append(nodename).append("\" />")
        .append("</workspaceFilter>");

        return sb.toString();
    }

    public InputStream toStream(String nodename) {
        return new ByteArrayInputStream(toXml(nodename).getBytes());
    }

    public static FilterXmlBuilder fromRoot(String root) {
        return new FilterXmlBuilder(root);
    }
}
