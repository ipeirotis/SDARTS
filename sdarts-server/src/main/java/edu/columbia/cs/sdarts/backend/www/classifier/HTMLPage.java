package edu.columbia.cs.sdarts.backend.www.classifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;


class Result {
    public String url;
    public String anchorText;
    public String snippetText;

    public Result(String url, String a, String s) {
        this.url = url;
        this.anchorText = a;
        this.snippetText = s;
    }

}


public class HTMLPage {

    Vector links;
    StringBuffer  pageText;

    boolean gettingSnippet;
    boolean gettingAnchorText;
    boolean gettingURL;

    String currentURL;
    String currentAnchorText;
    String currentSnippet;

    URL pageurl;
    File cacheurl;

    public HTMLPage(URL pageurl) {
        this.pageurl = pageurl;
        this.cacheurl = null;
        this.gettingSnippet = false;
        this.gettingAnchorText = false;
        this.gettingURL = false;
        this.currentURL = new String();
        this.currentAnchorText = new String();
        this.currentSnippet = new String();
        this.links = new Vector();

        Vector links = extractLinkInfo(this.getDOM());
        if (links==null) links = new Vector();
    }


    public HTMLPage(URL pageurl, File cacheURL) {
        this.pageurl = pageurl;
        this.cacheurl = cacheURL;
        this.gettingSnippet = false;
        this.gettingAnchorText = false;
        this.gettingURL = false;
        this.currentURL = new String();
        this.currentAnchorText = new String();
        this.currentSnippet = new String();
        this.links = new Vector();

        Vector links = extractLinkInfo(this.getDOM());
        if (links==null) links = new Vector();
    }

    public int getNumberOfLinks() {
        return links.size();
    }

    public URL getLink(int i) {
        URL url = null;

        try {
            url =  new URL( ((Result) links.get(i)).url );
        }
        catch (Exception e) {
        }
        return url;
    }

    public String getAnchorText(int i) {
        String t = ((Result) links.get(i)).anchorText;

        return t;
    }

    public String getSnippet(int i) {
        String t = ((Result) links.get(i)).snippetText;

        return t;
    }

  public String getPageText() {
         return pageText.toString();
    }

    private Vector extractLinkInfo(Node node) {

        if (node == null) {
            return null;
        }

        int type = node.getNodeType();

        switch (type) {
        case Node.DOCUMENT_NODE:
        	pageText = new StringBuffer();
            extractLinkInfo(((Document) node).getDocumentElement());
			break;

        case Node.ELEMENT_NODE:

            String nodename = new String(node.getNodeName().toLowerCase());
            boolean recordit=false;

            if (nodename.equals("a")) {
				if (gettingSnippet) {
					//System.out.println("Added "+currentURL);

					Result res = new Result(currentURL, currentAnchorText, currentSnippet);
	                links.add(res);
				}
				//System.out.print("Starting recording <A ");

                gettingSnippet = false;
                currentAnchorText = new String();
                currentURL = new String();
                currentSnippet = new String();

				NamedNodeMap attrs = node.getAttributes();

				for (int i = 0; i < attrs.getLength(); i++) {
					if (attrs.item(i).getNodeName().toLowerCase().equals("href")) {
						//System.out.println("HREF="+attrs.item(i).getNodeValue());
						currentURL = attrs.item(i).getNodeValue();
						gettingAnchorText = true;
						break;
					}
				}
                if (currentURL.startsWith("http://")) {
                    recordit=true;
                }
                else if (currentURL.startsWith("mailto:")) {
                    ;
                }
                else if (currentURL.startsWith("ftp:")) {
                    ;
                }
                else if (currentURL.startsWith("javascript:")) {
                    ;
                }
                else { //Relative URL
                    String baseurl = new String();

                    if (currentURL.startsWith("/")) {
						baseurl = pageurl.getProtocol()+"://"+pageurl.getHost();
                    } else if (baseurl.endsWith("/")) {
						baseurl = pageurl.toString();
					} else {
						baseurl = pageurl.toString().substring(0,pageurl.toString().lastIndexOf('/')+1);
					}

					currentURL = baseurl + currentURL;
                }
                //URL checkUrl = null;
		        try {
		            URL checkUrl = new URL(currentURL);
		            if (checkUrl==null)
		            recordit=false;
		            else
		            recordit=true;
				}
				catch (Exception e) {
					//System.out.println("BOO! URL="+currentURL);
					recordit=false;
				}


			}

            NodeList children = node.getChildNodes();

            if (children != null) {
                int len = children.getLength();

                for (int i = 0; i < len; i++) {
                    extractLinkInfo(children.item(i));
                }
            }

            if (nodename.equals("a")) {
                gettingURL = false;
                gettingAnchorText = false;
				gettingSnippet = recordit;
				currentSnippet = new String();
			}

            break;

        case Node.TEXT_NODE:
            String text = node.getNodeValue();
            pageText.append(text);
            pageText.append(" ");

            if (gettingAnchorText) {
                currentAnchorText  += " "+text;
            }
            if (gettingSnippet) {
            	currentSnippet  += " "+text;
            }
            break;

        }
        return links;

    }

    public Document getDOM() {

        Tidy tidy = new Tidy();

        try {
            tidy.setXmlOut(true);
            tidy.setXHTML(true);
            tidy.setUpperCaseAttrs(true);
            tidy.setDocType("strict");
            tidy.setCharEncoding(Configuration.ASCII);
            tidy.setAltText("none");
            tidy.setQuoteNbsp(false);
            tidy.setDropFontTags(true);
            tidy.setDropEmptyParas(true);
            tidy.setDropFontTags(true);
            tidy.setMakeClean(true);
            tidy.setFixBackslash(true);
            tidy.setWord2000(true);
            tidy.setIndentContent(false);
            tidy.setSmartIndent(false);
            tidy.setRawOut(false);
            tidy.setQuiet(true);
            tidy.setNumEntities(true);

            tidy.setErrout(new PrintWriter(new ByteArrayOutputStream(), true));

            if (cacheurl!=null) {
            	return tidy.parseDOM(new FileInputStream(cacheurl), null);
			} else {
				return tidy.parseDOM(this.pageurl.openStream(), null);
			}
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //return null;
    }


}
