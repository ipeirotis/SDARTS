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
		<starts:url method="get">

<xsl:text>http://www.ncbi.nlm.nih.gov/entrez/query.fcgi</xsl:text>
		</starts:url>

		<xsl:apply-templates select="starts:filter"/>
	</starts:script>
</xsl:template>

<xsl:template match="starts:squery/starts:filter">
	<starts:variable>
		<starts:name>cmd</starts:name>
		<starts:value>Search</starts:value>
	</starts:variable>

	<starts:variable>
		<starts:name>db</starts:name>
		<starts:value>PubMed</starts:value>
	</starts:variable>

	<starts:variable>
		<starts:name>dispmax</starts:name>
		<starts:value>
                <xsl:value-of select="/starts:squery/@max-docs"/>
                </starts:value>
	</starts:variable>

	<starts:variable>
                <starts:name>term</starts:name>
                <starts:value>
                 <xsl:for-each select=".//starts:value">
					<xsl:choose>
						<xsl:when test="string(../starts:field/@name)='author'">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.),'[au] AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),'[au]',' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string(../starts:field/@name)='mesh-term'">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.),'[MH] AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),'[MH]',' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string(../starts:field/@name)='title' ">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.),'[TI] AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),'[TI]',' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string(../starts:field/@name)='title-abstract' ">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.),'[TIAB] AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),'[TIAB]',' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string(../starts:field/@name)='journal-title' ">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.),'[TA] AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),'[TA]',' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string(../starts:field/@name)='publication-type' ">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.),'[PT] AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),'[PT]',' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string(../starts:field/@name)='mesh-major-topic' ">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.),'[MAJR] AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),'[MAJR]',' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string(../starts:field/@name)='mesh-subheading' ">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.),'[SH] AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),'[SH]',' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string(../starts:field/@name)='mesh-with-subheading' ">
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.), ' AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)), ' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="position()=last()">
									<xsl:value-of select="concat(normalize-space(.), ' AND BMJ [Journal:__jrid2274]')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(normalize-space(string(.)),' AND ')"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
                 </xsl:for-each>
                </starts:value>
        </starts:variable>
</xsl:template>

<!--
	<starts:variable>
		<starts:name>term</starts:name>
		<starts:value>
			<xsl:for-each select="./*">
				<xsl:if test="name()='starts:term'">
					<xsl:apply-templates/>
				</xsl:if>
				<xsl:if test="name()='starts:filter'">
					<xsl:apply-templates/>
				</xsl:if>
			</xsl:for-each>
		</starts:value>
	</starts:variable>
</xsl:template>

<xsl:template match="starts:filter/starts:filter">
	<xsl:for-each select="./*">
				<xsl:if test="name()='starts:term'">
					<xsl:apply-templates/>
				</xsl:if>
				<xsl:if test="name()='starts:filter'">
					<xsl:apply-templates/>
				</xsl:if>
	</xsl:for-each>
</xsl:template>

<xsl:template match="starts:term">
	<xsl:apply-templates select="starts:value"/>
</xsl:template>

<xsl:template match="starts:value">
	<xsl:value-of select="."/>
	<xsl:text> AND </xsl:text>
</xsl:template>

<xsl:template match="text()">
	<xsl:value-of select="normalize-space()"/>
</xsl:template>
-->
</xsl:stylesheet>
