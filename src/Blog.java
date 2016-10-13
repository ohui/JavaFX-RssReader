//http://www.vogella.com/tutorials/RSSFeed/article.html
//http://www.xml.com/pub/a/2003/09/17/stax.html

import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Blog implements Comparable<BlogEntry>{
    private String blogTitle = null;
    private URL rssLink;
    private ArrayList<BlogEntry> entries;
    private String blogUrl = null;

    private boolean grabbedBlogInfo = false;

    String[] tagNames = {
            "item",
            "title",
            "link",
            "description",
            "pubdate"
    };

    final int IS_ITEM = 0;
    final int IS_TITLE = 1;
    final int IS_LINK = 2;
    final int IS_DESC = 3;
    final int IS_PUBDATE = 4;

    public Blog(String url) {

        entries = new ArrayList<BlogEntry>();

        try {
            this.rssLink = new URL(url);
            URLConnection conn = this.rssLink.openConnection();
            conn.connect();
        } catch (MalformedURLException e) {
            System.out.println("Error: Malformed RSS URL.");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Error: IO exception when opening URL for the first time.");
            throw new RuntimeException(e);
        }

    }

    private InputStream openStream(){
        try {
            return rssLink.openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String getBlogTitle(){
        return blogTitle;
    }

    public String getBlogUrl(){
        return blogUrl;
    }

    private int isDesiredTagName(String name, String[] tagNames) {
        return Arrays.asList(tagNames).indexOf(name.toLowerCase());
    }

    private void processContent(int tagIndex, String text, BlogEntry entry){

        switch (tagIndex){
            case IS_TITLE:
                if(!grabbedBlogInfo){
                    blogTitle = text;
                }else {
                    entry.appendTitle(text);
                }
                break;

            case IS_LINK:
                if(!grabbedBlogInfo){
                    blogUrl = text;
                } else {
                    entry.appendUrl(text);
                }
                break;

            case IS_DESC:
                if(grabbedBlogInfo){
                    entry.appendDescription(text);
                }
                break;

            case IS_PUBDATE:
                if(grabbedBlogInfo){
                    entry.appendDateStr(text);
                }
                break;

            default:
                break;
        }
    }

    public void parseRss(){

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory.createXMLStreamReader(openStream());

            int currentTagIndex = 0;
            BlogEntry currentEntry = null;

            int entryEditingCount = 0;

            for (int event = parser.next();
                 event != XMLStreamConstants.END_DOCUMENT;
                 event = parser.next()) {

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ((currentTagIndex = isDesiredTagName(parser.getLocalName(), tagNames)) >= 0) {
                            entryEditingCount++;
                            if(currentTagIndex == IS_ITEM){
                                grabbedBlogInfo = true;
                                currentEntry = new BlogEntry(blogTitle);
                            }
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if ((currentTagIndex = isDesiredTagName(parser.getLocalName(), tagNames)) >= 0) {
                            entryEditingCount--;
                            if(currentTagIndex == IS_ITEM){
                                entries.add(currentEntry);
                            }
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        if (entryEditingCount > 0) {
                            processContent(currentTagIndex, parser.getText(), currentEntry);
                        }
                        break;
                    case XMLStreamConstants.CDATA:
                        if (entryEditingCount > 0) {
                            processContent(currentTagIndex, parser.getText(), currentEntry);
                        }
                        break;
                    default:
                        break;
                } // end switch
            } // end while
            parser.close();

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<BlogEntry> getBlogEntries(){

        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

        //credit to http://stackoverflow.com/questions/2705548/parse-rss-pubdate-to-date-object-in-java

        for (BlogEntry entry: entries){
            try {
                entry.setFormattedDate(formatter.parse(entry.getDateStr()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return entries;
    }

    @Override
    public int compareTo(BlogEntry o) {
        return 0;
    }
}
