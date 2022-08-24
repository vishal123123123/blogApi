package com.blogapi.config;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DateTimeConverter {

	 public Calendar stringToDateTime(String date) {
	        DateFormat formatter;
	        formatter = new SimpleDateFormat(AppConstants.DATE_TIME_FORMAT);
	        try {
	            Date date1 = (Date) formatter.parse(date);
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(date1);
	            return cal;
	        } catch (ParseException ex) {
	            log.info(ex.getMessage());
	        }
	        return null;
	    }
	 
	  public String calendarToString(Calendar date) {
	        SimpleDateFormat format1 = new SimpleDateFormat(AppConstants.DATE_FORMAT);
	        String formatted = format1.format(date.getTime());
	        return formatted;
	    }
}