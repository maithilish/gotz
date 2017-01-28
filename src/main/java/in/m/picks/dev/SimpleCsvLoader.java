package in.m.picks.dev;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.step.IStep;
import in.m.picks.step.Loader;

public class SimpleCsvLoader extends Loader {

	final static Logger logger = LoggerFactory.getLogger(SimpleCsvLoader.class);

	@Override
	public IStep instance() {
		return new SimpleCsvLoader();
	}

	@Override
	public Object fetchDocument(String url)
			throws Exception, MalformedURLException, IOException {
		List<CSVRecord> records = new ArrayList<CSVRecord>();

		Reader in = new FileReader(url);
		Iterable<CSVRecord> recs = CSVFormat.EXCEL.parse(in);
		for (CSVRecord rec : recs) {
			records.add(rec);
		}
		return records;
	}

}
