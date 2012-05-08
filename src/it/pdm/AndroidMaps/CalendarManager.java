package it.pdm.AndroidMaps;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarManager {

	
	public CalendarManager() {
	
	}
	
	static public Calendar getCalendar() {
		final Calendar c = Calendar.getInstance();
 
		// set current date into datepicker
		return c;
 
	}
	
	/* parte di parsing stringa datetime in formato YYYY/MM/DD HH:MM:SS */
	
	static String getYear(String datetime){
		String[] pieces = datetime.split(" "); //divido data da time
		String data=pieces[0];//prelevo solo la data
		
		pieces = data.split("/"); //prelevo le singole parti della data
		return pieces[0]; //restituisco l'anno
	}
	
	static String getMonth(String datetime){
		String[] pieces = datetime.split(" "); //divido data da time
		String data=pieces[0];//prelevo solo la data
		
		pieces = data.split("/"); //prelevo le singole parti della data
		return pieces[1]; //restituisco il mese
	}
	
	static String getDay(String datetime){
		String[] pieces = datetime.split(" "); //divido data da time
		String data=pieces[0];//prelevo solo la data
		
		pieces = data.split("/"); //prelevo le singole parti della data
		return pieces[2]; //restituisco il giorno
	}
	
	static String getHour(String datetime){
		String[] pieces = datetime.split(" "); //divido data da time
		String data=pieces[1];//prelevo solo l'ora
		
		pieces = data.split(":"); //prelevo le singole parti della data
		return pieces[0]; //restituisco le ore
	}
	
	static String getMinute(String datetime){
		String[] pieces = datetime.split(" "); //divido data da time
		String data=pieces[1];//prelevo solo l'ora
		
		pieces = data.split(":"); //prelevo le singole parti della data
		return pieces[1]; //restituisco i minuti
	}
	
	static String getSecond(String datetime){
		String[] pieces = datetime.split(" "); //divido data da time
		String data=pieces[1];//prelevo solo l'ora
		
		pieces = data.split(":"); //prelevo le singole parti della data
		return pieces[2]; //restituisco i secondi
	}
	
	
	
	//restituisce una stringa rappresentante la data di sistema
    static String getCurrentDate(){
		
    	final Calendar c = getCalendar();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
    	String result= day+"/"+(month+1)+"/"+year;
		
		return result;
		
	}
    
	//restituisce una stringa rappresentante l'ora di sistema
    static String getCurrentTime(){
		
    	final Calendar c = getCalendar();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		
    	String result= hour+":"+minute+":"+second;
		
		return result;
		
	}
    
  //restituisce una stringa rappresentante la data e l'ora di sistema
    static String getCurrentDateTime(){
    	
    	String result=getCurrentDate();
		
    	result+=" "+getCurrentTime();
    	
    	return result;
		
	}
	
    //formatta la stringa rappresentante la data in formato YYYY/MM/DD
	static String formatDateForDatabase(String date){
		
		String[] pieces = date.split("/");
		
		String day= pieces[0];
		String month= pieces[1];
		String year= pieces[2];
		
		String result= year+"/"+month+"/"+day;
		
		return result;
		
	}
	
	//formatta la stringa rappresentante l'ora in formato HH:MM:SS   --- non fa niente per ora
	static String formatTimeForDatabase(String time){
		
		String[] pieces = time.split(":");
		
		String second= pieces[2];
		String minute= pieces[1];
		String hour= pieces[0];
		
		String result= hour+":"+minute+":"+second;
		
		return result;
		
	}
	
	//formatta la stringa rappresentante la data e l'ora in formato YYYY/MM/DD HH:MM:SS
	static String formatDateTimeForDatabase(String datetime){
		
		String[] date_time = datetime.split(" ");
		
		String date= date_time[0];
		String time= date_time[1];
		
		date=formatDateForDatabase(date);
		time=formatTimeForDatabase(time);
		
		String result= date+" "+time;
		
		return result;
		
	}
	
	//trasforma la stringa rappresentante la datae l'ora da formato YYYY/MM/DD HH:MM:SS in DD/MM/YYYY HH:MM:SS
	static String readDateTime(String datetime){
		
		
		String hour= getHour(datetime);
		String minute= getMinute(datetime);
		String second= getSecond(datetime);
		String year= getYear(datetime);
		String month= getMonth(datetime);
		String day= getDay(datetime);
		
		month=("0"+month).substring(("0"+month).length()-2);
		day=("0"+day).substring(("0"+day).length()-2);
		hour=("0"+hour).substring(("0"+hour).length()-2);
		minute=("0"+minute).substring(("0"+minute).length()-2);
		second=("0"+second).substring(("0"+second).length()-2);
		
		String result= day+"/"+month+"/"+year+" "+hour+":"+minute+":"+second;
		
		return result;
		
	}
	
	//fa la comparazione tra due stringhe datatime
	static String compareDateTimes(String datetime1,String datetime2){
		
		/*return 'equal' if they are equal, return '>' if the first datetime is greater 
		 * than second, return '<' otherwise*/
	
		
		
		if(datetime1.equals(datetime2)){
			return "equal";
		}else{
			if(datetime1.compareTo(datetime2)>0){
				return ">";
			}else{
				return "<";
			}
		}
		
	}
	
	//calcola i giorni di differenza tra due date
	static double differenceDates(String date1,String date2){
		
		Integer hour= Integer.parseInt(getHour(date1));
		Integer minute= Integer.parseInt(getMinute(date1));
		Integer second= Integer.parseInt(getSecond(date1));
		Integer year= Integer.parseInt(getYear(date1));
		Integer month= Integer.parseInt(getMonth(date1));
		Integer day= Integer.parseInt(getDay(date1));
		
		GregorianCalendar cal1 = new GregorianCalendar(year, month, day, hour, minute, second);
		
		hour= Integer.parseInt(getHour(date2));
		minute= Integer.parseInt(getMinute(date2));
		second= Integer.parseInt(getSecond(date2));
		year= Integer.parseInt(getYear(date2));
		month= Integer.parseInt(getMonth(date2));
		day= Integer.parseInt(getDay(date2));
		
		GregorianCalendar cal2 = new GregorianCalendar(year, month, day, hour, minute, second);
		
		long date1Millis = cal1.getTimeInMillis();
		long date2Millis = cal2.getTimeInMillis();
		
		long difference = date1Millis - date2Millis;
		
		//1 giorno medio = 1000*60*60*24 ms
		// = 86400000 ms
		double giorniFraDueDate = Math.round( difference / 86400000.0 );
		
		return giorniFraDueDate;
	}
	
}
