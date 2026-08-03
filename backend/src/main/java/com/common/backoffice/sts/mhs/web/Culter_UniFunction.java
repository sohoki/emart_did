package com.common.backoffice.sts.mhs.web;


public class Culter_UniFunction {

	
	public String week_return(String WeekNumber){
		String weekTxt = ""; 
		String week = "";
		if (WeekNumber.length() >0 ){
		   	String[] WeekSplit = WeekNumber.split(",");
		   	for (int i =0; i < WeekSplit.length; i ++){ 
		   		switch ( Integer.parseInt(WeekSplit[i])){
		   	       case 2: week = "월요일";
		   	               break;
		   		   case 3: week = "화요일";
		   		           break;
		   		   case 4: week = "수요일";
		   		           break;
		   		   case 5: week = "목요일";
		   		           break;
		   		   case 6: week = "금요일";
		   		           break;
		   		   case 7: week = "토요일";
		   		           break;
		   		   case 1: week = "일요일";
		   		           break;
		   		}
		   		weekTxt += week+",";
		   		
		   	}
		}		
		return weekTxt.substring(0, (weekTxt.length() -1));
	}
	public String day_convert(String dayInfo){
		String dayTxt = "";
		if (dayInfo.length() > 5){
			dayTxt = dayInfo.substring(0, 4) +"."+dayInfo.substring(4, 2)+"."+dayInfo.substring(6, 2);
		}
		return dayTxt;
	}
}
