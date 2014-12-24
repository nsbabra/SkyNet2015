package com.nsb.skynet;

import java.net.InetAddress;
import java.util.ArrayList;

import android.app.Application;

public class GlobalAppActivity extends Application 
{
	public static ArrayList<InetAddress> ipsconnected =new ArrayList<InetAddress>();
	public static boolean canrun=false;
}
  
