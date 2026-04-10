package com.natamus.collective.functions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFunctions {
	public static String ymdhisToReadable(String ymdhis) {
		DateFormat ymdhisformat = new SimpleDateFormat("yyyyMMddhhmmss");
		DateFormat readableformat = new SimpleDateFormat("yyyy/MM/dd, hh:mm:ss");
		
		try {
			return readableformat.format(ymdhisformat.parse(ymdhis));
		} catch (ParseException e) {
			return ymdhis;
		}
	}
	
	public static String getNowInYmdhis() {
		Date now = new Date();
		return new SimpleDateFormat("yyyyMMddhhmmss").format(now);
	}
}
