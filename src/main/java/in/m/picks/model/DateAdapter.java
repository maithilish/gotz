package in.m.picks.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;

public class DateAdapter {

	public static Date parseDate(String s) {
		if (s == null) {
			return null;
		}
		return DatatypeConverter.parseDate(s).getTime();
	}

	public static String printDate(Date dt) {
		if (dt == null) {
			return null;
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);
		return DatatypeConverter.printDate(cal);
	}
}