package com.nsb.skynet;

import java.io.PrintWriter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactsProvider 
{
	public static String contactdetails ;
		int i=0;
	   public ContactsProvider(Context context) 
		{
			    contactdetails="";
		        Uri contactsuri= ContactsContract.Contacts.CONTENT_URI;    
		        Cursor c= context.getContentResolver().query(contactsuri, null,null,null,null);

		       
		        contactdetails = "<table id=\"myContacts\"><thead><tr><th colspan=\"2\"></th></tr></thead><tfoot><tr>" +
		        		"<th colspan=\"2\"></th></tr></tfoot><tbody>";
		        
		        
		        while(c.moveToNext())
		    	  {
		        	i++;
		   		    int colfor_name= c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		    		int colfor_id= c.getColumnIndex(ContactsContract.Contacts._ID);
		        	int colfor_hasphone=c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		    			    		
		    		int id;
		    		String displayname;
		    		int hasphno;
		    		String phnno;
		    		
		    		displayname = c.getString(colfor_name);
		    		id = c.getInt(colfor_id);
		   		    hasphno = c.getInt(colfor_hasphone);
		   		    
		   		   
		    		if(hasphno==1)
		    		{
		    			contactdetails += "<tr>" +
		    			   		    "<td class=\"check\"><input id=\""+displayname.replace(" ","%20")+"\"   name=\"contacts\" type=\"checkbox\" value=\"1\" AUTOCOMPLETE=OFF /></td>";
		    			contactdetails += "<td>" +displayname +"  ";
		    			
		    			Uri uri2=ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		    			Cursor c2= context.getContentResolver().query(uri2 , null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+id , null,null);
		    		
		    			while(c2.moveToNext())
		    			{		    			   	
		    			   phnno= c2.getString(c2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		    			   contactdetails += "<span>"+phnno+"</span>" ;
		    			}
		    			contactdetails+="</td>";
		    			c2.close();		    			
		    			
		    		}
		    		contactdetails+="</tr>";
		    		
		    	 }
		        contactdetails+="</tbody></table>";
		        Log.d("nsb", contactdetails);
		
		 }
			
}	


