<?xml version="1.0"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>
  <xsl:param name="tempVal" select="none"/>

  <xsl:template match="/">
    <HTML>
      <HEAD>
        <TITLE>
          <xsl:value-of select="@Name"/>
        </TITLE>

        <script type="text/javascript">
          var visible = 0;
          var FilterSettingsDetailsVisible = 0;
          var ScannerSettingsDetailsVisible = 0;
          function Show()
          {
          if(visible == 0)
          {
          window.document.getElementById("ID_1").style.display = "none";
          window.document.getElementById("ID_2").style.display = "block";
          visible = 1;
          }
          else
          {
          window.document.getElementById("ID_1").style.display = "block";
          window.document.getElementById("ID_2").style.display = "none";
          visible = 0;
          }
          }
          function ShowFilterSettingsDetails()
          {
          if(FilterSettingsDetailsVisible == 0)
          {
          window.document.getElementById("ID_3").style.display = "none";
          window.document.getElementById("ID_4").style.display = "block";
          FilterSettingsDetailsVisible = 1;
          }
          else
          {
          window.document.getElementById("ID_3").style.display = "block";
          window.document.getElementById("ID_4").style.display = "none";
          FilterSettingsDetailsVisible = 0;
          }
          }
          function ShowScannerSettingsDetails()
          {
          if(ScannerSettingsDetailsVisible == 0)
          {
          window.document.getElementById("ID_5").style.display = "none";
          window.document.getElementById("ID_6").style.display = "block";
          ScannerSettingsDetailsVisible = 1;
          }
          else
          {
          window.document.getElementById("ID_5").style.display = "block";
          window.document.getElementById("ID_6").style.display = "none";
          ScannerSettingsDetailsVisible = 0;
          }
          }
        </script>
      </HEAD>
      <BODY topmargin="0px" leftmargin="0px" bgcolor="#EEEEEE">
        <xsl:apply-templates select="Data"/>
      </BODY>
    </HTML>
  </xsl:template>

  <xsl:template match="Data">
    <xsl:apply-templates select ="Image/ImageDescription" />
    <xsl:apply-templates select ="Image/Attachment/HardwareSetting/ScannerSetting" />
    <xsl:apply-templates select ="Image/Attachment/HardwareSetting/FilterSetting" />
    <xsl:apply-templates select ="Image/Attachment/HardwareSetting/LDM_Block_Sequential" />
    <xsl:apply-templates select ="Image/Attachment/FRAPplus" />
    <xsl:apply-templates select ="Image/TimeStampList" />
    <xsl:apply-templates select ="Image/Attachment" />
  </xsl:template>

  <xsl:template name="break">
    <xsl:param name="text" select="//User-Comment"/>

    <xsl:comment>This inserts line breaks into the user description in place of line feeds</xsl:comment>

    <xsl:choose>
      <xsl:when test="contains($text, '&#xa;')">
        <xsl:value-of select="substring-before($text, '&#xa;')"/>
        <br/>
        <xsl:call-template name="break">
          <xsl:with-param name="text" select="substring-after($text,'&#xa;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="ImageDescription">
    <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
      <TR>
        <TD>
          <TABLE width="100%" align="center" border="0" cellspacing="0" cellpadding="3" bgcolor="#FFFFFF">
            <TR>
              <TD>
                <TABLE width="100%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
                  <TR>
                    <TD height="20" width="40%" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; padding: 5px;">
                      Image: <B>
                        <xsl:value-of select="Name"/>
                      </B>
                    </TD>
                    <TD width="30%"/>
                  </TR>
                  <TR>
                    <TD height="20" width="30%" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 5px;">
                      Size: <B>
                        <xsl:value-of select="Size"/>
                      </B>
                    </TD>
                  </TR>
                  <TR>
                    <TD width="20%" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 5px;">
                      File Location: <B>
                        <xsl:value-of select="FileLocation"/>
                      </B>
                    </TD>
                  </TR>
                  <TR>
                    <TD height="20" width="30%" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 5px;">
                      Start Time: <B>
                        <xsl:value-of select="StartTime"/>
                      </B>
                    </TD>
                  </TR>
                  <TR>
                    <TD height="20" width="30%" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 5px;">
                      End Time: <B>
                        <xsl:value-of select="EndTime"/>
                      </B>
                    </TD>
                  </TR>
                  <TR>
                    <TD height="20" width="30%" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 5px;">
                      Total Exposures: <B>
                        <xsl:value-of select="FrameCount"/>
                      </B>
                    </TD>
                  </TR>
                </TABLE>
              </TD>
              <TD align="center" valign="center" rowspan="2">
                <A href="http://www.confocal-microscopy.com/" target="about:blank">
                  <IMG src="LeicaLogo.jpg" border="0" alt="Leica Microsystems Heidelberg GmbH"/>
                </A>
              </TD>
            </TR>
          </TABLE>
        </TD>
      </TR>
    </TABLE>

    <xsl:if test ="//User-Comment != ' '">
      <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
        <TR>
          <TD>
            <TABLE topmargin="0" leftmargin="0" width="100%" align="center" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
              <TR style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; padding: 3px;">
                <TD colspan="2" width="35%">
                  <xsl:call-template name="break"/>
                </TD>
              </TR>
            </TABLE>
          </TD>
        </TR>
      </TABLE>
    </xsl:if>


    <HR width="98%"></HR>

    <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
      <TR>
        <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
          <b>Dimensions</b>
        </TD>
      </TR>
    </TABLE>
    <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
      <TR>
        <TD>
          <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
            <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: bold; color: 000000; padding: 3px;">
              <TD>Dimension</TD>
              <TD>Logical Size</TD>
              <TD>Physical Length</TD>
              <TD>Physical Origin</TD>
            </TR>

            <xsl:for-each select="Dimensions/DimensionDescription">
              <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                <TD>
                  <xsl:value-of select="@DimID"/>
                </TD>
                <TD>
                  <xsl:value-of select="@NumberOfElements"/>
                </TD>
                <TD>
                  <xsl:value-of select="@Length"/> &nbsp;<xsl:value-of select="@Unit"/>
                </TD>
                <TD>
                  <xsl:value-of select="@Origin"/> &nbsp;<xsl:value-of select="@Unit"/>
                </TD>
              </TR>
            </xsl:for-each>
          </TABLE>
        </TD>
      </TR>
    </TABLE>

  </xsl:template>

  <xsl:template match="TimeStampList">
    <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
      <TR>
        <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
          <b>Time Stamps:</b> &nbsp;



        </TD>
      </TR>
    </TABLE>
    <DIV ID="ID_1" style="display:block;">
      <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
        <TR>
          <TD>
            <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
              <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: bold; color: 000000; padding: 3px;">
                <TD>
                  Frame &nbsp; (<a href="javascript:Show()">Show All</a>)
                </TD>
                <TD>Relative Time (s)</TD>
                <TD>Absolute Time (h:m:s.ms)</TD>
                <TD>Date</TD>
              </TR>

              <xsl:for-each select="TimeStamp">
                <xsl:if test="not(position()!=1 and position()!=last())">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD>
                      <xsl:number value="position()" format="1 "/>
                    </TD>
                    <TD>
                      <xsl:value-of select="@RelativeTime"/>
                    </TD>
                    <TD>
                      <xsl:value-of select="@Time"/>.<xsl:value-of select="@MiliSeconds"/>
                    </TD>

                    <TD>
                      <xsl:value-of select="@Date"/>
                    </TD>

                  </TR>
                </xsl:if>
              </xsl:for-each>
            </TABLE>
          </TD>
        </TR>
      </TABLE>
      <BR></BR>
    </DIV>
    <DIV ID="ID_2" style="display:none;">
      <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
        <TR>
          <TD>
            <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
              <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: bold; color: 000000; padding: 3px;">
                <TD>
                  Frame &nbsp; (<a href="javascript:Show()">Show first + last</a>)
                </TD>
                <TD>Relative Time</TD>
                <TD>Absolute Time</TD>
                <TD>Date</TD>
              </TR>

              <xsl:for-each select="TimeStamp">

                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD>
                    <xsl:number value="position()" format="1 "/>
                  </TD>
                  <TD>
                    <xsl:value-of select="@RelativeTime"/>
                  </TD>
                  <TD>
                    <xsl:value-of select="@Time"/>.<xsl:value-of select="@MiliSeconds"/>
                  </TD>

                  <TD>
                    <xsl:value-of select="@Date"/>
                  </TD>


                </TR>
              </xsl:for-each>
            </TABLE>
          </TD>
        </TR>
      </TABLE>
    </DIV>
    <BR></BR>

  </xsl:template>



  <xsl:template match="HardwareSetting">
    <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="FRAPplus">

    <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
      <TR>
        <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
          <b>Bleach Settings</b>
        </TD>
      </TR>
    </TABLE>

    <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
      <TR>
        <TD>
          <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">

            <xsl:for-each select="Block_FRAP">

              <xsl:for-each select="Block_FRAP_Bleach_Info/LaserLineSettingArray">
                <xsl:for-each select="LaserLineSetting">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      Laser Line <xsl:value-of select="@AotfType"/>(<xsl:value-of select="@LaserLine"/>)
                    </TD>
                    <TD >
                      <xsl:value-of select="@IntensityShow"/> %
                    </TD>
                  </TR>
                </xsl:for-each>
              </xsl:for-each>

            </xsl:for-each>

            <xsl:for-each select="Block_FRAP_XT">

              <xsl:for-each select="Block_FRAP_XT_Bleach_Info/LaserLineSettingArray">
                <xsl:for-each select="LaserLineSetting">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      Laser Line <xsl:value-of select="@AotfType"/>(<xsl:value-of select="@LaserLine"/>)
                    </TD>
                    <TD >
                      <xsl:value-of select="@IntensityShow"/> %
                    </TD>
                  </TR>
                </xsl:for-each>
              </xsl:for-each>

            </xsl:for-each>

          </TABLE>
        </TD>
      </TR>
    </TABLE>

    <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
      <TR>
        <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
          <b>Pre/Post Settings</b>
        </TD>
      </TR>
    </TABLE>

    <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
      <TR>
        <TD>
          <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">

            <xsl:for-each select="Block_FRAP">

              <xsl:for-each select="Block_FRAP_PrePost_Info/LaserLineSettingArray">
                <xsl:for-each select="LaserLineSetting">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      Laser Line <xsl:value-of select="@AotfType"/>(<xsl:value-of select="@LaserLine"/>)
                    </TD>
                    <TD >
                      <xsl:value-of select="@IntensityShow"/> %
                    </TD>
                  </TR>
                </xsl:for-each>
              </xsl:for-each>

            </xsl:for-each>

            <xsl:for-each select="Block_FRAP_XT">

              <xsl:for-each select="Block_FRAP_XT_PrePost_Info/LaserLineSettingArray">
                <xsl:for-each select="LaserLineSetting">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      Laser Line <xsl:value-of select="@AotfType"/>(<xsl:value-of select="@LaserLine"/>)
                    </TD>
                    <TD >
                      <xsl:value-of select="@IntensityShow"/> %
                    </TD>
                  </TR>
                </xsl:for-each>
              </xsl:for-each>

            </xsl:for-each>

          </TABLE>
        </TD>
      </TR>
    </TABLE>

  </xsl:template>


  <xsl:template match="ScannerSetting">
    <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
      <TR>
        <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
          <b>Scanner Settings</b>
        </TD>
      </TR>
    </TABLE>
    <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
      <TR>
        <TD>
          <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
            <xsl:for-each select="ScannerSettingRecord">

              <xsl:if test="@Identifier='csScanMode'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblZoom'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblSizeZ'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; <xsl:value-of select="@Unit"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblSizeY'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; µm
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblSizeX'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; µm
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nFormatOutDimension'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; pixels
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nFormatInDimension'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; pixels
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nSections'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>


              <xsl:if test="@Identifier='dblStepSize'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; µm
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblVoxelZ'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; nm
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblVoxelY'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; nm
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblVoxelX'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; nm
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblVoxelVolume'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; nm³
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblPinhole'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; µm
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='dblPinholeAiry'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/> &nbsp;
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='bIsRoi'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">ROI Scan </TD>
                  <xsl:if test="@Variant='0'">
                    <TD >No</TD>
                  </xsl:if>
                  <xsl:if test="@Variant!='0'">
                    <TD >Yes</TD>
                  </xsl:if>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='eDirectional'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nBit'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>  &nbsp; bits
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nChannels'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='eSequentialMode'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='DelayTime'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nRepeatActions'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nAccumulation'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nLineAccumulation'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nAverageFrame'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nAverageLine'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='nLambdaSections'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='LambdaBeginBW'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='LambdaEndBW'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:if test="@Identifier='bUseChaserUVShutter'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseChaserVisibleShutter'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseMP2Shutter'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseMPShutter'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseSuperContVisibleShutter'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseUV405Shutter'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseUVShutter'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseVisibleShutter'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseFSOPOLight'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseCARSLight'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUsePumpLight'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>
              <xsl:if test="@Identifier='bUseStokesLight'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@Variant"/>
                  </TD>
                </TR>
              </xsl:if>

              <xsl:comment> show PMT used for lambda scan here </xsl:comment>

            </xsl:for-each>
          </TABLE>
        </TD>
      </TR>
    </TABLE>

    <DIV ID="ID_5" style="display:block;" >
      <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
        <TR>
          <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
            <b>Scanner Settings Details</b> &nbsp;
            (<a href="javascript:ShowScannerSettingsDetails()">Show</a>)
          </TD>
        </TR>
      </TABLE>
    </DIV>
    <DIV ID="ID_6" style="display:none;">
      <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
        <TR>
          <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
            <b>Scanner Settings Details</b> &nbsp;
            (<a href="javascript:ShowScannerSettingsDetails()">Hide</a>)
          </TD>
        </TR>
      </TABLE>
      <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
        <TR>
          <TD>
            <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">

              <xsl:for-each select="ScannerSettingRecord">
                <xsl:if test="@Description!=''">
                  <xsl:if test="not(contains(@Identifier, 'csLutName'))">
                    <xsl:if test="@Variant!=''">
                      <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                        <TD width="40%">
                          <xsl:value-of select="@Description"/>
                        </TD>
                        <TD >
                          <xsl:value-of select="@Variant"/>  &nbsp; <xsl:value-of select="@Unit"/>
                        </TD>
                      </TR>
                    </xsl:if>
                  </xsl:if>
                </xsl:if>
                <xsl:comment> show PMT used for lambda scan here </xsl:comment>

              </xsl:for-each>
            </TABLE>
          </TD>
        </TR>
      </TABLE>
    </DIV>
    
  </xsl:template>

  <xsl:template match="LDM_Block_Sequential">
    <xsl:for-each select="LDM_Block_Sequential_List/ATLConfocalSettingDefinition">

      <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
        <TR>
          <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
            <b>
              Sequential Setting Nr.<xsl:number value="position()" format="1 "/>
            </b>
          </TD>
        </TR>
      </TABLE>
      <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
        <TR>
          <TD>
            <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
              <xsl:for-each select="FilterWheel/Wheel">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@FilterType"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@FilterName"/>
                  </TD>
                </TR>
              </xsl:for-each>
              <xsl:for-each select="DetectorList/Detector">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Type"/> &nbsp; <xsl:value-of select="@Band"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@ActiveShow"/>, Gain: <xsl:value-of select="@GainShow"/>, Offset: <xsl:value-of select="@OffsetShow"/>
                  </TD>
                </TR>
                <xsl:if test="@CanDoTimeGate='1'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Type"/> Timegate (<xsl:value-of select="@TimeGateWavelength"/> nm)
                    </TD>
                    <TD >
                      <xsl:value-of select="@TimegateActiveShow"/>, PulseStart: <xsl:value-of select="@TimeGatePulseStart"/> ns, PulseEnd: <xsl:value-of select="@TimeGatePulseEnd"/> ns
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:for-each>
              <xsl:for-each select="AotfList/Aotf">
                <xsl:for-each select="LaserLineSetting">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      Laser Line <xsl:value-of select="@AotfType"/>(<xsl:value-of select="@LaserLine"/>)
                    </TD>
                    <TD >
                      <xsl:value-of select="@IntensityShow"/> %
                    </TD>
                  </TR>
                </xsl:for-each>
              </xsl:for-each>
            </TABLE>
          </TD>
        </TR>
      </TABLE>
    </xsl:for-each>

  </xsl:template>

  <xsl:template match="FilterSetting">
    <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
      <TR>
        <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
          <b>Hardware Settings</b>
        </TD>
      </TR>
    </TABLE>
    <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
      <TR>
        <TD>
          <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
            <xsl:for-each select="FilterSettingRecord">

              <xsl:if test="@ClassName='CTurret'">
                <xsl:if test="@Attribute!='OrderNumber'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>

              <xsl:if test="@ClassName='CFolderHardwareTree'">
                <xsl:if test="@Attribute='System_Number'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>

              <xsl:if test="@ClassName='CAotf'">

                <xsl:if test="@ObjectName='Visible AOTF'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; %
                    </TD>
                  </TR>
                </xsl:if>
                <xsl:if test="@ObjectName='UV AOTF'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; %
                    </TD>
                  </TR>
                </xsl:if>
                <xsl:if test="@ObjectName='UV Chaser AOTF'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; %
                    </TD>
                  </TR>
                </xsl:if>
                <xsl:if test="@ObjectName='Supercontinuum Visible AOTF'">
                  <xsl:if test="@Attribute='Intensity'">
                    <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                      <TD width="40%">
                        WLL <xsl:value-of select="@Description"/>
                      </TD>
                      <TD >
                        <xsl:value-of select="@Variant"/> &nbsp; %
                      </TD>
                    </TR>
                  </xsl:if>
                </xsl:if>

              </xsl:if>

              <xsl:if test="@ClassName='CDetectionUnit'">
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@Description"/>
                  </TD>
                  <xsl:choose>
                    <xsl:when test="@Attribute = 'VideoOffset'">
                      <TD >
                        <xsl:value-of select="@Variant"/> &nbsp; %
                      </TD>
                    </xsl:when>
                    <xsl:when test="@Attribute = 'HighVoltage'">
                      <TD >
                        <xsl:value-of select="@Variant"/> &nbsp;
                      </TD>
                    </xsl:when>
                    <xsl:when test="@Attribute = 'TimeGateWavelength'">
                      <TD >
                        <xsl:value-of select="@Variant"/> &nbsp; nm
                      </TD>
                    </xsl:when>
                    <xsl:when test="@Attribute = 'PulseStart'">
                      <TD >
                        <xsl:value-of select="@Variant"/> &nbsp; ps
                      </TD>
                    </xsl:when>
                    <xsl:when test="@Attribute = 'PulseEnd'">
                      <TD >
                        <xsl:value-of select="@Variant"/> &nbsp; ps
                      </TD>
                    </xsl:when>                    
                    <xsl:otherwise>
                      <TD >
                        <xsl:value-of select="@Variant"/>
                      </TD>
                    </xsl:otherwise>
                  </xsl:choose>
                </TR>
              </xsl:if>

              <xsl:if test="@ClassName='CSpectrophotometerUnit'">
                <xsl:if test="@Attribute = 'Bandwidth'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>

              <xsl:if test="@ClassName='CMicroscopeStand'">
                <xsl:if test="@Attribute = 'OpticalZoom'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>

              <xsl:if test="@ClassName='CScanCtrlUnit'">
                <xsl:if test="@Attribute = 'Speed'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; Hz
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>

              <xsl:if test="@ClassName='CRotator'">
                <xsl:if test="@Attribute='Scan Rotation'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; degrees
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>

              <xsl:if test="@ClassName='CFilterWheel'">
                <xsl:if test="@ObjectName='Excitation Beam Splitter FW'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>

              <xsl:if test="@ClassName='CLaser'">
                <xsl:if test="@Description='Power State'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@ObjectName"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
                <xsl:if test="@Description='Laser output power'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@ObjectName"/> (Power)
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> %
                    </TD>
                  </TR>
                </xsl:if>
                <xsl:if test="@Description='Bypass'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@ObjectName"/> (Bypass)
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>                
              </xsl:if>

              <xsl:if test="@ObjectName='AOM'">
                <xsl:if test="@Attribute='STED Output-Power'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='AOM'">
                <xsl:if test="@Attribute='Intensity'">
                  <xsl:choose>
                    <xsl:when test ="@Description='unused'"> </xsl:when>
                    <xsl:otherwise>
                      <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                        <TD width="40%">
                          <xsl:value-of select="@Description"/>
                        </TD>
                        <TD >
                          <xsl:value-of select="@Variant"/> &nbsp; %
                        </TD>
                      </TR>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='MP EOM'">
                <xsl:if test="@Attribute='Gain'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; %
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='MP EOM'">
                <xsl:if test="@Attribute='Offset'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; %
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='MP EOM'">
                <xsl:if test="@Attribute='Intensity'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='Excitation FW MP'">
                <xsl:if test="@Attribute='Filter'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='MP2 EOM'">
                <xsl:if test="@Attribute='Gain'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; %
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='MP2 EOM'">
                <xsl:if test="@Attribute='Offset'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/> &nbsp; %
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='MP2 EOM'">
                <xsl:if test="@Attribute='Intensity'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ObjectName='Excitation FW MP2'">
                <xsl:if test="@Attribute='Filter'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ClassName='CXYZStage'">
                <xsl:if test="@Attribute='XPos'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>
              <xsl:if test="@ClassName='CXYZStage'">
                <xsl:if test="@Attribute='YPos'">
                  <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                    <TD width="40%">
                      <xsl:value-of select="@Description"/>
                    </TD>
                    <TD >
                      <xsl:value-of select="@Variant"/>
                    </TD>
                  </TR>
                </xsl:if>
              </xsl:if>

              <xsl:comment> Multifunciton port (MFP) goes here </xsl:comment>
              <xsl:comment> Y Scan Actuator (POS) goes here </xsl:comment>

              <xsl:if test="@ClassName='CScanActuator'">
                <xsl:if test="@ObjectName='Z Scan Actuator'">
                  <xsl:if test="@Attribute='Position'">
                    <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                      <TD width="40%">
                        <xsl:value-of select="@Description"/>
                      </TD>
                      <TD >
                        <xsl:value-of select="@Variant"/> &nbsp; µm
                      </TD>
                    </TR>
                  </xsl:if>
                </xsl:if>
              </xsl:if>

              <xsl:comment> Several missing fields go here </xsl:comment>

            </xsl:for-each>

          </TABLE>

        </TD>
      </TR>
    </TABLE>

    <DIV ID="ID_3" style="display:block;" >
      <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
        <TR>
          <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
            <b>Hardware Settings Details</b> &nbsp;
            (<a href="javascript:ShowFilterSettingsDetails()">Show</a>)
          </TD>
        </TR>
      </TABLE>
    </DIV>
    <DIV ID="ID_4" style="display:none;">
      <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
        <TR>
          <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
            <b>Hardware Settings Details</b> &nbsp;
            (<a href="javascript:ShowFilterSettingsDetails()">Hide</a>)
          </TD>
        </TR>
      </TABLE>
      <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
        <TR>
          <TD>
            <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
              <xsl:for-each select="FilterSettingRecord">

                <xsl:if test="@ClassName='CLaser'">
                  <xsl:if test="@Description='Power State'">
                    <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                      <TD width="40%">
                        <xsl:value-of select="@ObjectName"/>
                      </TD>
                      <TD width="40%">
                        <xsl:value-of select="@Attribute"/>
                      </TD>                      
                      <TD >
                        <xsl:value-of select="@Variant"/>
                      </TD>
                    </TR>
                  </xsl:if>
                  <xsl:if test="@Description='Laser output power'">
                    <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                      <TD width="40%">
                        <xsl:value-of select="@ObjectName"/> (Power)
                      </TD>
                      <TD width="40%">
                        <xsl:value-of select="@Attribute"/>
                      </TD>
                      <TD >
                        <xsl:value-of select="@Variant"/> %
                      </TD>
                    </TR>
                  </xsl:if>
                </xsl:if>

                <xsl:if test="@ClassName!='CLaser'">
                  <xsl:if test="@Variant!=''">

                    <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                      <TD width="40%">
                        <xsl:value-of select="@Description"/>
                      </TD>
                      <TD width="40%">
                        <xsl:value-of select="@Attribute"/>
                      </TD>
                      <TD >
                        <xsl:value-of select="@Variant"/> &nbsp; 
                      </TD>
                    </TR>
                  </xsl:if>
                </xsl:if>


              </xsl:for-each>
            </TABLE>
          </TD>
        </TR>
      </TABLE>
    </DIV>
  </xsl:template>

  <xsl:template match="Attachment">
    <xsl:if test="@Name='Annotations'">
    <TABLE width="98%" align="center" border="0" cellspacing="5" cellpadding="5">
      <TR>
        <TD align="left" style="font-family: arial, helvetica; font-size: 8pt; font-weight: normal; color: 000000; padding: 3px;">
          <b>Bleach Points</b>
        </TD>
      </TR>
    </TABLE>
    <TABLE width="98%" align="center" border="0" cellspacing="0" cellpadding="5" bgcolor="#DDDAD7">
      <TR>
        <TD>
          <TABLE topmargin="0" leftmargin="0" width="100%" align="left" border="1" cellspacing="0" cellpadding="5" bgcolor="#FFFFFF">
            <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: bold; color: 000000; padding: 3px;">
              <TD width="40%">
                Name
              </TD>
              <TD >
                Position in X
              </TD>
              <TD >
                Position in Y
              </TD>
              <TD >
                Duration
              </TD>
            </TR>
            <xsl:for-each select="//Annotation">
              <xsl:if test="@type='1'">
                
                <TR style="font-family: arial, helvetica; font-size: 7pt; font-weight: normal; color: 000000; padding: 3px;">
                  <TD width="40%">
                    <xsl:value-of select="@name"/>
                  </TD>
                  <TD >
                    <xsl:value-of select="@transTransX"/> m
                  </TD>
                  <TD >
                    <xsl:value-of select="@transTransY"/> m
                  </TD>
                  <TD >
                    <xsl:value-of select="@bleachduration"/> <xsl:value-of select="@bleachdurationunit"/>
                  </TD>
                </TR>

              </xsl:if>
            </xsl:for-each>
          </TABLE>
        </TD>
      </TR>
    </TABLE>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>