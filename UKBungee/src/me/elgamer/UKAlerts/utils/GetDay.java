package me.elgamer.UKAlerts.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class GetDay {
	
	public static int getDay() {
		
		Date date = new Date();
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		
		return DayOfWeek.from(localDate).getValue();
		
	}

}
