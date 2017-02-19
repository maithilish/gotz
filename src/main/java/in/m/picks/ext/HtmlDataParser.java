package in.m.picks.ext;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import in.m.picks.exception.DataDefNotFoundException;
import in.m.picks.exception.FieldNotFoundException;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.DataDef;
import in.m.picks.model.Member;
import in.m.picks.shared.DataDefService;
import in.m.picks.step.IStep;
import in.m.picks.util.Util;

public class HtmlDataParser extends HtmlParser {

	final static Logger logger = LoggerFactory.getLogger(HtmlDataParser.class);

	@Override
	public IStep instance() {
		return new HtmlDataParser();
	}

	@Override
	public Object parse() {
		try {
			parseData();
		} catch (DataDefNotFoundException | ScriptException
				| FieldNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void parseData() throws DataDefNotFoundException, ScriptException,
			FieldNotFoundException {
		DataDef dataDef = DataDefService.INSTANCE.getDataDef(dataDefName);
		for (Member member : data.getMembers()) {
			// collections.sort not possible as axes is set so implied sort
			// as value field of an axis may be referred by later axis
			for (AxisName axisName : AxisName.values()) {
				Axis axis = member.getAxis(axisName.toString());
				if (axis != null) {
					if (isDocumentLoaded()) {
						HtmlPage page = (HtmlPage) getDocument().getDocumentObject();
						String value = getValue(page, dataDef, member, axis);
						axis.setValue(value);
					}
				}
			}
		}
		Util.logState(logger, "parser-" + dataDefName, "Data after parse",
				getFields(), data);
	}

}
