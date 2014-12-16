package com.nsb.skynet;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSSending 
{
	public SMSSending(String resourceurl,Context context) 
	{
		Log.d("nsb", resourceurl);
		String phonenumber;
		String message;
	    StringTokenizer smstoken1=  new StringTokenizer(resourceurl,"=");
	    smstoken1.nextToken();
	    
	    StringTokenizer smstoken2=new StringTokenizer(smstoken1.nextToken(),"&");
	    phonenumber=smstoken2.nextToken();
	    Log.d("nsb", "Phone Number :"+phonenumber);
	   		
	    
	    //StringTokenizer smstoken3 = new StringTokenizer(smstoken1.nextToken()," ");
	    message=smstoken1.nextToken();
	    Log.d("nsb", "Message :"+message);
	    
	    message=message.replace("%20", " ");
	    message=message.replace("%0D%0A", "\n");
//	    message=message.replace("%21", "!");
//	    message=message.replace("%22", "\"");
//	    message=message.replace("%23", "#");
//	    message=message.replace("%24", "$");
//	    message=message.replace("%25", "%");      
//	    message=message.replace("%26", "&");	  
//	    message=message.replace("%27", "'");      
//	    message=message.replace("%28", "(");
//	    message=message.replace("%29", ")");              
//	    message=message.replace("%2A", "*");
//	    message=message.replace("%2B", "+");
//      message=message.replace("%2C", ",");
//      message=message.replace("%2F", "/");
//	    message=message.replace("%3F", "?");
	    
	    Log.d("nsb",message);
 
	    SmsManager smsmanager = SmsManager.getDefault();
	    
	    if(message.length()<=160)
	    {
	    	 smsmanager.sendTextMessage(phonenumber, null, message, null, null);
	    }
	    else
	    {
	    	ArrayList<String> messageparts = smsmanager.divideMessage(message); 
		    smsmanager.sendMultipartTextMessage(phonenumber, null, messageparts, null, null);
	    }   
	    
	    
	    ContentValues values = new ContentValues();       
	    values.put("address", phonenumber+""); 
	    values.put("body", message); 
	    context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
	}

}
