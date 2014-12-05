<?xml version="1.0" encoding="UTF-8"?>
<!-- String Functions: Convert URLS and filenames into valid XML element names -->
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    xmlns:pd="http://www.adobe.com/pando">

    <xsl:function name="pd:removeTrailingSlash">
        <xsl:param name="path"/>
        <xsl:value-of select="replace($path, '/+$', '')"/>
    </xsl:function>

    <xsl:function name="pd:removeExtension">
        <xsl:param name="path"/>
        <xsl:value-of select="replace($path, '\.[^\.]*$', '')"/>
    </xsl:function>

    <xsl:function name="pd:getFragment">
        <xsl:param name="path"/>
        <xsl:value-of select="tokenize($path, '#')[2]"/>
    </xsl:function>

    <xsl:function name="pd:removeFragment">
        <xsl:param name="path"/>
        <xsl:value-of select="tokenize($path, '#')[1]"/>
    </xsl:function>

    <xsl:function name="pd:pathToFileName">
        <xsl:param name="path"/>
        <xsl:value-of select="tokenize(pd:removeTrailingSlash($path), '/')[last()]"/>
    </xsl:function>

    <xsl:function name="pd:replaceNonAlphanum">
        <xsl:param name="name"/>
        <xsl:variable name="alphanum" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_0123456789'"/>
        <xsl:variable name="underscores" select="'_____________________________________'"/>
        <xsl:variable name="xmlName" select="translate($name, translate($name, $alphanum, ''), $underscores)"/>
        <xsl:if test="$xmlName=''">
            <xsl:value-of select="'_dummy'"/>
        </xsl:if>
        <xsl:value-of select="$xmlName"/>
    </xsl:function>

    <xsl:function name="pd:pathToXMLElementName">
        <xsl:param name="path"/>
        <xsl:value-of select="pd:replaceNonAlphanum(pd:removeExtension(pd:pathToFileName($path)))"/>
    </xsl:function>

</xsl:stylesheet>