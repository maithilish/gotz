package org.codetab.gotz.util;

import java.io.Console;
import java.io.File;
import java.io.IOException;

import org.codetab.gotz.messages.Messages;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * <p>
 * Find JSoup selector.
 * @author Maithilish
 *
 */
public final class FindSelector {

    /**
     * <p>
     * main method.
     * @param args
     *            command args
     */
    public static void main(final String[] args) {
        if (args.length < 2) {
            System.out.println(Messages.getString("FindSelector.0")); //$NON-NLS-1$
            System.exit(1);
        }
        String fileName = args[0];
        String selector = args[1];
        boolean inner = false;
        final int optionIndex = 3;
        if (args.length == optionIndex) {
            inner = Boolean.valueOf(args[2]);
        }

        FindSelector findSelector = new FindSelector();
        try {
            Elements elements = findSelector.parseHtml(fileName, selector);
            Console console = System.console();
            if (console == null) {
                return;
            }
            if (elements.size() > 0) {
                String output = findSelector.buildOutput(elements, inner);
                console.printf("%s", output); //$NON-NLS-1$
            } else {
                console.printf("%s", Messages.getString("FindSelector.3")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Parse HTML with JSoup.
     * @param fileName
     *            file name
     * @param selector
     *            selector
     * @return elements
     * @throws IOException
     *             if file error
     */
    public Elements parseHtml(final String fileName, final String selector)
            throws IOException {
        File file = new File(fileName);
        Document doc = Jsoup.parse(file, null);
        Elements elements = doc.select(selector);
        return elements;
    }

    /**
     * <p>
     * Build output from elements.
     * @param elements
     *            elements
     * @param inner
     *            only inner or outer HTML
     * @return string
     */
    public String buildOutput(final Elements elements, final boolean inner) {
        StringBuilder sb = new StringBuilder();
        sb.append(Util.LINE);
        sb.append(Messages.getString("FindSelector.1")); //$NON-NLS-1$
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        if (inner) {
            sb.append(elements.html());
        } else {
            sb.append(elements.outerHtml());
        }
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        sb.append(Messages.getString("FindSelector.5")); //$NON-NLS-1$
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        sb.append(elements.text());
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        sb.append(Messages.getString("FindSelector.6") + elements.size()); //$NON-NLS-1$
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        return sb.toString();
    }
}
