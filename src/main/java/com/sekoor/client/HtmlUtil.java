package com.sekoor.client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Some methods to manipulate HTML
 */
public class HtmlUtil {

    public static Document transformToDoc(String html) {
       return Jsoup.parse(html);
    }

    public static boolean hasBody(Document doc) {
        return doc.body().hasText();
    }

    public static void addTitle(Document doc, String title) {
        doc.title(title);
    }

    public static String getHtmlAsString(Document doc) {
        return doc.outerHtml();
    }
}
