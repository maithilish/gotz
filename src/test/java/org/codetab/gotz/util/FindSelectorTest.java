package org.codetab.gotz.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.jsoup.select.Elements;
import org.junit.Test;

/**
 * <p>
 * FindSelector tests.
 * @author Maithilish
 *
 */
public class FindSelectorTest {

    @Test
    public void testMain() {
        String[] args = new String[] {
                "src/test/resources/itest/itc/page/itc-bs.html",
                "table:contains(Sources Of Funds) tr:nth-child(1) > td:nth-child(2)"};
        FindSelector.main(args);
    }

    @Test
    public void testParseHtml() throws IOException {
        String fileName = "src/test/resources/itest/itc/page/itc-bs.html";
        String selector =
                "table:contains(Sources Of Funds) tr:nth-child(1) > td:nth-child(2)";
        FindSelector fs = new FindSelector();
        Elements elements = fs.parseHtml(fileName, selector);
        assertThat(elements.size()).isEqualTo(1);
        assertThat(elements.get(0).outerHtml()).isEqualTo("<td>Mar '16</td>");
    }

    @Test
    public void testBuildOutputInner() throws IOException {
        String fileName = "src/test/resources/itest/itc/page/itc-bs.html";
        String selector =
                "table:contains(Sources Of Funds) tr:nth-child(1) > td:nth-child(2)";
        FindSelector fs = new FindSelector();
        Elements elements = fs.parseHtml(fileName, selector);
        String actual = fs.buildOutput(elements, true);
        String expected = expectedOutput(true);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testBuildOutputOuter() throws IOException {
        String fileName = "src/test/resources/itest/itc/page/itc-bs.html";
        String selector =
                "table:contains(Sources Of Funds) tr:nth-child(1) > td:nth-child(2)";
        FindSelector fs = new FindSelector();
        Elements elements = fs.parseHtml(fileName, selector);
        String actual = fs.buildOutput(elements, false);
        String expected = expectedOutput(false);

        assertThat(actual).isEqualTo(expected);
    }

    public String expectedOutput(final boolean inner) {
        StringBuilder sb = new StringBuilder();
        sb.append(Util.LINE);
        sb.append("       --- HTML ---");
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        if (inner) {
            sb.append("Mar '16");
        } else {
            sb.append("<td>Mar '16</td>");
        }
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        sb.append("       --- Text ---");
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        sb.append("Mar '16");
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        sb.append("Number of matched elements : 1");
        sb.append(Util.LINE);
        sb.append(Util.LINE);
        return sb.toString();
    }

}
