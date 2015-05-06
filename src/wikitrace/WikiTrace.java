package wikitrace;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author UndeadScythes <udscythes@gmail.com>
 */
public class WikiTrace {
    private static final String[] EXCLUDE = {
        "/wiki/File:",
        "/wiki/Wikipedia:",
        "/wiki/Help:",
        "/wiki/Portal:",
        "/wiki/Category:",
        "/wiki/Talk:",
        "/wiki/Template:",
        "(disambiguation)"};
    private static final String[] INCLUDE = {" href=\"/wiki/"};
    
    private ArrayList<String> urls = new ArrayList();
    
    public static void main(String[] args) throws IOException, InterruptedException {
        WikiTrace tracer = new WikiTrace();
        //while(true) {
            String start = tracer.random();
            System.out.println("\n" + start.replace("/wiki/", "").replace("_", " ") + " ---> Philosophy in " + tracer.trace(start, "Philosophy", true) + " links");
        //}
    }
    
    private String random() throws IOException {
        String html;
        html = fetchHTML("http://en.wikipedia.org/wiki/Special:Random");
        int canonStart = html.indexOf("<link rel=\"canonical\" href=\"http://en.wikipedia.org/wiki");
        return html.substring(canonStart + 51, html.indexOf("\" />", canonStart));
    }
    
    private int trace(String start, String term, boolean titles) throws InterruptedException {
        System.out.print("Tracing...");
        if (titles) System.out.print("\n");
        int step = 0;
        String html;
        String url = start;
        String title = "";
        //while(!url.equals("/wiki/" + TERM)) {
        while(!title.equals(term)) {
            if (!titles) System.out.print(".");
            Thread.sleep(1000);
            try {
                html = fetchHTML("http://en.wikipedia.org" + url);
            } catch (IOException ex) {
                System.out.println("UNREADABLE URL");
                break;
            }
            title = html.substring(html.indexOf("<title>") + 7, html.indexOf("</title>") - 35);
            if (titles) System.out.println(title);
            int offset = html.indexOf("<div id=\"bodyContent\" class=\"mw-body-content\">");
            String anchor;
            do {
                int nextAnchor = html.indexOf("<a ", offset);
                offset = html.indexOf("</a>", nextAnchor);
                anchor = html.substring(nextAnchor, offset + 4);
                int urlStart = anchor.indexOf("href=\"") + 6;
                url = anchor.substring(urlStart, anchor.indexOf("\"", urlStart));
            } while (badLink(anchor) || urls.contains(url));
            urls.add(url);
            step++;
        }
        return step;
    }
    
    private String trace(String start, int steps, boolean titles) throws InterruptedException {
        int step = 0;
        String html;
        String url = start;
        //while(!url.equals("/wiki/" + TERM)) {
        while(step < steps) {
            Thread.sleep(5000);
            try {
                html = fetchHTML("http://en.wikipedia.org" + url);
            } catch (IOException ex) {
                System.out.println("UNREADABLE URL");
                break;
            }
            if (titles) System.out.println(html.substring(html.indexOf("<title>") + 7, html.indexOf("</title>") - 35));
            int offset = html.indexOf("<div id=\"bodyContent\" class=\"mw-body-content\">");
            String anchor;
            do {
                int nextAnchor = html.indexOf("<a ", offset);
                offset = html.indexOf("</a>", nextAnchor);
                anchor = html.substring(nextAnchor, offset + 4);
                int urlStart = anchor.indexOf("href=\"") + 6;
                url = anchor.substring(urlStart, anchor.indexOf("\"", urlStart));
            } while (badLink(anchor) || urls.contains(url));
            urls.add(url);
            step++;
        }
        return url;
    }
    
    private static boolean badLink(String link) {
        for (String exclude : EXCLUDE) {
            if (link.contains(exclude)) return true;
        }
        for (String include : INCLUDE) {
            if (!link.contains(include)) return true;
        }
        return false;
    }
    
    public String fetchHTML(String url) throws MalformedURLException, IOException {
        String html = "";
        URLConnection connection = new URL(url).openConnection();
        Scanner scanner = new Scanner(connection.getInputStream());
        scanner.useDelimiter("\\Z");
        html = scanner.next();
        return html;
    }
}
