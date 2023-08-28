package com.discutiamo.solaretermico.beans;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Impostazioni
{
	private static Context context;
	private static Impostazioni instance=null;
	private String hostname;
	private String pwdArduino;
	private boolean udp;

	private Impostazioni(){}
	
	private Impostazioni(Context context)
	{
		this.context=context;
		
		SharedPreferences prefs = context.getSharedPreferences("Impostazioni", context.MODE_PRIVATE);
		this.hostname = prefs.getString("hostname", "");
		this.pwdArduino = prefs.getString("pwdArduino", "");
		this.udp = prefs.getBoolean("udp", true);
	}
	
	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
		salvaImpostazioni();
	}
	
	
	public String getPwdArduino()
	{
		return pwdArduino;
	}
	
	public void setPwdArduino(String pwdArduino)
	{
		this.pwdArduino = pwdArduino;
		salvaImpostazioni();
	}

	public boolean isUdp() {
		return udp;
	}

	public void setUdp(boolean udp) {
		this.udp = udp;
		salvaImpostazioni();
	}

	public void salvaImpostazioni()
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences("Impostazioni", context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sharedPreferences.edit();
	    
		editor.putString("hostname", hostname);
		editor.putString("pwdArduino", pwdArduino);
		editor.putBoolean("udp", udp);

	    editor.commit();
	}


	public static Impostazioni generateNewInstance(Context context)
	{
		if(instance==null)
			instance = new Impostazioni(context);
		return instance;
	}


	public static Impostazioni getInstance()
	{
		return instance;

	}
}
