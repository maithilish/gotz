# Gotz ETL

Gotz ETL is a tool to extract data from HTML pages. In Java, one can scrape web pages with libraries such as JSoup and HtmlUnit. But, when one intents to extract huge amount of data from hundreds of pages as a dataset then task becomes daunting. Scraping libraries such as JSoup do well in scraping data but they are not meant to handle large set of data.

Gotz is built upon JSoup and HtmlUnit and some of the functionality offered by Gotz over and above the scrapping libraries are:

   - Gotz is completely model driven like a real ETL tool. Data structure, task workflow and pages to scrape are defined with a set of XML definition files and no coding is required
   - It can be configured to use either JSoup or HtmlUnit as scraper
   - Query can be written either using Selectors with JSoup or XPath with HtmlUnit
   - Gotz persists pages and data to database so that it recover from the failed state without repeating the tasks already completed
   - Gotz is a multithreaded application which process pages in parallel for maximum throughput. Threads alloted to each task pool is configureable based on workload
   - Allows to transform, filter and sort the data
   - Comes with built-in appenders such as FileAppender, DBAppender and ListAppender.
   - GotzEngine can be embeded in other programs and access scrapped data with ListAppender
   - Flexible workflow allows one to change sequence of steps
   - Gotz is extensible. Developers can extends the predefined base steps or even create new ones with different functionality and weave them in workflow

## Gotz Installation

To install and run Gotz ETL see [CodeTab Gotz Reference](http://www.codetab.org/gotz-etl/). It is also a step-by-step guide to create data definition files through a set of examples.
