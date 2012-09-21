<?xml version="1.0" encoding="UTF-8"?>
<!-- Transforms a starts:squery into a starts:intermediate, holding a starts:script that can be used -->
<!-- to query the PubMed search engine -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:starts="http://sdarts.cs.columbia.edu/STARTS/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.w3.org/2001/XMLSchema-instance">

<!-- All doc_style.xsl files you write ought to have this instruction -->
<!-- here, in order to make sure that the document output is declared -->
<!-- as being of type "starts_intermediate.dtd"                       -->
<xsl:output method="xml"/> <!--doctype-system="http://sdarts.cs.columbia.edu/dtd/starts_intermediate.dtd"/-->
	
<xsl:template match="/">
	<starts:intermediate>
		<xsl:attribute name="xsi:schemaLocation">http://sdarts.cs.columbia.edu/STARTS/ http://sdarts.cs.columbia.edu/xsd/starts_intermediate.xsd</xsl:attribute>
	<xsl:apply-templates/>
	</starts:intermediate>
</xsl:template>

<xsl:template match="starts:squery">
	<starts:script>
		<starts:url method="post">
      <xsl:text>http://www.noah-health.org/cgi-bin/htsearch</xsl:text>
		</starts:url>

    <starts:variable>
      <starts:name>config</starts:name>
      <starts:value>htdig</starts:value>
    </starts:variable>

    <starts:variable>
      <starts:name>method</starts:name>
      <starts:value>boolean</starts:value>
    </starts:variable>

    <starts:variable>
      <starts:name>format</starts:name>
      <starts:value>builtin-short</starts:value>
    </starts:variable>

    <starts:variable>
      <starts:name>words</starts:name>
      <starts:value>
        <xsl:apply-templates select="starts:filter"/>
      </starts:value>
    </starts:variable>

	</starts:script>
</xsl:template>

<!-- process starts:filter recursively -->

<!-- 1) filter of type: TERM  -->
<xsl:template match="starts:filter[(count(*) = 1) and ( name(./*[1]) = 'starts:term')]">
  <xsl:apply-templates select="starts:term" />
</xsl:template>

<!-- 2) filter of type: FILTER_BOOLEANOP_FILTER -->
<!-- noah supports boolean op -->
<xsl:template match="starts:filter[(count(*) = 3) and (name(./*[1]) = 'starts:filter') and (name(./*[2]) = 'starts:boolean-op') and (name(./*[3]) = 'starts:filter')]">
  <xsl:apply-templates select="./*[1]" />
  <xsl:variable name="opname" select="./*[2]/@name" />
  <xsl:choose>
    <xsl:when test="$opname = 'and'">
      <xsl:text> AND </xsl:text>
    </xsl:when>
    <!-- no NOT support -->
    <xsl:otherwise>
      <xsl:text> OR </xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates select="./*[3]" />
</xsl:template>

<!-- 3) filter of type: TERM_PROXOP_TERM -->
<xsl:template match="starts:filter[(count(*) = 3) and (name(./*[1]) = 'starts:term') and (name(./*[2]) = 'starts:prox-op') and (name(./*[3]) = 'starts:term')]">
<!-- this is not supported , so don't implement -->
</xsl:template>

<xsl:template match="starts:term">
  <xsl:apply-templates select="starts:value"/>
</xsl:template>

<xsl:template match="starts:value">
  <xsl:value-of select="concat(text(), ' ')"/>
</xsl:template>

</xsl:stylesheet>
