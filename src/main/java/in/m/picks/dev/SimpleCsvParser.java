package in.m.picks.dev;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.m.picks.model.Data;
import in.m.picks.model.Member;
import in.m.picks.shared.DataDefService;
import in.m.picks.step.IStep;
import in.m.picks.step.Parser;
import in.m.picks.util.AccessUtil;
import in.m.picks.util.Util;

public class SimpleCsvParser extends Parser {

	final Logger logger = LoggerFactory.getLogger(SimpleCsvParser.class);

	@Override
	public IStep instance() {
		return new SimpleCsvParser();
	}

	@Override
	public Object parse() throws Exception {
		@SuppressWarnings("unchecked")
		List<CSVRecord> records = (List<CSVRecord>) getDocument()
				.getDocumentObject();

		Data data = DataDefService.INSTANCE
				.getDataTemplate(AccessUtil.getStringValue(getAfields(), "datadef"));

		System.out.println(Util.getIndentedJson(data, true));

		int rowIndex = 0;
		for (CSVRecord record : records) {
			for (Member member : data.getMembers()) {
				int colIndex = member.getAxis("col").getIndex();
				String value = record.get(colIndex);
				Member copy = new Member(member);
				copy.getAxis("fact").setValue(value);
				copy.getAxis("row").setIndex(rowIndex);
				logger.trace("{}", copy.traceMember());
			}
			rowIndex++;
		}

		return null;
	}

}
