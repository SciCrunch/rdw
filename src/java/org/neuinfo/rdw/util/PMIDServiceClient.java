package org.neuinfo.rdw.util;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import groovyx.net.http.URIBuilder;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class PMIDServiceClient {
	public static PMArticleInfo getCitationInfo(String pmid) throws Exception {

        HttpClient client = new DefaultHttpClient();
        
        URIBuilder builder = new URIBuilder(
                "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi");
       
        builder.addQueryParam("db", "PubMed");
        builder.addQueryParam("retmode", "xml");
        builder.addQueryParam("id", pmid);
        URI uri = builder.toURI();
        
        HttpGet httpGet = new HttpGet(uri);
        try {
            HttpResponse resp = client.execute(httpGet);
            HttpEntity entity = resp.getEntity();
            if (entity != null) {
                String s = EntityUtils.toString(entity);
               //  System.out.println(s);

                SAXBuilder saxBuilder = new SAXBuilder();
                Document doc = saxBuilder.build(new StringReader(s));
                Element rootNode = doc.getRootElement();
                if (rootNode == null) {
                    return null;
                }
                Element pae = rootNode.getChild("PubmedArticle");
                if (pae == null) {
                    return null;
                }
                Element mce = pae.getChild("MedlineCitation");
                if (mce == null) {
                    return null;
                }
                Element ac = mce.getChild("Article");
                if (ac == null) {
                    return null;
                }
                Element jc = ac.getChild("Journal");
                PMArticleInfo pmai = new PMArticleInfo();
                if (jc != null) {
                    String journal = jc.getChildText("Title");
                    System.out.println("Journal:" + journal);
                    pmai.journal = journal;
                }
                Element ai = ac.getChild("ArticleTitle");
                if (ai != null) {
                    String title = ac.getChildTextTrim("ArticleTitle");
                    System.out.println("Title:" + title);
                    pmai.title = title;
                }
                Element al = ac.getChild("AuthorList");
                if (al != null) {
                    List<?> authorEls = al.getChildren("Author");
                    for (Object o : authorEls) {
                    	Element authorEl = (Element) o;
                        String lastName = authorEl.getChildText("LastName");
                        String foreName = authorEl.getChildText("ForeName");
                        String author = foreName + " " + lastName;
                        System.out.println("Author:" + author);
                        pmai.addAuthor(author);
                    }
                }
                return pmai;
            }

        } finally {
            if (client != null) {
                client.getConnectionManager().shutdown();
            }
        }
        return null;
    }

    public static class PMArticleInfo {
        String journal;
        String title;
        List<String> authorList = new ArrayList<String>(2);

        public PMArticleInfo() {
        }

        public PMArticleInfo(String journal, String title) {
            this.journal = journal;
            this.title = title;
        }

        public void addAuthor(String author) {
            authorList.add(author);
        }

        public String getJournal() {
            return journal;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getAuthorList() {
            return authorList;
        }

        public boolean matches(String aJournal, String aTitle) {
            boolean ok = similarEnough(aTitle, title);
            if (ok && aJournal != null && journal != null) {
                return similarEnough(aJournal, journal);
            }
            return ok;
        }

        public static boolean similarEnough(String ref, String other) {
            ref = ref.toLowerCase();
            other = other.toLowerCase();
            if (ref.equals(other)) {
                return true;
            }
            if (ref.startsWith(other)) {
                return true;
            } else if (other.startsWith(ref)) {
                return true;
            }
            return false;
        }
    }
    
    public static void main(String[] args) throws Exception {
		
    	System.out.println( PMIDServiceClient.getCitationInfo("23898408"));
	}
}
