package org.codetab.gotz.util;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public final class FindSelector {

    private FindSelector() {
    }

    public static void main(final String[] args) {
        if (args.length < 2) {
            System.out.println("Usage : Find <file> <selector> [inner]");
            System.exit(1);
        }
        try {
            File file = new File(args[0]);
            String selector = args[1];
            boolean inner = false;
            final int optionIndex = 3;
            if (args.length == optionIndex) {
                inner = Boolean.valueOf(args[2]);
            }
            Document doc = Jsoup.parse(file, null);
            Elements elements = doc.select(selector);
            printElements(elements, inner);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printElements(final Elements elements,
            final boolean inner) {
        if (elements.size() > 0) {
            System.out.println("");
            System.out.println("       --- HTML ---\n");
            if (inner) {
                System.out.println(elements.html());
            } else {
                System.out.println(elements.outerHtml());
            }
            System.out.println("");
            System.out.println("       --- Text ---\n");
            System.out.println(elements.text());
            System.out.println("");
        }
        System.out.println("Number of matched elements : " + elements.size());
    }
}
