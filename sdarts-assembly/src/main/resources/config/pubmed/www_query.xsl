<?xml version="1.0" encoding="UTF-8"?>
<!-- Transforms a starts:squery into a starts:intermediate, holding a starts:script that can be used -->
<!-- to query the PubMed search engine -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:starts="http://sdarts.cs.columbia.edu/STARTS/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.w3.org/2001/XMLSchema-instance">
	<!-- All doc_style.xsl files you write ought to have this instruction -->
	<!-- here, in order to make sure that the document output is declared -->
	<!-- as being of type "starts_intermediate.dtd"                       -->
	<xsl:output method="xml"/>
	<!--doctype-system="http://sdarts.cs.columbia.edu/dtd/starts_intermediate.dtd"/-->
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
					<xsl:apply-templates select="starts:filter"/>
				</starts:value>
			</starts:variable>
		</starts:script>
	</xsl:template>
	<!-- process starts:filter recursively -->
	<!-- 1) filter of type: TERM  -->
	<xsl:template match="starts:filter[(count(*) = 1) and ( name(./*[1]) = 'starts:term')]">
		<xsl:apply-templates select="starts:term"/>
	</xsl:template>
	<!-- 2) filter of type: FILTER_BOOLEANOP_FILTER -->
	<xsl:template match="starts:filter[(count(*) = 3) and (name(./*[1]) = 'starts:filter') and (name(./*[2]) = 'starts:boolean-op') and (name(./*[3]) = 'starts:filter')]">
		<xsl:text>(</xsl:text>
		<xsl:apply-templates select="./*[1]"/>
		<xsl:variable name="opname" select="./*[2]/@name"/>
		<xsl:choose>
			<xsl:when test="$opname = 'and'">
				<xsl:text> AND </xsl:text>
			</xsl:when>
			<!-- no NOT support -->
			<xsl:otherwise>
				<xsl:text> OR </xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="./*[3]"/>
		<xsl:text>)</xsl:text>
	</xsl:template>
	<!-- 3) filter of type: TERM_PROXOP_TERM -->
	<xsl:template match="starts:filter[(count(*) = 3) and (name(./*[1]) = 'starts:term') and (name(./*[2]) = 'starts:prox-op') and (name(./*[3]) = 'starts:term')]">
		<!-- this is not supported , so don't implement -->
	</xsl:template>
	<xsl:template match="starts:term">
		<xsl:apply-templates select="starts:value"/>
	</xsl:template>
	<xsl:template match="starts:value">
		<xsl:choose>
			<xsl:when test="string(../starts:field/@name)='author'">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[au]')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[au]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='mesh-term'">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[MH]')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[MH]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='subset'">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[sb')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[sb]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='title' ">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[TI]')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[TI]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='title-abstract' ">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[TIAB]')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[TIAB]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='journal-title' ">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[TA]')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[TA]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='mesh-with-subheading' ">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="normalize-space(.)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)), ' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='publication-type' ">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[PT]')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[PT]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='mesh-major-topic' ">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[MAJR]')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[MAJR]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="string(../starts:field/@name)='mesh-subheading' ">
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="concat(normalize-space(.),'[SH]')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),'[SH]',' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="position()=last()">
						<xsl:value-of select="normalize-space(.)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(normalize-space(string(.)),' AND ')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
