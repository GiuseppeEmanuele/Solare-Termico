package com.discutiamo.solaretermico;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.discutiamo.solaretermico.beans.Impostazioni;
import com.discutiamo.solaretermico.beans.StatoServizi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WidgetTempSolareCasa extends AppWidgetProvider
{

	public static final String AGGIORNA_TEMP_SOLARE_CASA = "com.discutiamo.solaretermico.aggiorna_temp_solare_casa";

	private MediaPlayer mp = null;
	private static RemoteViews views = null;
	
	public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId)
	{
		if(views==null) {

			views = new RemoteViews(context.getPackageName(), R.layout.widget_temp_solare_casa);

			Intent aggiornaTemperatura = new Intent(context, WidgetTempSolareCasa.class);
			aggiornaTemperatura.setAction(AGGIORNA_TEMP_SOLARE_CASA);
			PendingIntent pendingIntent_aggiornatemperatura = PendingIntent.getBroadcast(context, 0, aggiornaTemperatura, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.idtutto, pendingIntent_aggiornatemperatura);
		}
		appWidgetManager.updateAppWidget(appWidgetId, views);

	}
	
	@Override
    public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if(views==null) {

			views = new RemoteViews(context.getPackageName(), R.layout.widget_temp_solare_casa);

			Intent aggiornaTemperatura = new Intent(context, WidgetTempSolareCasa.class);
			aggiornaTemperatura.setAction(AGGIORNA_TEMP_SOLARE_CASA);
			PendingIntent pendingIntent_aggiornatemperatura = PendingIntent.getBroadcast(context, 0, aggiornaTemperatura, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.idtutto, pendingIntent_aggiornatemperatura);
		}

		Impostazioni imp = Impostazioni.getInstance()!=null ? Impostazioni.getInstance() : Impostazioni.generateNewInstance(context);

		if (action.equals(AGGIORNA_TEMP_SOLARE_CASA))
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			eseguiSuono(context, "beep.mp3");
		}



		//Log.d("Carlo", action);

		if (AGGIORNA_TEMP_SOLARE_CASA.equals(action))
	    {
	        Log.v("Carlo", AGGIORNA_TEMP_SOLARE_CASA);

	        StatoServizi stato = eseguiIstruzioni(context);

	    	if(stato.getEsito_ar4()==1)
	    	{
				views.setTextViewText(R.id.w_temp1, stato.getTemp_1_casa()+"°");
				views.setTextViewText(R.id.w_temp2, stato.getTemp_2_casa());
				views.setTextViewText(R.id.w_temp3, stato.getTemp_3_casa());
				views.setTextViewText(R.id.w_temp4, stato.getTemp_4_casa());
				views.setTextViewText(R.id.w_temp5, stato.getTemp_5_casa());

				if(stato.getAttManualePompeSolare_casa().equals("1"))
					views.setImageViewResource(R.id.w_pompaSolare1, R.drawable.pompaonm);
				else
				if(stato.isPompa_solare1_casa())
					views.setImageViewResource(R.id.w_pompaSolare1, R.drawable.pompaon);
				else
					views.setImageViewResource(R.id.w_pompaSolare1, R.drawable.pompaoff);

				if(stato.getAttManualePompeSolare_casa().equals("1"))
					views.setImageViewResource(R.id.w_pompaSolare2, R.drawable.pompaonm);
				else
				if(stato.isPompa_solare2_casa())
					views.setImageViewResource(R.id.w_pompaSolare2, R.drawable.pompaon);
				else
					views.setImageViewResource(R.id.w_pompaSolare2, R.drawable.pompaoff);

				Toast.makeText(context, "Aggiornata!", Toast.LENGTH_LONG).show();
	    		eseguiSuono(context, "aperturaok.mp3");

				AppWidgetManager manager = AppWidgetManager.getInstance(context);
				ComponentName thisWidget = new ComponentName(context, WidgetTempSolareCasa.class);
				manager.updateAppWidget(thisWidget, views);
	    	}
	    	else
	    	{
	    		Toast.makeText(context, "<<< ERRORE >>>", Toast.LENGTH_LONG).show();
	    		eseguiSuono(context, "errore.mp3");
	    	}
	    }

	    super.onReceive(context, intent);
    }

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.d("Carlo", "onUpdate");

		for (int appWidgetId : appWidgetIds)
		{
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	private StatoServizi eseguiIstruzioni(Context context)
	{
		StatoServizi stato = new StatoServizi();

		Impostazioni imp = Impostazioni.getInstance();
		String dataora = new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date());

		String strURL = "http://"+imp.getHostname()+":5150/?time="+dataora+"&pwd="+imp.getPwdArduino();

		InputStream in=null;

		for(int i=0 ; i<3 ; i++)
		{
			try
			{
				Log.d("Carlo", strURL);

				URLConnection connection = (new URL(strURL)).openConnection();
				connection.setConnectTimeout(2000);
				connection.setReadTimeout(6000);
				connection.connect();

				in = connection.getInputStream();


				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(in);
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("temp_1");
				Element elemento = (Element) nList.item(0);
				stato.setTemp_1_casa(elemento.getTextContent());

				nList = doc.getElementsByTagName("temp_2");
				elemento = (Element) nList.item(0);
				stato.setTemp_2_casa(elemento.getTextContent());

				nList = doc.getElementsByTagName("temp_3");
				elemento = (Element) nList.item(0);
				stato.setTemp_3_casa(elemento.getTextContent());

				nList = doc.getElementsByTagName("temp_4");
				elemento = (Element) nList.item(0);
				stato.setTemp_4_casa(elemento.getTextContent());

				nList = doc.getElementsByTagName("temp_5");
				elemento = (Element) nList.item(0);
				stato.setTemp_5_casa(elemento.getTextContent());

				nList = doc.getElementsByTagName("pompa_solare1");
				elemento = (Element) nList.item(0);
				stato.setPompa_solare1_casa(elemento.getTextContent().equals("1"));

				nList = doc.getElementsByTagName("pompa_solare2");
				elemento = (Element) nList.item(0);
				stato.setPompa_solare2_casa(elemento.getTextContent().equals("1"));

				nList = doc.getElementsByTagName("attManualePompeSolare");
				elemento = (Element) nList.item(0);
				stato.setAttManualePompeSolare_casa(elemento.getTextContent());

				stato.setEsito_ar4(1);

				return stato;
			} catch (Exception e)
			{
				stato.setEsito_ar4(-1);
				Log.e("Carlo", e.getMessage());
				try {Thread.sleep(1000);}
				catch (InterruptedException e1){}
			}
			finally {
				try {
					if (in != null) in.close();
				} catch (IOException e) {
				}
			}
		}

		return stato;

	}
	
    public void eseguiSuono(final Context context, final String mp3Url)
    {
    	Log.d("Carlo", "start: eseguiSuono()");
    	
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

    	
		if (mp != null && mp.isPlaying())
		{
			mp.stop();
			mp.reset();
		}
    	
    	try
		{
    		AssetFileDescriptor file = context.getAssets().openFd("audio/"+mp3Url);
			mp = new MediaPlayer();
            mp.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mp.prepare();
        	mp.start();
        	file.close();
		}
		catch (Exception e)
		{
			 Log.e("Carlo", e.getMessage());
		}
    	Log.d("Carlo", "stop: eseguiSuono()");
    }


}
