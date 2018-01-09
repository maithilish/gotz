![gotz-banner](https://user-images.githubusercontent.com/12656407/34713923-9ea9a668-f51f-11e7-8fd9-7465f262fe12.png)

[![Build Status](https://travis-ci.org/maithilish/gotz.svg?branch=master)](https://travis-ci.org/maithilish/gotz)
[![Coverage Status](https://coveralls.io/repos/github/maithilish/gotz/badge.svg?branch=master)](https://coveralls.io/github/maithilish/gotz?branch=master&service=github)

Gotz Documentation <a href="http://www.codetab.org/gotz-etl/"> Gotz ETL Quickstart and Reference</a>

<hr>

Gotz ETL is a tool to extract data from HTML pages. In Java, one can scrape web pages with libraries such as JSoup and HtmlUnit. But, when one intents to extract huge amount of data from hundreds of pages as a dataset then task becomes daunting. Scraping libraries such as JSoup do well in scraping data but they are not meant to handle large set of data.

Gotz is built upon <a href="https://jsoup.org/">JSoup</a> and <a href="http://htmlunit.sourceforge.net/">HtmlUnit</a> and some of the functionality offered by Gotz over and above the scrapping libraries are:

   - Gotz is completely model driven like a real ETL tool. Data structure, task workflow and pages to scrape are defined with a set of XML definition files and no coding is required
   - It can be configured to use either JSoup or HtmlUnit as scraper
   - Query can be written either using Selectors with JSoup or XPath with HtmlUnit
   - Gotz persists pages and data to database so that it recover from the failed state without repeating the tasks already completed
   - For Transparent persistence, Gotz uses <a href="https://db.apache.org/jdo">JDO Standard</a> and <a href="http://www.datanucleus.org" >DataNucleus AccessPlatform</a> and you can choose your Datastore from a very wide range!
   - Gotz is a multithreaded application which process pages in parallel for maximum throughput. Threads alloted to each task pool is configureable based on workload
   - Allows to transform, filter and sort the data
   - Comes with built-in appenders such as FileAppender, DBAppender and ListAppender.
   - GotzEngine can be embeded in other programs and access scrapped data with ListAppender
   - Flexible workflow allows one to change sequence of steps
   - Gotz is extensible. Developers can extends the predefined base steps or even create new ones with different functionality and weave them in workflow

## Gotz Installation

To install and run Gotz ETL see [CodeTab Gotz Reference](http://www.codetab.org/gotz-etl/). It is also a step-by-step guide to create data definition files through a set of examples.
