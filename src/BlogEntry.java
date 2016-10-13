import java.util.Comparator;
import java.util.Date;

public class BlogEntry implements Comparable<BlogEntry> {
    private String title ="";

    private String url ="";
    private String description = "";
    private String dateStr = "";
    private String blogName ="";
    private Date formattedDate;

    public BlogEntry(String blogName){
        this.blogName = blogName;
    }

    public String getDescription() {
        return description;
    }

    public void appendDescription(String text) {
        this.description = (this.description + text).trim();
    }

    public String getTitle() {
        return title;
    }

    public void appendTitle(String text){
        this.title = (this.title + text).trim();
    }

    public String getDateStr() {
        return dateStr;
    }

    public void appendDateStr(String text){
        this.dateStr = (this.dateStr + text).trim();
    }


    public String getUrl() {
        return url;
    }

    public void appendUrl(String text){
        this.url = (this.url + text).trim();
    }

    public String getBlogName() {
        return blogName;
    }

    public void appendBlogName(String text){
        this.blogName = (blogName + text).trim();
    }

    public void setFormattedDate(Date date){
        this.formattedDate = date;
    }

    public Date getFormattedDate(){
        return formattedDate;
    }

    @Override
    public int compareTo(BlogEntry o) {
        return -(this.formattedDate.compareTo(o.getFormattedDate()));
    }
}
