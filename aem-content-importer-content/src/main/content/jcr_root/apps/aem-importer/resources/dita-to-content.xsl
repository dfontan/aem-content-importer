<?xml version="1.0" encoding="UTF-8"?>
<!-- Dita to JCR -->
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs" xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
    xmlns:cq="http://www.day.com/jcr/cq/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
    xmlns:pd="http://www.adobe.com/pando">

    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>


    <!-- Templates -->

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


    <!-- Helper Functions -->

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


    <!-- Map DITA To HTML -->

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

</xsl:stylesheet>