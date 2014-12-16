package com.nsb.skynet;

import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.*;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast; 

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.BreakIterator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;



  
public class MainActivity extends Activity 
{   
	   TextView tv_ip,tv_devices; 
	   int i=0; 
	   MyHandler mh; 
	   EditText et_error;
	   android.text.ClipboardManager clipboard;
	   WifiManager wifimanager;
	   NotificationManager notificationmanager;
	   Button bt; 
	   Thread tmain;  
	   String devicesip=""; 
	   int notifid; 
	   boolean isserverup=true;
	   SkynetServer skn; 
	   
	    
	   String portnumberopened="";
	   
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        i=0; 
        setContentView(R.layout.main);
        tv_ip=(TextView)findViewById(R.id.tv_ipaddress);
        tv_devices =(TextView)findViewById(R.id.tv_connecteddevices);
        bt=(Button)findViewById(R.id.bt_server); 
        bt.setEnabled(false);
        tv_ip.setVisibility(0);
        
		notificationmanager = (NotificationManager)getSystemService("notification");
        clipboard = (android.text.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        wifimanager=(WifiManager) getSystemService(WIFI_SERVICE);
        //Toast.makeText(getBaseContext(), GlobalAppActivity.canrun+"<- On Create", Toast.LENGTH_SHORT).show();
        
        
        
        //--------------- Resume wala Logic ---------------------
        
     	System.gc();
    	GlobalAppActivity.canrun=true;
    	//Toast.makeText(getBaseContext(), GlobalAppActivity.canrun+"<-- On Resume", Toast.LENGTH_SHORT).show();
    	mh=new MyHandler();

        try 
        {
          InputStream ins=(getBaseContext().getResources().openRawResource(R.raw.webphone));
           
          //File folder = new File("mnt/sdcard/");
           
          File folder = Environment.getExternalStorageDirectory();
          //File folder = new File(device_path);
          folder = new File(folder.toString()+"/webphone");
          Log.d("nsb - folder", folder.toString());
          if(!folder.exists())
          { 
            folder.mkdir();
          }
 
          ZipInputStream zis = new ZipInputStream(ins);
          while(true)
          {
            ZipEntry ze = zis.getNextEntry();

            if(ze==null)
              break;

            String filename = ze.getName();
            Log.d("nsb","File Name : "+filename);
            File f = new File(folder+File.separator+filename);

            if(ze.isDirectory())
            {
               f.mkdir();
                Log.d("nsb","folder created "+ ze.getName());
            }
            else
            {
             FileOutputStream fos = new FileOutputStream(f);

            int len;
            byte b[] = new byte[10000];
            int r;

            while(true)
            {
              r = zis.read(b, 0, 10000);
              if(r==-1)
                break;
              fos.write(b,0,r);
            }
            fos.close();

                Log.d("nsb","file copied "+ f.getPath());
            }
          }
        }
        catch(Exception ex)
        {
          //ex.printStackTrace();
        	Log.d("nsb - exception", ex.toString());
        }
        
        
    	bt.setEnabled(true);
        
        //----------------------------------------------------
    }   
    
    public void go(View view)
    { 	      
       	tv_ip.setText("http://"+Formatter.formatIpAddress(wifimanager.getConnectionInfo().getIpAddress()));

        if(!(tv_ip.getText().equals("http://0.0.0.0")))
        { 
        	skn=new SkynetServer();
       	    tmain=new Thread(skn);
           	tmain.start();  
           	bt.setEnabled(false);
           	        	
           	  Toast.makeText(getApplicationContext(), "Server Up", Toast.LENGTH_LONG).show();
        	  //tv_ip.setVisibility(1);
           	  createNotification(view);
        }        
         else
        {
         tv_ip.setText("");
         Toast.makeText(getBaseContext(), "Connect to a WifiNetwork First", Toast.LENGTH_LONG).show();
        }
        
        tv_ip.append(":"+portnumberopened);       
    }
    
    @Override
    public void onBackPressed() 
    {
    	 new AlertDialog.Builder(this)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .setTitle("Exit SkyNet..!!")
         .setMessage("Are you sure ?")
         .setPositiveButton("Yes", new DialogInterface.OnClickListener()
          {
             public void onClick(DialogInterface dialog, int which) 
             {
              GlobalAppActivity.canrun=false;
              try
              {
                skn.sersock.close(); 
                GlobalAppActivity.ipsconnected.clear();
              }
              catch(Exception e)
              {
            	e.printStackTrace();
                //Toast.makeText(getApplicationContext() , "Exception -->" + e.toString() , Toast.LENGTH_LONG).show();
              }
              
              //Toast.makeText(getBaseContext(), GlobalAppActivity.canrun+"<-- Yes Pressed", Toast.LENGTH_SHORT).show();
            //------- Clearing Notifcation create by Notification Activity ---------------
              
              notifid=getIntent().getIntExtra("notifid",37);  // ("name",default);
              //NotificationManager notificationmananger = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
              notificationmanager.cancel(notifid);
        
          //---------------------------------------------------------------------------------
              MainActivity.this.finish();    
             } 

          })
     .setNegativeButton("No", null)
     .show();
    }
      
    @Override
    protected void onDestroy() 
    {
    	// TODO Auto-generated method stub
    	
    	
     //------- Clearing Notifcation create by Notification Activity ---------------
        
        notifid=getIntent().getIntExtra("notifid",37);  // ("name",default);
        notificationmanager.cancel(notifid);
  
    //---------------------------------------------------------------------------------
    	
        super.onDestroy();
    }
 
    @Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.skynet, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// TODO Auto-generated method stub
		switch(item.getItemId())
		{   
	  
		  case (R.id.Rate_SkyNet):
			  showGooglePlayActivity();
		      return true;
	
		  default:
		  return super.onOptionsItemSelected(item);
		}
	}

	private void showGooglePlayActivity() 
	{
		// TODO Auto-generated method stub
		 Intent intent = new Intent(Intent.ACTION_VIEW);
		    //Try Google play
		    intent.setData(Uri.parse("market://details?id=com.nsb.skynet"));
		    if (MyStartActivity(intent) == false) 
		    {
		        //Market (Google play) app seems not installed, let's try to open a webbrowser
		        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.nsb.skynet"));
		        if (MyStartActivity(intent) == false) 
		        {
		            //Well if this also fails, we have run out of options, inform the user.
		            Toast.makeText(this, "Could not open Android market, please install the market app.", Toast.LENGTH_SHORT).show();
		        }
		    }
	}
	
	private boolean MyStartActivity(Intent aIntent) {
	    try
	    {
	        startActivity(aIntent);
	        return true;
	    }
	    catch (ActivityNotFoundException e)
	    {
	        return false;
	    }
	}
    

	public void createNotification(View v)	
	{

		Notification notification =
				new Notification(R.drawable.ic_launcher, "SkyNet Server Up" , System.currentTimeMillis());
		
	
		
		Intent intent = new Intent(this,MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi=PendingIntent.getActivity(this, 0, intent, 0);	
		notification.setLatestEventInfo(this, tv_ip.getText()+"", "SkyNet Server is running", pi);
		notification.flags = Notification.FLAG_NO_CLEAR;		
		notificationmanager.notify(37, notification);	
		intent.putExtra("notifid", 37);		
	}
 
    class SkynetServer implements Runnable
    {
    	     ServerSocket sersock;
 	         Socket sock;
 	   
    		 public void run()
    		 {
    		 
    			try
    			{
    			 sersock = new ServerSocket(9999);
    			}
    			catch(Exception e)
    			{
    				try
    				{
    			      sersock = new ServerSocket(8888);
    				}
    				catch(Exception e1)
    				{
    					try
    					{
    						sersock = new ServerSocket(9090);
    					}
    					catch(Exception e2)
    					{
    						try
    						{
    							sersock = new ServerSocket(8080);
    						}
    						catch(Exception ef)
    						{
    						   //Toast.makeText(getApplicationContext(), "No free Port found..\n Exiting Now", Toast.LENGTH_LONG).show();
    						   isserverup=false;
    						   
    						}
    					}
    					
    				}
    			}
    			portnumberopened=sersock.getLocalPort()+"";
    			//Log.d("nsb",portnumberopened+"");
    			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);       			 
    			v.vibrate(300);
    		
    			Log.d("nsb", "Server Up");
    		
    	//	if(isserverup)
    	//	{
    	    try 
             {		
              while(GlobalAppActivity.canrun)
              {                 
         	   try
         	   {
            	sock = sersock.accept();
            	new HTTPHandler(sock);
            	Socket tempsock=sock;
            	int device_flag=1;
            	
            	for(String s:GlobalAppActivity.ipsconnected)
            	{
            		if(s.equals(tempsock.getInetAddress()+""))
            		{  device_flag=0;
            		   break;
            		}
            		else 
            			continue;
            		
            	}
            	if(device_flag==1)
            	{
            		//GlobalAppActivity.ipsconnected.add(tempsock.getInetAddress()+"");
            		devicesip = tempsock.getInetAddress()+"";
            		GlobalAppActivity.ipsconnected.add(devicesip);
            		mh.sendEmptyMessage(99);            		
            	}
            	
         	   }
         	   catch(Exception e)
         	   { 	
         		   Log.d("nsb","breaking loop");
         		   break;
         	   }
         	   if(i==0)
                {
           			v.vibrate(200); 
                }
         	   Log.d("nsb", "New Request");
         	   
         	   i=1;
             } 
            }
    		   catch (Exception e) 
            {
     		e.printStackTrace();
     	   }	
    	 //}//-- if of server up
    		 
        }//-- Run
      }
    
    
   public class HTTPHandler implements Runnable
   {
   Socket sock;
   Thread t; 
   File f;
   StringTokenizer st;
   DataOutputStream dos;
   DataInputStream dis;
   FileInputStream fis;
   long filesize;
   String resourceurl;
   String contenttype;
   String mimetype;
   
   String contentlengthforpost;
  
   String requestheader="";   //-------- For File Receiving 
   long receivedfilesize=0;   // ------- For File Receiving
   String filename="";		  //-------- For File Receiving 
   String folderpath="";	  //-------- For File Receiving 
     
    public HTTPHandler (Socket sock) 
    {
        this.sock=sock;
        t=new Thread(this);
        t.start();
        
    }
        
	public void run()
    {       
      try
      { 
    	  System.gc();
          dis = new DataInputStream(sock.getInputStream());
          dos = new DataOutputStream(sock.getOutputStream());
          String firstline = dis.readLine();                  //---- GET /<resource> HTTP/1.1
          //errorString = ""+firstline;
          //mh.sendEmptyMessage(99);
          if(firstline!=null)
          {
    	   st = new StringTokenizer(firstline);   
           Log.d("nsb",st.nextToken()+" - Token 1 ");   // Get or Post       
         
         resourceurl = st.nextToken();       
         Log.d("nsb",resourceurl+"- Resource URL ");
                     
         
        
         
         if(firstline.contains("/FileUploaded"))
         {      	 
        	 String filesizeline=dis.readLine();					// dos.writeBytes("Content-Length:"+filesize+"\r\n");      	   
	         receivedfilesize = Long.parseLong( 
	        		 filesizeline.substring(filesizeline.indexOf(":")+1 ,filesizeline.length()) );
	         
  	      	
    	     String filenameline=dis.readLine();  					//dos.writeBytes("Filename:"+filename+"\r\n");
    	     filename = filenameline.substring(filenameline.indexOf(":")+1,filenameline.length()) ;
    	     
    	     														
    	     String folderpathline=dis.readLine();					//dos.writeBytes("Path:"+folderpath+"\r\n");
    	     
    	     if(folderpathline.contains("default"))
    	     {
    	    	 folderpath = Environment.getExternalStorageDirectory()+"webphone/downloads";
    	    	 f=new File(folderpath);
    	    	 if(!f.exists())
    	    	 {
    	    		 f.mkdir();
    	    	 }
    	     }
    	     else
    	     { 
    	      folderpath = folderpathline.substring(folderpathline.indexOf(":")+1,folderpathline.length()) ;
    	      folderpath = folderpath.replace("%20", " ");
    	     }
    	     
    	     dis.readLine(); //--------- Reading Blank Line
         }    
        
         else   //--- Default Case for Reading Header ------
         {
            while(true)
            {
        	   String currentline=dis.readLine();                   	 
        	   requestheader = requestheader.concat("\n"+currentline); 
               if(currentline==null || currentline.equals(""))
               {
                  break;
               }
            } 
         }
         Log.d("nsb",requestheader);
         
         
//------------- File Uploader/Receiver ---------------------
    	 
        if(resourceurl.equals("/FileUploaded"))
    	 {    		 	 
    		 FileOutputStream fos=new FileOutputStream(new File(folderpath+filename));
    	     
    	      byte b[] = new byte[1024];
    	      long count = 0;
    	      int r;
    	      
    	      while(true)
    	      {
    	        r = dis.read(b,0,1024);
    	        if(r==-1)
    	        	continue;
    	        fos.write(b,0,r);
    	        count+=r;
    	        if(count==receivedfilesize)
    	        {
    	          break;
    	        }
    	      }
    	      fos.close(); 
    	      dis.close(); 	        
              dos.close(); 
	    }
    	 
   //---------------- File Uploader/Receiver Ends -----------------------
        
    	 
     
   
// ---------------------------------- Cookie & Password Checker ---------------------------------------------------        
         
      /*   if(resourceurl.contains("/checkauthentication"))				//  /checkauthenticationnumber?password=12345
         {		
        	 int size=Integer.parseInt(contentlengthforpost);
        	 contentlengthforpost=null;
        	 byte b[] = new byte[size];
             long count = 0;
             int r;
             String loginsentdata="";
             
             while(true)
             {
               r = dis.read(b,0,size);              
               count+=r;
               if(count==size)
               {
                 break;
               }
               
             }
             String passwordreceived=new String(b);
             
        	 Log.d("nsb",passwordreceived+"<- Data");
        	 
        	 if(passwordreceived.equals("password=nsbabra"))
        	 {
        		
        		 loginsentdata="Login Successful<br>";
                 loginsentdata += "<a href=\"./\">Continue</a>\r\n";
        		 
                
        		 dos.writeBytes("HTTP/1.1 200 OK\r\n");
    			 dos.writeBytes("Content-Type: text/html\r\n"); 
    			 //dos.writeBytes("Set-Cookie: var=100; Expires="+expirytime+"\r\n");      //<----------------- Cookie Added
    			 dos.writeBytes("Content-Length: "+loginsentdata.length()+"\r\n");
                 dos.writeBytes("Connection: Close\r\n");
                 dos.writeBytes("\r\n");
              
        	 }
        	 else
        	 {
        		 loginsentdata="Incorrect Password\n" +
        		 			"<a href=\"./\">Try Again</a>\r\n";
        		 dos.writeBytes("HTTP/1.1 200 OK\r\n");
    			 dos.writeBytes("Content-Type: text/html\r\n"); 
    			 dos.writeBytes("Content-Length: "+loginsentdata.length()+"\r\n");
                 dos.writeBytes("Connection: Close\r\n");
                 dos.writeBytes("\r\n");
                
        	 }
        	 
        	 dos.writeBytes(loginsentdata);
             dos.flush();
             dis.close();
             dos.close();
        	 
         }*/
         
   //----------------------- Check Login Ends ----------------------------------------      
   
    // if(checkCookie(requestheader))   
    // {   
  //-------------------------- Root Page/Login -----------------------------------------      
     
         if(resourceurl.equals("/"))
         {
        //	  f = new File("/mnt/sdcard/webphone/pages/index.html");
        	  
        	  f= new File(Environment.getExternalStorageDirectory()+"/webphone/pages/index.html");
        	  fis=new FileInputStream(f);
			  filesize = f.length(); 
			
   		      dos.writeBytes("HTTP/1.1 200 OK\r\n");
			  dos.writeBytes("Content-Type: text/html\r\n");          
			  dos.writeBytes("Content-length: "+filesize+"\r\n");
              dos.writeBytes("Connection: Close\r\n");
              dos.writeBytes("\r\n");
           
              byte b[] = new byte[1024];    
              long count = 0;
              int r;
              while(true)
              {
                r = fis.read(b,0,1024);
                if(r==-1)
                {
                  continue;
                }   
                dos.write(b,0,r);
                count+=r;
                if(count==filesize)
                {
                   break;
                }
             }
           
           fis.close();
           dos.flush();      
           dos.close(); 
                    
        }
  
 //--------------------- Root Page Ends ---------------------------
         
         
  //----------------------Get Applications -----------------------------
         
         if(resourceurl.contains("getApps"))
         {
           
           String htmldata = "<table id=\"myApps\"><thead> <tr> <th>Installed Applications</th> <th colspan=\"2\"></th> </tr> </thead><tfoot><tr>" +
  	        		"<th colspan=\"2\"></th></tr></tfoot><tbody>";
           
           Intent intent = new Intent(Intent.ACTION_MAIN, null);
           intent.addCategory(Intent.CATEGORY_LAUNCHER);
           PackageManager packagemanager = getBaseContext().getPackageManager();
           
           List<ResolveInfo> list = packagemanager.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
           
           for (ResolveInfo rInfo : list) 
           {
            htmldata +="<tr><td id=\""+rInfo.activityInfo.applicationInfo.packageName.toString()+"\" onclick=\"uninstallApp(this.id)\">"
            			+rInfo.activityInfo.applicationInfo.loadLabel(packagemanager).toString()+
            		"<span>"+rInfo.activityInfo.applicationInfo.packageName.toString() + "<span></td></tr>";
           } 
           

           htmldata +="</tbody></table>";
            
		   
		   dos.writeBytes("HTTP/1.1 200 OK\r\n");
		   dos.writeBytes("Content-Type: text/html\r\n");          
		   dos.writeBytes("Content-length: "+htmldata.length()+"\r\n");
           dos.writeBytes("Connection: Close\r\n");
           dos.writeBytes("\r\n");
           
           dos.writeBytes(htmldata);     
           
           dos.flush();      
           dos.close();
          
         }
         
         else if(resourceurl.contains("uninstallApp"))
         {
        	 String packagename= resourceurl.substring(resourceurl.indexOf("uninstallApp?name=")+18);
        	// errorString=packagename;
        	 //mh.sendEmptyMessage(99);
        	 Uri packageURI = Uri.parse("package:"+packagename);
     		 Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
     		 startActivity(uninstallIntent);
     		 
     		dos.writeBytes("HTTP/1.1 204 No Content\r\n");
            dos.writeBytes("Connection: Close\r\n");
            
            dos.flush();      
            dos.close();
         }
         
          
         
         
         
  //-----------------------Get Applications Ends-------------------------
        
         
/* ------------------------------------------- Contacts --------------------------------------------------   */         
         
 //---------------------------- Create a Contact ---------------------------------
         
         else if(resourceurl.contains("createContact"))
         {
                                   //     /createContact?name=abcdef&number=1234
     	    String contactname=resourceurl.substring(resourceurl.indexOf("createContact?name=")+19,resourceurl.indexOf("&"));
        	contactname=contactname.replace("%20"," ");
        	     Log.d("nsb", "Contact Name :"+contactname);
        	     
        	String contactnumber=resourceurl.substring(resourceurl.indexOf("&number=")+8);	     
        	contactnumber=contactnumber.replace("%2B", "+");
        	     Log.d("nsb", "Contact Numebr :"+contactnumber);
        	     
        	ContentValues contact = new ContentValues();
   	        contact.put(People.NAME, contactname);
   	        Uri insertUri = getContentResolver().insert(People.CONTENT_URI, contact);
   	      
   	        Uri phoneUri = Uri.withAppendedPath(insertUri, People.Phones.CONTENT_DIRECTORY);
   	        contact.clear();
   	      
   	        contact.put(People.Phones.TYPE, People.TYPE_MOBILE);
   	        contact.put(People.NUMBER, contactnumber);
   	        Uri updateUri = getContentResolver().insert(phoneUri, contact);
        	 
        	 
        	 dos.writeBytes("HTTP/1.1 204 No Content\r\n");
             dos.writeBytes("Connection: Close\r\n");
             dos.writeBytes("\r\n");
             
             dos.close();
         }
        		 

  //---------------------------- Contact Creation Ends---------------------------------

         
  //---------------------------- Contacts Provider -------------------------------------
         
         else if(resourceurl.contains(("getContacts")))
    	 {    		 
   
        	   
    		  new ContactsProvider(getApplicationContext()); 
    		  

     		  dos.writeBytes("HTTP/1.1 200 OK\r\n");
 			  dos.writeBytes("Content-Type: text/html\r\n");          
 			  dos.writeBytes("Content-length: "+ContactsProvider.contactdetails.length()+"\r\n");
 			  Log.d("nsb",ContactsProvider.contactdetails.length()+"");
              dos.writeBytes("Connection: Close\r\n");
              dos.writeBytes("\r\n");
             
              dos.writeBytes(ContactsProvider.contactdetails);
              
              
              dos.flush();      
              dos.close(); 
	     }
         
         
         else if(resourceurl.contains("deleteContact"))
         {				//  ./deleteContact?name=abc,def,ghi
           boolean isdeleted=true; 	 
           String deletecontactstring = resourceurl.substring(resourceurl.indexOf("name=")+5);
       	   StringTokenizer stdelcontacttoken = new StringTokenizer(deletecontactstring,",");
        	
       	   
          Uri contactUri= ContactsContract.Contacts.CONTENT_URI; 
          while(stdelcontacttoken.hasMoreTokens())
          {
        	String currentcontact=stdelcontacttoken.nextToken().replace("%20", " ");  
        	Cursor cur = getBaseContext().getContentResolver().query(contactUri, null, null, null, null);   
 		    try 
 		    {	
 		    	if (cur.moveToFirst()) 
		        {
 		          do 
 		            {		            	
 		        	 if (cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase((currentcontact)))
 		            	  {
 		                    String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
 		                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
 		                    getBaseContext().getContentResolver().delete(uri, null, null);
 		                  }
 		            	
 		            } while (cur.moveToNext());
		        }
 		    } 
 		    catch (Exception e) 
 		    {	
 		    	isdeleted =false;  
 		    	
 		    }
          }
          if(isdeleted)
          {
            dos.writeBytes("HTTP/1.1 204 No Content\r\n");
            dos.writeBytes("Connection: Close\r\n");
            dos.writeBytes("\r\n");
            dos.close();
          }
          else
          {
        	      dos.writeBytes("HTTP/1.1 200 Ok\r\n");
		    	  dos.writeBytes("Content-Type: text/html\r\n");          
	 			  dos.writeBytes("Content-length: 3\r\n");
		          dos.writeBytes("Connection: Close\r\n");
		          dos.writeBytes("\r\n");
		          
		          dos.writeBytes("Not");
		          dos.close(); 
          }
         }
        	 
    	    
 //--------------------------- Contacts Provider Ends ------------------------------------
         

/* ---------------------------  Contacts Ends ------------------------------------------ */
         
 
         
/* ------------------------------ Messages ----------------------------------- */ 
         
         
     //---------------- Messages Provider ------------------
    	 
         else if(resourceurl.equalsIgnoreCase(("/getMessages")))
    	 {  
       		 new MessageProvider(getApplicationContext());
       		 
     		 dos.writeBytes("HTTP/1.1 200 OK\r\n");
 			 dos.writeBytes("Content-Type: text/plain\r\n");          
 			 dos.writeBytes("Content-length: "+MessageProvider.messagedetails.length()+"\r\n");
             dos.writeBytes("Connection: Close\r\n");
             dos.writeBytes("\r\n");
             
             dos.writeBytes(MessageProvider.messagedetails);     
             
             dos.flush();      
             dos.close();
          	
    	}
         
    //-------------------Message Provider Ends---------------------  	 
   	
    //------------------- delete Messages ---------------------
        else if(resourceurl.contains("deleteMessage"))
    	{  
        	Uri inboxUri = Uri.parse("content://sms/inbox");
        	boolean msgdeleted=true;
        	Cursor c = getBaseContext().getContentResolver().query(inboxUri , null, null, null, null);
        	if(c.moveToFirst())
        	{
        	  do
        	  {
        	    try 
        	    {
        	        // Delete the SMS
        	        String pid = c.getString(0);   // Get id;
        	        String uri = "content://sms/" + pid;
        	        getApplicationContext().getContentResolver().delete(Uri.parse(uri),
        	                null, null);
        	                	        
        	    } 
        	    catch (Exception e) 
        	    {
        	    	msgdeleted =false;        	    	
        	    }
        	  }
        	  while(c.moveToNext()); 
        	  
        	  if(msgdeleted)
        	  {
        	    dos.writeBytes("HTTP/1.1 204 No Content\r\n");
                dos.writeBytes("Connection: Close\r\n");    
        	  }
        	  else
        	  {
        		  dos.writeBytes("HTTP/1.1 200 OK\r\n");
      			  dos.writeBytes("Content-Type: text/plain\r\n");          
      			  dos.writeBytes("Content-length: 3\r\n");
                  dos.writeBytes("Connection: Close\r\n");
                  dos.writeBytes("\r\n");
                  
                  dos.writeBytes("Not");                                          
        	  }      	  
        	}
        	 dos.flush();      
             dos.close();
        	
    	}
     
    //------------------- delete Messages ends--------------------- //     
    	 
  // -------------------- Sending SMS Logic ---------------------------
    	 
         else if(resourceurl.contains("sendMessage"))
    	 {  
    		    new SMSSending(resourceurl,getBaseContext());			
    		    dos.writeBytes("HTTP/1.1 204 No Content\r\n");
                dos.writeBytes("Connection: Close\r\n");
                dos.writeBytes("\r\n");
             
                dos.flush();      
                dos.close();
     	 }
    //----------------------- Sending Logic Ends -------------------------
         
         
/* ----------------------------- Messages Ends----------------------------------- */  
         
         
         //---------------------- Clipboard Access --------------------------
         
         else if(resourceurl.contains("getClipboard"))
         {	 
        	 String cliptext="";
        	 if(clipboard.getText()!=null)
        	 {
        	  cliptext =clipboard.getText()+"";
        	 }
        	       	 
        	 dos.writeBytes("HTTP/1.1 200 OK\r\n");
 		     dos.writeBytes("Content-Type: text/html\r\n");          
 		     dos.writeBytes("Content-length: "+cliptext.length()+"\r\n");
             dos.writeBytes("Connection: Close\r\n");
             dos.writeBytes("\r\n");
             dos.writeBytes(cliptext);
             
             dos.flush();
             dos.close();
        	 
         }
         else if(resourceurl.contains("setClipboard"))
         {
        	 String cliptext=resourceurl.substring(resourceurl.indexOf("setClipboard?text=")+18);
        	 cliptext=cliptext.replace("%20", " ");
        	 cliptext=cliptext.replace("%0D%0A", "\n");
        	 clipboard.setText(cliptext+"");
        	 
        	 dos.writeBytes("HTTP/1.1 204 No Content\r\n");
             dos.writeBytes("Connection: Close\r\n");
             dos.writeBytes("\r\n");
          
             dos.flush();      
             dos.close();
         }
         
       //---------------------- Clipboard Access Ends--------------------------
         
    //-------------------------------- Music Player ---------------------------------
         
         else if(resourceurl.contains("getMusic"))
         {
        	 String htmldata = "<table id=\"myMusic\"><thead><tr><th></th><th colspan=\"5\"></th> </tr> </thead><tfoot><tr>" +
   	        		"<th colspan=\"5\"></th></tr></tfoot><tbody>";
        	
        	 String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        	 String[] projection = {
        	    MediaStore.Audio.Media.DATA,
        	 	MediaStore.Audio.Media.TITLE,
        	 	MediaStore.Audio.Media.ARTIST,
        	 	MediaStore.Audio.Media.ALBUM,
        	 	MediaStore.Audio.Media.DURATION
        	 };

        	 Cursor cursor = getBaseContext().getContentResolver().query(
        		        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        		        projection,
        		        selection,
        		        null,
        		        null);
        	 
        	if(!(cursor.equals(null)) )
            { 
        	   while(cursor.moveToNext())
        	   { 
        		 long duration=0;  
        		 try
        		 {
        		  duration=(Long.parseLong(cursor.getString(4))/1000);
        		 }
        		 catch(Exception e)
        		 {
        			 duration=0;
        			 continue;
        		 }
        	     long minutes=0;
        		 String secs="";
        		 while(duration>=60)
        		 {
        			 minutes++;
        			 duration-=60;
        		 }
        		 if(duration<10)
        		 {
        			 secs="0"+duration;
        		 }
        		 else
        		 {
        			 secs =duration+"";
        		 }
        		 htmldata+="<tr>" +
			   		    "<td class=\"check\"><img src=\"../.images/music_list.png\" width=\"20\" height=\"20\"></td>" +
			   		    "<td width=\"300\" id=\""+cursor.getString(0)+"\" onclick=\"playMusic(this.id)\">"+cursor.getString(1)+"</td>" +			 
	   		    		
			   		    "<td id=\""+cursor.getString(1)+"\" width=\"80\"><span>"+cursor.getString(2)+"</span></td>" +
			   		    "<td width=\"130\"><span>"+cursor.getString(3)+"</span></td>" +
			   		   
			   		    "<td><span>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp"+minutes+":"+secs+"</span></td>" +
			   		    "</tr>";
        	   }
            }

        	 htmldata +="</tbody></table>";
        	 
        	 
        	 filesize = htmldata.length();
        	   
             dos.writeBytes("HTTP/1.1 200 OK\r\n");
  		     dos.writeBytes("Content-Type: text/html\r\n");     
  		     dos.writeBytes("Content-length: "+filesize+"\r\n");
             dos.writeBytes("Connection: Close\r\n");
             dos.writeBytes("\r\n");

             
             dos.writeBytes(htmldata);
             
             dos.flush();
             dos.close();
        	 
            
         }
                  
    //------------------------------Music Player Ends ------------------------------------------
         
         
//-------------------------------- Video Player ---------------------------------
         
         else if(resourceurl.contains("getVideo"))
         {
        	 String htmldata = "<table id=\"myVideo\"><thead><tr><th></th><th colspan=\"4\"></th> </tr> </thead><tfoot><tr>" +
   	        		"<th colspan=\"4\"></th></tr></tfoot><tbody>";
        	
        	

        	 String[] projection = {
        			 //MediaStore.Video.Media._ID,
        			 MediaStore.Video.Media.DATA,
        			 MediaStore.Video.Media.DISPLAY_NAME,
        			 MediaStore.Video.Media.SIZE,
        			 MediaStore.Video.Media.DURATION
        	 };

        	 Cursor cursor = getBaseContext().getContentResolver().query(
        			    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        		        projection,
        		        null,
        		        null,
        		        null);
        	 
        	if(!(cursor.equals(null)) )
            { 
        	   while(cursor.moveToNext())
        	   { 
        		 long duration=(Long.parseLong(cursor.getString(3))/1000);
        		 long minutes=0;
        		 String secs="";
        		 while(duration>=60)
        		 {
        			 minutes++;
        			 duration-=60;
        		 }
        		 if(duration<10)
        		 {
        			 secs="0"+duration;
        		 }
        		 else
        		 {
        			 secs =duration+"";
        		 }
        		 
        		 DecimalFormat df=new DecimalFormat(".#");
        		 double videosize=Double.parseDouble(cursor.getString(2));
        		 String sizecategory="";
        		 if(videosize>=1024*1024*1024)
                 {
           	      videosize = (videosize/1024/1024/1024.0) ;
           	      sizecategory="Gb";
                 }
                 else if(videosize>=1024*1024)
                 {
                	 videosize = (videosize/1024/1024.0) ;
           	      sizecategory="Mb";
                 }
                 else if(videosize>=1024)
                 {
                	 videosize = (videosize/1024.0) ;
           	      sizecategory="Kb";
                 }
                 else
                 {
           	      sizecategory ="Bytes";
                 }
        		 
                 String vsize=df.format(videosize);
        		 
        		 htmldata+="<tr>" +
			   		    "<td class=\"check\"><img src=\"../.images/video_list.png\" width=\"20\" height=\"20\"></td> "+
			   		    "<td width=\"400\" id=\""+cursor.getString(0)+"\" onclick=\"playVideo(this.id)\">"+cursor.getString(1)+"</td>" +		 	   		    
			   		    "<td width=\"150\" ><span>"+minutes+":"+secs+"</td>" +
			   		    "<td><span>"+vsize+" "+sizecategory+"</span></td></tr>";
        	   }
            }

        	 htmldata +="</tbody></table>";
        	  
        	 
        	 filesize = htmldata.length();
        	   
             dos.writeBytes("HTTP/1.1 200 OK\r\n");
  		     dos.writeBytes("Content-Type: text/html\r\n");     
  		     dos.writeBytes("Content-length: "+filesize+"\r\n");
             dos.writeBytes("Connection: Close\r\n");
             dos.writeBytes("\r\n");

             
             dos.writeBytes(htmldata);
             
             dos.flush();
             dos.close();
        	 
            
         }
                  
    //------------------------------------Video Player Ends---------------------------------- 
         
    //-------------------------------- Get Images Viewer ---------------------------   
         
         else if(resourceurl.contains("getImages"))
         {
        	 System.gc();
        	 int index = Integer.parseInt(resourceurl.substring(resourceurl.indexOf("getImages?index=")+16));
        	 int numofpics=0;
        	 //errorString = index+" <- Index";
        	 //mh.sendEmptyMessage(99);
        	 String[] projection = new String[]{
        	            MediaStore.Images.Media._ID,
        	            MediaStore.Images.Media.DATA,
        	            MediaStore.Images.Media.TITLE
        	    };
        	 
        	    Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        	    Cursor cur = getBaseContext().getContentResolver().query(imagesUri,
        	            projection, 
        	            "",         
        	            null,       
        	            null              	            
        	            );

        	    File tempfolder = new File(Environment.getExternalStorageDirectory()+"/webphone/thumbnails");
        	    File imgthumb ;
        	    FileOutputStream fosimg = null;
        	    
        	    if(!(tempfolder.exists()))
	            {
        	    	tempfolder.mkdir();
	            }
	        	

        	    String htmldata = "<table id=\"myImages\"><thead><tr><th></th><th colspan=\"5\"></th> </tr> </thead><tfoot><tr>" +
       	        		"<th colspan=\"5\"></th></tr></tfoot><tbody>";
        	    
        	    if(cur.moveToFirst())
        	    {
        	    	while(cur.moveToNext())
        	    	 numofpics++;	
        	    }
        	   if(numofpics<index || index<0)
        	   {
        		index=0;   
        	   }
        	    if(cur.moveToPosition(index))
        	    {
        	    	int columncount=5;
        	    	htmldata+="<tr>";
        	    	int countimg=0;
        	        do 
        	        {               	                     
        	          countimg++;
        	          if(countimg==21)
        	          {
        	        	break;  
        	          }
        	          imgthumb = new File(Environment.getExternalStorageDirectory()+"/webphone/thumbnails/"+cur.getString(2) );
        	          if(!(imgthumb.exists()))
        	         {
        	            try
            	        {	 
        	             fosimg = new FileOutputStream(imgthumb);
            	        }
       	                catch(Exception e)
       	                {
       	        	      continue;
       	                }
        	            
        	            FileInputStream stream = new FileInputStream(cur.getString(1));
        	            
        	            //Bitmap bmpthumb = BitmapFactory.decodeStream(stream, null, null);
        	            
        	            BitmapFactory.Options options = new BitmapFactory.Options();
        	            //options.inSampleSize=1;
        	            //options.inJustDecodeBounds = true;
        	            //BitmapFactory.decodeStream(stream, null, options);
        	            
        	            //final int REQSIZE=80;
        	            
        	            //int scale=1;
        	            
        	           // while(options.outWidth/scale/2>=REQSIZE && options.outHeight/scale/2>=REQSIZE)
        	           // 	scale*=2;
        	            
        	           // BitmapFactory.Options option2 = new BitmapFactory.Options();
        	            //option.inSampleSize=scale;
        	            
        	            //options.inJustDecodeBounds = false;
        	            options.inSampleSize=8;
        	            Bitmap bmpthumb= BitmapFactory.decodeStream(stream, null, options);
        	            bmpthumb = ThumbnailUtils.extractThumbnail(bmpthumb, 70, 70);
        	            			
        	            			
        	            
        	            
        	            stream.close();
        	            stream=null;
        	              
        	            
        	            bmpthumb.compress(Bitmap.CompressFormat.PNG, 20, fosimg); 
        	            fosimg.close();        	           
        	          }
        	          String hypelinkthumb = imgthumb.getAbsolutePath().replace(" ","%20");
        	          
        	        	
        	          
        	            htmldata+="<td><img class=\"imagelist\" src=\""+hypelinkthumb+"\" id=\""+cur.getString(1)+"\"  " +
        	            		"width=\"110\" height=\"80\" alt=\""+cur.getString(2)+"\" onclick=\"viewImage(this.id)\" /></td>";
        	            columncount--;
        	        	if(columncount==0)
        	        	{
        	        		htmldata+="</tr><tr>";
        	        		columncount=5;
        	        	}
        	        } 
        	        while (cur.moveToNext());
        	        
       	        
        	        htmldata +="</tr></tbody></table>";
              	  
               	 
               	    filesize = htmldata.length();
               	   
                    dos.writeBytes("HTTP/1.1 200 OK\r\n");
         		    dos.writeBytes("Content-Type: text/html\r\n");     
         		    dos.writeBytes("Content-length: "+filesize+"\r\n");
                    dos.writeBytes("Connection: Close\r\n");
                    dos.writeBytes("\r\n");

                    
                    dos.writeBytes(htmldata);
                    dos.close();
                    
                   
                    System.gc();
                    
        	    }
  	 
         }
         
         else if(resourceurl.contains("viewImage"))
         {
        	 String imgpath=resourceurl.substring(resourceurl.indexOf("name=")+5);
        	 
        	 
        	 String htmldata="<img src=\""+imgpath+"\" width=\"696\" height=\"392\">";
        	 
        	 filesize = htmldata.length();
         	   
             dos.writeBytes("HTTP/1.1 200 OK\r\n");
  		     dos.writeBytes("Content-Type: text/html\r\n");     
  		     dos.writeBytes("Content-length: "+filesize+"\r\n");
             dos.writeBytes("Connection: Close\r\n");
             dos.writeBytes("\r\n");

             
             dos.writeBytes(htmldata);
             
             dos.close();
        	 
        	 
         }
         
         
         
    //--------------------------------- Image Viewer Ends --------------------------     
         
         
    //-------------------------- Open Browser --------------------------------
         else if(resourceurl.contains("openBrowser"))   //-  /openBrowser?url=urladdress
         {
        //	 String url = resourceurl.substring(resourceurl.indexOf("?url=")+5);
        	// errorString = "URL --> "+resourceurl.substring(resourceurl.indexOf("?url=")+5) ;
        	 //mh.sendEmptyMessage(99);
        	 Intent browserintent = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse("http://"+resourceurl.substring(resourceurl.indexOf("?url=")+5)));
        	 browserintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	 getBaseContext().startActivity(browserintent);
         }
    //------------------------------Browser Open Ends--------------------------      
         
        
    //------------------------- Deletion of Files ------------------------
         
         else if(resourceurl.contains("deleteFile"))
         {   
        	  
        
         
        	  String deletefilestring = resourceurl.substring(resourceurl.indexOf("filename=")+9);
        	  StringTokenizer stdelfiletoken = new StringTokenizer(deletefilestring,",");
      	      
        	  while(stdelfiletoken.hasMoreTokens())
        	  {
      	    
        	   String filepath = stdelfiletoken.nextToken();
        	   
   	           filepath = filepath.replace("%20"," ");
      	      
   	           File filetodelete = new File(filepath);
      	      
   	            DeleteRecursive(filetodelete);
   	         
        	  }
      	       dos.writeBytes("HTTP/1.1 204 No Content\r\n");
               dos.writeBytes("Connection: Close\r\n");
               dos.writeBytes("\r\n");
                 
               dos.flush();      
               dos.close();  
               
               
               
               
         }
         
           
    	 
  //-----------------------File Deletion Ends ---------------------------- 
         
         
         
  // ----------------- Creating Folder Logic -----------------------------       
         
         
         else if(resourceurl.contains("createFolder"))         // ./createFolder?name=/mnt/ext_card/Abc	
         {    
        	 
        	  String foldertocreatename = resourceurl.substring(resourceurl.indexOf("name=")+5);
        	        
        	  foldertocreatename = foldertocreatename.replace("%20"," ");
        	  File dir = new File(foldertocreatename);
        	
              try
              {
                if(dir.mkdir())
                {
                   dos.writeBytes("HTTP/1.1 204 No Content");
            	   dos.writeBytes("Connection: Close\r\n");
                   dos.writeBytes("\r\n");
                } 
                else 
                {
             	   dos.writeBytes("HTTP/1.1 200 Ok\r\n");
             	   dos.writeBytes("Content-Type: text/plain\r\n");          
       		 	   dos.writeBytes("Content-length: 3\r\n");
                   dos.writeBytes("Connection: Close\r\n");
                   dos.writeBytes("\r\n");
                   dos.writeBytes("Not");
                }
              } 
              catch(Exception e)
              {
            	 dos.writeBytes("HTTP/1.1 404 Not Found\r\n");
                 dos.writeBytes("Connection: Close\r\n");
                 dos.writeBytes("\r\n");             
              }
            
         }
         
   // ---------------------- Creating Folder Ends -------------------------      
         
  //------------------------ Files Manager --------------------------------
    	 
    	 
     else if(resourceurl.contains("/mnt"))
     {  		         	 
        	  resourceurl=resourceurl.replace("%20"," ");      
        		if(resourceurl.endsWith("/"))
        		    resourceurl=resourceurl.substring(0, resourceurl.length()-1);  		   
        		f=new File(resourceurl);
        		String parentfilepath=f.getParent().replace(" ", "%20");
        // ------------------- Folder Logic -----------------------
         
        		
         if(f.isDirectory())
         {   
        	File[] filesindir = f.listFiles();
            String sizecategory="";  //--- For Files
      	   
            DecimalFormat df = new DecimalFormat(".#"); 
            String tempforfolder = f.getAbsolutePath().replace(" ", "%20");


            String htmldata = "<table id=\"myFileManager\"><thead><tr><th colspan=\"5\"></th><th></th><th></th></tr></thead><tfoot><tr>" +
	        		"<th colspan=\"5\"></th></tr></tfoot><tbody>";
 	

      	   if(filesindir!=null)  
      	   {
      		   double dsize=0.0; 
      		   
                       
                for(File tempfile:filesindir)
                {
      	           String hyperlinkid = tempfile.getAbsolutePath().replace(" ", "%20");
      	         if(resourceurl.contains("mnt/sdcard")) 
      	              htmldata+="<tr>" +
      	              "<td class=\"check\"><input name=\"filesint\" id=\""+hyperlinkid+"\" type=\"checkbox\" value=\"1\" AUTOCOMPLETE=OFF /></td>";
      	         
      	         if(resourceurl.contains("mnt/ext_card"))
      	        	htmldata+="<tr>" +
            	              "<td class=\"check\"><input name=\"filesext\" id=\""+hyperlinkid+"\" type=\"checkbox\" value=\"1\" AUTOCOMPLETE=OFF /></td>";
      	        	 
      	           if(tempfile.isFile())
      	           {
      	        	 filesize=tempfile.length();
                     
                     if(filesize>=1024*1024*1024)
                     {
               	      dsize = (filesize/1024/1024/1024.0) ;
               	      sizecategory="GBs";
                     }
                     else if(filesize>=1024*1024)
                     {
               	      dsize = (filesize/1024/1024.0) ;
               	      sizecategory="MBs";
                     }
                     else if(filesize>=1024)
                     {
               	      dsize = (filesize/1024.0) ;
               	      sizecategory="KBs";
                     }
                     else
                     {
               	      dsize = filesize ;
               	      sizecategory ="Bytes";
                     }  
      	        	   
      	        	        	
      	        	 
      	        	 if(tempfile.getName().endsWith(".jpg") || tempfile.getName().endsWith(".JPG") ||      	        	    
      	        	    tempfile.getName().endsWith("jpeg") || tempfile.getName().endsWith("JPEG") ||      	        	   
      	        	    tempfile.getName().endsWith("png") || tempfile.getName().endsWith("png") ||      	        	   
      	        	    tempfile.getName().endsWith("bmp") || tempfile.getName().endsWith("BMP") )
      	        	 		
      	        		 htmldata+="<td><img src=\"../.images/image_list.png\" width=\"20\" height=\"20\"></td>";
      	        	 
      	        	 else if(tempfile.getName().endsWith("mp3") || tempfile.getName().endsWith("MP3") ||
      	        			 tempfile.getName().endsWith("wav") || tempfile.getName().endsWith("WAV") ||
      	        			 tempfile.getName().endsWith("m4a") || tempfile.getName().endsWith("M4A") ||
      	        			 tempfile.getName().endsWith("acc") || tempfile.getName().endsWith("ACC") ) 
      	        	 		
      	        		  htmldata+="<td> <img src=\"../.images/music_list.png\" width=\"20\" height=\"20\"></td>";
      	        	 
      	        	else if(tempfile.getName().endsWith("mp4") || tempfile.getName().endsWith("MP4") || 
      	        			tempfile.getName().endsWith("wmv") || tempfile.getName().endsWith("WMV") || 
      	        			tempfile.getName().endsWith("m4v") || tempfile.getName().endsWith("M4V") || 
      	        			tempfile.getName().endsWith("avi") || tempfile.getName().endsWith("AVI") || 
      	        			tempfile.getName().endsWith("mkv") || tempfile.getName().endsWith("MKV") ) 
  	        	 		htmldata+="<td><img src=\"../.images/video_list.png\" width=\"20\" height=\"20\"></td>";

      	        	/* else if(tempfile.getName().endsWith("apk") || tempfile.getName().endsWith("APK") )
      	        		htmldata+="<td><img src=\"../.images/apk-icon.png\" width=\"20\" height=\"20\"></td>"; */
      	        	 
      	        	else if(tempfile.getName().endsWith("doc") || tempfile.getName().endsWith("DOC") || 
      	        			tempfile.getName().endsWith("docx") || tempfile.getName().endsWith("DOCX") ) 
      	        		htmldata+="<td><img src=\"../.images/docicon.png\" width=\"25\" height=\"25\"></td>";
      	        	 
      	        	else if(tempfile.getName().endsWith("xls") || tempfile.getName().endsWith("XLS") ||
      	        			tempfile.getName().endsWith("xlsx") || tempfile.getName().endsWith("XLSX"))
      	        		htmldata+="<td><img src=\"../.images/excelicon.png\" width=\"25\" height=\"25\"></td>"; 
      	        	
      	        	 
      	        	else if(tempfile.getName().endsWith("pdf") || tempfile.getName().endsWith("PDF"))
      	        		htmldata+="<td><img src=\"../.images/pdf_icon.png\" width=\"25\" height=\"25\"></td>";
      	        	 
      	        	else if(tempfile.getName().endsWith("zip") || tempfile.getName().endsWith("ZIP") || 
      	        			tempfile.getName().endsWith("rar") || tempfile.getName().endsWith("RAR")  )
      	        		htmldata+="<td><img src=\"../.images/zipicon.png\" width=\"25\" height=\"25\" ></td>"; 
      	        	 
      	        	else if(tempfile.getName().endsWith("exe") || tempfile.getName().endsWith("EXE"))
      	        		htmldata+="<td><img src=\"../.images/exe_icon.png\" width=\"25\" height=\"25\" ></td>";
      	        	 
      	        	else     	        		  
      	        		htmldata+="<td><img src=\"../.images/gen_file.png\" width=\"25\" height=\"25\" ></td>";
     	        	
      	        	htmldata +="<td>&nbsp&nbsp&nbsp&nbsp" +
      	        	"<a href=\"."+hyperlinkid+"\" target=\"_blank\" style=\"text-decoration: none;color: #444;\">"+tempfile.getName()+"</td> " +
      	        				 "<td><br><span class=\"rightspan\" >Size : "+df.format(dsize)+" "+sizecategory+"</span></td></a>" ;			 
                    
      	          }
      	           
      	          
      	          if(tempfile.isDirectory())
      	          {
      	        	 if(resourceurl.contains("mnt/sdcard")) 
      	        	    htmldata +="<td><img src=\"../.images/folder.png\" width=\"25\" height=\"25\" ></td><td id=\""+hyperlinkid+"/\" onclick=\"viewFileManagerint(this.id)\" >&nbsp&nbsp&nbsp&nbsp"+tempfile.getName()+"</td>"; 
      	        	 
      	        	 else if(resourceurl.contains("mnt/ext_card"))
      	        		 htmldata +="<td><img src=\"../.images/folder.png\" width=\"25\" height=\"25\" ></td><td id=\""+hyperlinkid+"/\" onclick=\"viewFileManagerext(this.id)\" >&nbsp&nbsp&nbsp&nbsp"+tempfile.getName()+"</td>"; 
      	        	 
      	        	 int numberoffiles=0;
      	        	 int numberofdirs=0;
      	        	 File[] filesintempdir=tempfile.listFiles();
      	        	 if(filesintempdir!=null)
      	        	 {
      	        	    for(File temp:filesintempdir)
      	        	    {
      	        	      if(temp.isFile())
      	        	    	  numberoffiles++;
      	        	      if(temp.isDirectory())
      	        	    	  numberofdirs++;
      	        	    }
      	        	 }      	        	
      	        	 htmldata += "<td><br><span class=\"rightspan\" >Folders : "+numberofdirs+"</span>" +
      	        	 		     "<br><span class=\"rightspan\" >Files : "+numberoffiles+"</span></td>" ;     	        	
      	          } 
      	          
      	        htmldata += "</tr>";
               } 

               htmldata += "</tbody></table>"; 
      	    }
      	      
      	     
  
   	     	 
      	     filesize = htmldata.length();
      	   
             dos.writeBytes("HTTP/1.1 200 OK\r\n");
  		     dos.writeBytes("Content-Type: text/html\r\n");     
  		     dos.writeBytes("Content-length: "+filesize+"\r\n");
             dos.writeBytes("Connection: Close\r\n");
             dos.writeBytes("\r\n");

             
             dos.writeBytes(htmldata);
             
             dos.flush();      
             dos.close();
             
        
          }
         
    	         
         if(f.isFile())        
         {     

//             contenttype = resourceurl.substring(resourceurl.lastIndexOf("."), resourceurl.length());
            
                fis=new FileInputStream(f);
    			filesize = f.length(); 
    			
//    			for(MimeTypes m:MimeTypes.al)
//    			{
//                  if(contenttype.equalsIgnoreCase(m.res))
//                  {
//                	  mimetype=m.mimetype;
//                	  break;
//                  }
//    			}
    			
    			dos.writeBytes("HTTP/1.1 200 OK\r\n");
    			dos.writeBytes("Content-Type: application/octet-stream\r\n");          
    			dos.writeBytes("Content-length: "+filesize+"\r\n");
                dos.writeBytes("Connection: Close\r\n");
                dos.writeBytes("\r\n");
                
                byte b[] = new byte[1024];    
                long count = 0;
                int r;
                while(true)
                {
                   r = fis.read(b,0,1024);
                   if(r==-1)
                   {
                      continue;
                   }   
                   dos.write(b,0,r);
                   count+=r;
                   if(count==filesize)
                   {
                       break;
                   }
                }
                fis.close();
               
             
          
            dos.flush();      
            dos.close();          
          } 
         
     
     }
         
  //------------------- File Mananger ends ------------------------

         
  //---------------------------------------Default Case -------------------------------------------------------       
     else  
       {
    	   if(resourceurl.endsWith("/"))
    		  resourceurl=resourceurl.substring(0, resourceurl.length()-1);
    	   
    	   resourceurl = java.net.URLDecoder.decode(resourceurl, "UTF-8");
                   	   
          	// f=new File("/mnt/sdcard/webphone/"+resourceurl );
    	   
          	 f= new File(Environment.getExternalStorageDirectory().toString()+resourceurl);
          	 
          	
          	 
          	 Log.i("nsb","Request for "+resourceurl+" is in : "+f.getPath());
          	 
          	 if(!f.exists())
          	 {        	   
          		 try
          		 {
          			if(resourceurl.contains("html"))
                 	 {
                 		 f= new File(Environment.getExternalStorageDirectory().toString()+"/webphone/"+resourceurl);
                 	 }
                 	 else if(resourceurl.contains("jar"))
                 	 {
                 		 f=new File(Environment.getExternalStorageDirectory().toString()+"/webphone/"+resourceurl);
                 	 }
                 	 else if(resourceurl.contains("css") || resourceurl.contains("js") )
                 	 {
                 		 f=new File(Environment.getExternalStorageDirectory().toString()+"/webphone/"+resourceurl); 
                 	 } 
                 	 else 
                 	 {
                 		 f= new File(resourceurl);
                 	 } 
                 	 Log.i("nsb - FilePath",f.getAbsolutePath());
          		 }
          		 catch(Exception ex)
          		 {
          			 String abcdef="Not Found\r\n";
          			 dos.writeBytes("HTTP/1.1 200 Ok\r\n");
            		 dos.writeBytes("Content-Type: text/plain\r\n");          
            		 dos.writeBytes("Content-length: "+abcdef.length() +"\r\n");
                     dos.writeBytes("Connection: Close\r\n");
                     dos.writeBytes("\r\n");  
                     dos.writeBytes(abcdef);   
          		 }
          	 } 
          	 
          	   fis=new FileInputStream(f);
        //       contenttype = resourceurl.substring(resourceurl.lastIndexOf("."), resourceurl.length());
        //       Log.d("nsb", contenttype);
               
      			filesize = f.length(); 
      			
//      			for(MimeTypes m:MimeTypes.al)
//      			{
//                    if(contenttype.equals(m.res))
//                    {
//                  	  mimetype=m.mimetype;
//                  	  break;
//                    }
//      			}
      			
      			dos.writeBytes("HTTP/1.1 200 OK\r\n");
      			//dos.writeBytes("Content-Type: "+mimetype+"\r\n");          
      			dos.writeBytes("Content-length: "+filesize+"\r\n");
                dos.writeBytes("Connection: Close\r\n");
                dos.writeBytes("\r\n");
                  
                  byte b[] = new byte[1024];    
                  long count = 0;
                  int r;
                  while(true)
                  {
                     r = fis.read(b, 0, 1024);
                     if(r==-1)
                     {
                        continue;
                     }   
                     dos.write(b,0,r);
                     count+=r;
                     if(count==filesize)
                     {
                         break;
                     }
                  }
                 fis.close();
          	
             
             dos.flush();      
             dos.close();          
    	   
    	   
       }
      }	

       //---------------------------------------Default case Ends -------------------------------------------------------------
    }//--- Try of Run
	   
      catch (IOException e) 
	  {
    	  //errorString=e.toString();
    	  //mh.sendEmptyMessage(1);
		  e.printStackTrace();
	  }
      finally
      {
    	   
    	   sock=null;
    	   f=null;
    	   dos=null;
    	   dis=null;
    	   fis=null;
    	   filesize=0;
    	   resourceurl=null;
    	   contenttype=null;
    	   mimetype=null;
    	   
    	   contentlengthforpost=null;
    	  
    	   requestheader=null;    // ------- For File Receiving 
    	   receivedfilesize=0;    // ------- For File Receiving
    	   filename=null;		  // ------- For File Receiving 
    	   folderpath=null;    	  // ------- 
      }
      
     }
    
    void DeleteRecursive(File fileOrDirectory) 
    {
       if (fileOrDirectory.isDirectory())
         for (File child : fileOrDirectory.listFiles())
             DeleteRecursive(child);

       fileOrDirectory.delete();
    }
   }


   public class MyHandler extends Handler
   {
	 @Override
	 public void handleMessage(Message msg) 
	 {
		super.handleMessage(msg);
		devicesip=devicesip.substring(1);
		tv_devices.append("\n"+devicesip);
	 }
}




}







    

