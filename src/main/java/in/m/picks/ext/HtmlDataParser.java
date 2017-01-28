package in.m.picks.ext;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import in.m.picks.exception.DataDefNotFoundException;
import in.m.picks.model.Axis;
import in.m.picks.model.AxisName;
import in.m.picks.model.DataDef;
import in.m.picks.model.Member;
import in.m.picks.shared.DataDefService;
import in.m.picks.step.IStep;

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
			System.out.println(data.toStringIds());
			System.out.println(document.getDocumentObject());
			for(Member member : data.getMembers()){
				System.out.println(member.traceMember());
			}
		} catch (DataDefNotFoundException | ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		return null;
	}

	public void parseData() throws DataDefNotFoundException, ScriptException {
		DataDef dataDef = DataDefService.INSTANCE.getDataDef(dataDefName);
		for (Member member : data.getMembers()) {
			// collections.sort not possible as axes is set so implied sort
			// as value field of an axis may be referred by later axis
			for (AxisName axisName : AxisName.values()) {
				Axis axis = member.getAxis(axisName.toString());
				if (axis != null) {					
					if (getDocument().isDocumentLoaded()) {
						HtmlPage page = (HtmlPage) getDocument().getDocumentObject();
						String value = getValue(page, dataDef, member, axis);
						axis.setValue(value);
					}
				}
			}
		}
	}

}
