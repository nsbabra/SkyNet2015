package com.nsb.skynet;
import android.content.Context;
import android.database.*;
import android.net.Uri;
import android.util.Log;

public class MessageProvider 
{
       public static String messagedetails="";
		
       public MessageProvider(Context context) 
		{
    	    messagedetails="";
			Uri inboxuri= Uri.parse("content://sms/inbox"); 
				
		    Cursor c = context.getContentResolver().query(inboxuri, null,null,null,null);

		    
		    messagedetails = "<table id=\"myMessages\"><thead><tr><th colspan=\"2\"></th></tr></thead><tfoot><tr>" +
	        		"<th colspan=\"2\"></th></tr></tfoot><tbody>";
		    
		    
		    while(c.moveToNext())
			  {
				int colfor_body=c.getColumnIndex("body");
				int colfor_number= c.getColumnIndex("address");
				
				//int colfor_person =c.getColumnIndex("person");
				//Log.d("nsb", colfor_person+"Column");
				
				String msgbody,number,person;
				                      		
				msgbody= c.getString(colfor_body);
			    number= c.getString(colfor_number);
			    //person= c.getString(colfor_person);
			    
			    messagedetails += "<tr>";
			    
				
				messagedetails +=  "<td>"+number+"<span>"+msgbody+"</span></td>";
				messagedetails+="</tr>";
			  }	

		    messagedetails+="</tbody></table>" ;
		}

}


