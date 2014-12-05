<?xml version="1.0" encoding="UTF-8"?>
<!-- Convert ditamap to TOC page and each topic XML to subpage -->
<xsl:stylesheet
    version="2.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xs="http://www.w3.org/2001/XMLSchema"
     exclude-result-prefixes="xs"
     xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
     xmlns:cq="http://www.day.com/jcr/cq/1.0"
     xmlns:jcr="http://www.jcp.org/jcr/1.0"
     xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
     xmlns:pd="http://www.adobe.com/pando">

    <xsl:template match="/">
        <xsl:apply-templates select="map"/>
    </xsl:template>

    <xsl:template match="map">
        <jcr:root jcr:primaryType="cq:Page">
            <jcr:content
                jcr:primaryType="nt:unstructured"
                jcr:title="{title}"
                sling:resourceType="mac-help/components/page_section">
                <par
                    jcr:primaryType="nt:unstructured"
                    sling:resourceType="wcm/foundation/components/parsys">
                    <sitemap
                    jcr:primaryType="nt:unstructured"
                    sling:resourceType="mac-help/components/sitemap"
                    rootPath="/content/pando"/>
                </par>
            </jcr:content>
            <xsl:apply-templates select="topicref | mapref" mode="subpage">
                <xsl:with-param name="pathPrefix" select="''"/>
            </xsl:apply-templates>
        </jcr:root>
    </xsl:template>

    <xsl:template match="topicref" mode="subpage">
        <xsl:param name="pathPrefix"/>
        <xsl:variable name="xmlElementName" select="pd:pathToXMLElementName(@href)"/>
        <xsl:variable name="nextPathPrefix" select="concat('../', $pathPrefix)"/>
        <xsl:choose>
            <xsl:when test="@scope='local'">
                <xsl:element name="{$xmlElementName}">
                    <xsl:attribute name="jcr:primaryType" select="'cq:Page'"/>
                    <xsl:apply-templates select="document(@href)/(concept | task)">
                        <xsl:with-param name="pathPrefix" select="$pathPrefix"/>
                    </xsl:apply-templates>
                    <xsl:apply-templates select="topicref | mapref" mode="subpage">
                        <xsl:with-param name="pathPrefix" select="$nextPathPrefix"/>
                    </xsl:apply-templates>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{$xmlElementName}">
                    <xsl:attribute name="jcr:primaryType" select="'cq:Page'"/>
                    <xsl:apply-templates select="topicref | mapref" mode="subpage">
                        <xsl:with-param name="pathPrefix" select="$nextPathPrefix"/>
                    </xsl:apply-templates>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="mapref" mode="subpage">
        <xsl:param name="pathPrefix"/>
        <xsl:variable name="xmlElementName" select="pd:pathToXMLElementName(@href)"/>
        <xsl:variable name="nextPathPrefix" select="concat('../', $pathPrefix)"/>
        <xsl:element name="{$xmlElementName}">
            <xsl:attribute name="jcr:primaryType" select="'cq:Page'"/>
            <xsl:apply-templates select="topicref | mapref">
                <xsl:with-param name="pathPrefix" select="$nextPathPrefix"/>
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>