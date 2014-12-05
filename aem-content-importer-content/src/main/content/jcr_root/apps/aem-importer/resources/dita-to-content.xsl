<?xml version="1.0" encoding="UTF-8"?>
<!-- Dita to JCR -->
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs">

    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:include href="functions.xsl"/>
    <xsl:include href="ditamap-to-jcr.xsl"/>
    <xsl:include href="topic-to-jcr.xsl"/>
    <xsl:include href="dita-to-html5.xsl"/>

</xsl:stylesheet>