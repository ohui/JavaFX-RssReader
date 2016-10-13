//I probably spent more time trying to style the entire thing because
//it was hard trying to find out where a color was coming from. It could be a
//border color or background color. etc


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Main extends Application {

    private Stage homeScreen;
    private VBox contentArea = new VBox(2);
    ArrayList<BlogEntry> allBlogEntries = new ArrayList<BlogEntry>();
    ArrayList<Blog> blogs = new ArrayList<>();
    ObservableList<String> blogNames = FXCollections.observableArrayList();
    ArrayList<BlogButtonMirror> blogButtonMirrors= new ArrayList<>();

    private final int MAX_BLOG_TITLE_LEN = 20;
    private final int MAX_ENTRY_TITLE_LEN = 80;
    private final int WEBVIEW_EXTRA_HEIGHT = 60;
    private final double SCROLL_SPEED = 0.025;
    private final int ESTIMATED_ITEM_HEIGHT = 48;
    private final int MAX_SIDEBAR_BLOG_NAME_LEN = 35;
    private final String fileName = "BlogRssLinks.txt";

    private String getStringWithinLimit(String str, int limit){

        if(str.length() > limit){
            return str.substring(0, limit-3) + "...";
        } else {
            return str;
        }
    }

    private String getPaddedTitle (String str, int limit){

        String format = "%-"+ limit + "s";

        if(str.length() > limit){
            return str.substring(0, limit-3) + "...";
        }else {
            return String.format(format, str);
        }
    }

    private String getHrefTitle(String title, String link){
        return "<h1><a href =\"" + link + "\">"+ title +"</a></h1>";
    }

    ArrayList<WebEnginePane> webEngines = new ArrayList<WebEnginePane>();
    private ScrollPane mainContentScrollArea = new ScrollPane();

    private class WebEnginePane{
        WebEngine engine;
        TitledPane pane;
        ScrollPane scroll;
        WebView webView;

        public WebEnginePane(WebEngine engine, TitledPane pane, ScrollPane scroll, WebView webView){
            this.engine = engine;
            this.pane = pane;
            this.scroll = scroll;
            this.webView = webView;
        }
    }

    private void updateWebViewHeight(WebEngine currEngine, int height){
        for (WebEnginePane engine: webEngines){
            if (currEngine == engine.engine){
                engine.pane.setMaxHeight(height);
                break;
            }
        }
    }

    private void updateContentArea(ArrayList<BlogEntry> entries){

        for(BlogEntry entry: entries) {

            WebView browser = new WebView();
            WebEngine webEngine = browser.getEngine();

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setContent(browser);
            scrollPane.setPadding(new Insets(0, 0 , 0, 30));

            String divStartTag = "<html><body ><div id=\"RssDiv\">";
            String divEndTag = "</div></body></html>";

            String changeAnchorLinksJS = "";

            String content = divStartTag + entry.getDescription() + divEndTag;

            webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    webEngine.executeScript("document.body.style.overflowY = \"hidden\"");
                    int height = (int) webEngine.executeScript("document.getElementById(\"RssDiv\").offsetHeight");
                    updateWebViewHeight(webEngine, WEBVIEW_EXTRA_HEIGHT + height);
                }
            });

            webEngine.loadContent(content, "text/html");
            TitledPane titledPane = new TitledPane("      " + getStringWithinLimit(entry.getBlogName(), MAX_BLOG_TITLE_LEN) + "   " + getStringWithinLimit(entry.getTitle(), MAX_ENTRY_TITLE_LEN), scrollPane);
            titledPane.setAnimated(false);
            titledPane.setExpanded(false);

            titledPane.addEventFilter(ScrollEvent.ANY, (x)->{

                if(x.getDeltaY() > 0 ){
                    if(mainContentScrollArea.getVvalue() != 0){
                        mainContentScrollArea.setVvalue(mainContentScrollArea.getVvalue() - SCROLL_SPEED);
                    } else{
                    }
                } else {
                    if(mainContentScrollArea.getVvalue() != 1){
                        mainContentScrollArea.setVvalue(mainContentScrollArea.getVvalue() + SCROLL_SPEED);
                    }
                }
            });
            webEngines.add(new WebEnginePane(webEngine, titledPane, scrollPane, browser));
            contentArea.getChildren().add(titledPane);
        }
    }

    private int getIndexOfBlogInSidebar(Button target) {
        int i = 0;
        for(BlogButtonMirror currPair : blogButtonMirrors){
            if(currPair.deleteBtn == target){
                blogButtonMirrors.remove(i);
                break;
            } else {
                i++;
            }
        }
        return i;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scanner input = null;

        try {
            input = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while(input.hasNext()){
            String link = input.nextLine();
            Blog blog = new Blog(link);
            blogs.add(blog);
            blog.parseRss();
            allBlogEntries.addAll(blog.getBlogEntries());
        }

        Collections.sort(allBlogEntries);
        updateContentArea(allBlogEntries);

        ScrollPane blogScroll = new ScrollPane();
        blogScroll.setStyle("-fx-background: white");
        blogScroll.getStyleClass().add("blog-scroll");
        VBox listOfBlogs = new VBox(5);
        listOfBlogs.getStyleClass().add("blog-list");

        for(Blog blog: blogs){
            HBox blogBox = new HBox(10);
            Button blogName = new Button (getPaddedTitle(blog.getBlogTitle(), MAX_SIDEBAR_BLOG_NAME_LEN));
            blogName.getStyleClass().add("blog-button");
            HBox.setHgrow(blogName, Priority.ALWAYS);

            blogName.setMaxWidth(Double.MAX_VALUE);

            blogName.setPadding(new Insets(6, 0, 0, 0));
            Button deleteBtn = new Button("X");
            deleteBtn.getStyleClass().add("delete-btn");

            deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    int target = getIndexOfBlogInSidebar(deleteBtn);
                    listOfBlogs.getChildren().remove(target);
                }
            });

            blogName.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    contentArea.getChildren().clear();
                    updateContentArea(blog.getBlogEntries());
                }
            });
            blogBox.getChildren().addAll(deleteBtn,blogName);
            blogNames.add(blog.getBlogTitle());
            listOfBlogs.getChildren().add(blogBox);
            blogButtonMirrors.add(new BlogButtonMirror(deleteBtn));
        }

        blogScroll.setContent(listOfBlogs);

        homeScreen = primaryStage;
        homeScreen.setTitle("RSS Reader in JavaFX");

        SplitPane splitPane = new SplitPane();

        //Sidebar setup
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(20, 20, 20, 15));

        Label AddSubsText = new Label("Add Subscription");
        HBox addSubHBox = new HBox(5);
        TextField newRssLink = new TextField();
        Button addNew = new Button ("+");
        addSubHBox.getChildren().addAll(newRssLink, addNew);

        Label homeLabel = new Label("home");
        sidebar.getChildren().addAll(homeLabel, AddSubsText, addSubHBox, blogScroll);
        sidebar.getStyleClass().add("sidebar");
        sidebar.prefHeightProperty().bind(homeScreen.heightProperty());


        //Main Content Area
        mainContentScrollArea.setStyle("-fx-background: white");
        mainContentScrollArea.setPrefSize(1000, 1000);
        mainContentScrollArea.setFitToWidth(true);
        contentArea.getStyleClass().add("vbox");
        contentArea.setPadding(new Insets(0, -5, 0, -25));
        mainContentScrollArea.setContent(contentArea);

        splitPane.getItems().addAll(sidebar, mainContentScrollArea);
        splitPane.setDividerPositions(0.25f, 0.75f);
        Scene scene = new Scene(splitPane, 1200, 800);
        scene.getStylesheets().add("style.css");

        homeScreen.setScene(scene);
        homeScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private class BlogButtonMirror{
        Button deleteBtn;
        public BlogButtonMirror(Button delete){
            this.deleteBtn = delete;
        }
    }
}

//http://feeds.feedburner.com/zenhabits
//http://feeds.gawker.com/lifehacker/full