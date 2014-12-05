<?xml version="1.0" encoding="UTF-8"?>
<!-- Process each topic file -->
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    xmlns:nt="http://www.jcp.org/jcr/nt/1.0">

    <xsl:template match="concept | task">
        <xsl:param name="pathPrefix"/>
        <jcr:content
            jcr:primaryType="nt:unstructured"
            jcr:title="{title}"
            sling:resourceType="mac-help/components/page_article">
            <xsl:apply-templates select="conbody | taskbody">
                    <xsl:with-param name="pathPrefix" select="$pathPrefix"/>
            </xsl:apply-templates>
        </jcr:content>
    </xsl:template>

    <xsl:template match="conbody | taskbody">
        <xsl:param name="pathPrefix"/>
        <par
            jcr:primaryType="nt:unstructured"
            sling:resourceType="wcm/foundation/components/parsys">
            <xsl:variable name="serialized">
                <xsl:apply-templates select="*" mode="serialize">
                    <xsl:with-param name="pathPrefix" select="$pathPrefix"/>
                </xsl:apply-templates>
            </xsl:variable>
            <text
                jcr:primaryType="nt:unstructured"
                sling:resourceType="wcm/foundation/components/text"
                textIsRich="true"
                text="{$serialized}"/>
        </par>
    </xsl:template>
</xsl:stylesheet>