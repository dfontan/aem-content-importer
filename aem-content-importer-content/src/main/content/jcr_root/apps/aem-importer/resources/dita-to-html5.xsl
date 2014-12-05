<?xml version="1.0" encoding="UTF-8"?>
<!-- Serialize DITA content into HTML5 richtext. -->
<xsl:stylesheet
        version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        exclude-result-prefixes="xs"
        xmlns:pd="http://www.adobe.com/pando">

    <!-- MAP DITA TO HTML -->
    <xsl:function name="pd:convert">
        <xsl:param name="dita-el"/>
        <xsl:variable name="mapping">
            <entry key="keyword">b</entry>
            <entry key="filepath">i</entry>
            <entry key="codeph">i</entry>
            <entry key="steps">ul</entry>
            <entry key="step">li</entry>
            <entry key="stepresult">p</entry>
            <entry key="choices">ul</entry>
            <entry key="choice">li</entry>
            <entry key="postreq">p</entry>
            <entry key="cmd">b</entry>
            <entry key="context">p</entry>
            <entry key="info">p</entry>
            <entry key="result">p</entry>
            <entry key="ul">ul</entry>
            <entry key="ol">ol</entry>
            <entry key="li">li</entry>
            <entry key="sl">dl</entry>
            <entry key="sli">dt</entry>
            <entry key="xref">a</entry>
            <entry key="term">b</entry>
            <entry key="b">b</entry>
            <entry key="title">title</entry>
            <entry key="table">table</entry>
            <entry key="thead">thead</entry>
            <entry key="tbody">tbody</entry>
            <entry key="row ">tr</entry>
            <entry key="entry">td</entry>
            <entry key="codeblock">code</entry>
            <entry key="menucascade">p</entry>
            <entry key="uicontrol">b</entry>
            <entry key="wintitle">b</entry>
            <entry key="image">img</entry>
            <entry key="note">p</entry>
            <!-- TODO
            <entry key="tgroup">?</entry>
            <entry key="colspec">?</entry>
            -->
        </xsl:variable>
        <xsl:variable name="html-el" select="$mapping/entry[@key=$dita-el]"/>
        <xsl:choose>
            <xsl:when test="$html-el">
                <xsl:value-of select="$html-el"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$dita-el"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--  ELEMENTS -->
    <!-- xref element -->
    <xsl:template match="xref" mode="serialize">
        <xsl:param name="pathPrefix"/>
        <xsl:text>&lt;a href=&quot;</xsl:text>
        <xsl:value-of select="$pathPrefix"/>
        <xsl:choose>
            <xsl:when test="@scope='local'">
                <xsl:value-of select="pd:removeExtension(pd:removeFragment(@href))"/>
                <xsl:text>.html</xsl:text>
                <xsl:if test="pd:getFragment(@href) != ''">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="pd:getFragment(@href)"/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@href"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&quot;&gt;</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>&lt;/a&gt;</xsl:text>
    </xsl:template>

    <!-- image element -->
    <xsl:template match="image" mode="serialize">
        <xsl:param name="pathPrefix"/>
        <xsl:text>&lt;img src=&quot;</xsl:text>
        <xsl:value-of select="$pathPrefix"/>
        <xsl:value-of select="@href"/>
        <xsl:text>&quot;/&gt;</xsl:text>
    </xsl:template>

    <!-- Other elements -->
    <xsl:template match="*" mode="serialize">
        <xsl:param name="pathPrefix"/>
        <xsl:text>&lt;</xsl:text>
        <xsl:value-of select="pd:convert(name())"/>

        <xsl:if test="not(@class)">
            <xsl:text> class="</xsl:text>
            <xsl:text> dita-</xsl:text>
            <xsl:value-of select="name()"/>
            <xsl:text>"</xsl:text>
        </xsl:if>
        <xsl:apply-templates select="@*" mode="serialize">
            <xsl:with-param name="dita-el" select="name()"/>
        </xsl:apply-templates>

        <xsl:choose>
            <xsl:when test="node()">
                <xsl:text>&gt;</xsl:text>
                <xsl:apply-templates select="*|text()" mode="serialize">
                    <xsl:with-param name="pathPrefix" select="$pathPrefix"/>
                </xsl:apply-templates>
                <xsl:text>&lt;/</xsl:text>
                <xsl:value-of select="pd:convert(name())"/>
                <xsl:text>&gt;</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>/&gt;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ATTRIBUTES -->
    <!-- class attribute -->
    <xsl:template match="@class" mode="serialize">
        <xsl:param name="dita-el"/>
        <xsl:text> class="</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text> dita-</xsl:text>
        <xsl:value-of select="$dita-el"/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <!-- Other attributes -->
    <xsl:template match="@*" mode="serialize">
        <xsl:text> </xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>="</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <!-- TEXT -->
    <xsl:template match="text()" mode="serialize">
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>