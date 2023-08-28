package com.discutiamo.solaretermico;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import com.discutiamo.solaretermico.beans.*;

import android.net.wifi.WifiManager;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.widget.Toolbar;

import android.app.*;
import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.speech.RecognizerIntent;
import androidx.appcompat.app.*;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class MainActivity extends AppCompatActivity
{
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mPlanetTitles;
	private static MainActivity ma;
	private static MenuItem myMenuItem;
	private AudioManager audio;
	private int currentLayout;
	private String fraseAltolivello="";
	private MediaPlayer mp = null;
	private TextToSpeech tts;

	public static int positionStartApp;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.v("Carlo", "onCreate");

		ma = this;
		Impostazioni.generateNewInstance(this.getApplicationContext());

		setContentView(R.layout.activity_main);
		mTitle = mDrawerTitle = getTitle();
		mPlanetTitles = getResources().getStringArray(R.array.planets_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mPlanetTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


		tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
			public void onInit(int status) {
				if(status != TextToSpeech.ERROR) {
					tts.setLanguage(Locale.ITALIAN);
				}
			}
		});


		final ActionBar actionBar = getSupportActionBar();
		// enable ActionBar app icon to behave as action to toggle nav drawer
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);  //visualizza l'icona del cancello
		actionBar.setDisplayHomeAsUpEnabled(true); //visualizza le 3 linee del menù
		actionBar.setDisplayShowTitleEnabled(true); //visualizza il titolo
		actionBar.setDisplayShowCustomEnabled(false);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				(Toolbar) findViewById(R.id.toolbar),  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				actionBar.setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				actionBar.setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.addDrawerListener(mDrawerToggle);

		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		Log.d("Carlo", "title: "+this.getIntent().getStringExtra("title"));

	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.d("Carlo", "onKeyDown(int keyCode, KeyEvent event)");

		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				if(ma.currentLayout==R.layout.impostazioni)
					selectItem(0, 0);
				else
					moveTaskToBack(true);
				break;
			case KeyEvent.KEYCODE_HOME:
				//selectItem(0, 0);
				break;
			case KeyEvent.KEYCODE_VOLUME_UP:
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,	AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,	AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
				break;
			default:
				return super.onKeyDown(keyCode, event);
		}
		return true;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		MenuItem item = menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		myMenuItem = item;

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}

		switch(item.getItemId())
		{
			case R.id.action_websearch:

				Log.d("Carlo", item+"");
				int tipoConn = isConnessoARete(ma);
				if(item.getIcon().getConstantState().equals(ma.getDrawable(R.drawable.terra).getConstantState()) && (tipoConn==2 || tipoConn==3 ))
					item.setIcon(R.drawable.casetta);
				else
					item.setIcon(R.drawable.terra);

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void promptSpeechInput()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Alessio, cosa desideri ?");

		try {
			startActivityForResult(intent, 100);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(getApplicationContext(), "Spiacente, questo dispositivo non supporta il servizio voce.",
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case 100: {
				if (resultCode == RESULT_OK && null != data)
				{
					ArrayList<String> result = data
							.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					fraseAltolivello = result.get(0);
					Log.i("Carlo", fraseAltolivello);
				}
				break;
			}

		}
	}


	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position, 0);
		}
	}

	public void selectItem(int position, int subposition)
	{
		Log.d("Carlo", "selectItem()"+position+" "+subposition);

		// update the main content by replacing fragments
		Fragment fragment = new PlanetFragment();

		Bundle args = new Bundle();
		args.putInt(PlanetFragment.POSITION, position);
		args.putInt(PlanetFragment.SUBPOSITION, subposition);

		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();


		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);

		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title)
	{
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private static long timeTypeConnection = 0;
	private static int returnTypeConnection=0;
	public static int isConnessoARete(Context context) //0 non connesso, 1 connesso con mobile, 2 connesso con wifi, 3 connesso con wifi con accosso ad internet
	{
		Log.d("Carlo", "start: isConnessoARete()");

		if(timeTypeConnection+3000>=System.currentTimeMillis()) return returnTypeConnection;

		try
		{
			ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo netInfo = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (netInfo.getState() == NetworkInfo.State.CONNECTED)
			{
				Log.d("Carlo", "isConnessoARete - connesso con 3G");
				timeTypeConnection = System.currentTimeMillis();
				returnTypeConnection=1;
				return returnTypeConnection;
			}

			NetworkInfo wifiInfo = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifiInfo.getState() == NetworkInfo.State.CONNECTED)
			{
				Log.d("Carlo", "stop: isConnessoARete - connesso con WIFI");
				WifiManager wifii = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				DhcpInfo d = wifii.getDhcpInfo();

				String dns1 = ( d.dns1 & 0xFF) + "." + ((d.dns1 >> 8 ) & 0xFF) + "." + ((d.dns1 >> 16 ) & 0xFF ) + "." + ((d.dns1 >> 24 ) & 0xFF);
				InetAddress address = InetAddress.getByName(dns1);

				if(address.isReachable(500)) {
					timeTypeConnection = System.currentTimeMillis();
					returnTypeConnection=3;
					return returnTypeConnection;
				}

				timeTypeConnection = System.currentTimeMillis();
				returnTypeConnection=2;
				return returnTypeConnection;
			}

			Log.d("Carlo",
					"stop: isConnessoARete - Nessuna connessione ad Internet");
		}
		catch(Exception e)
		{
			Log.e("Carlo", e.getMessage());
		}


		return 0;
	}

	public boolean isCasetta() //se l'icona impostata è la casetta
	{
		int tipoConn = ma.isConnessoARete(ma);
		if(myMenuItem!=null && tipoConn!=2 && tipoConn!=3)
		{
			ma.runOnUiThread(new Thread()
			{
				public void run()
				{
					myMenuItem.setIcon(R.drawable.terra);
				}
			});
			return false;
		}

		if(myMenuItem==null || myMenuItem.getIcon().getConstantState().equals(ma.getDrawable(R.drawable.terra).getConstantState()))
		{
			Log.d("Carlo", "Casetta FALSE");
			return false;
		}

		Log.d("Carlo", "Casetta TRUE");
		return true;
	}

	/**
	 * Fragment that appears in the "content_frame", shows a planet
	 */
	public static class PlanetFragment extends Fragment
	{
		public static final String POSITION = "position";
		public static final String SUBPOSITION = "subposition";

		static View rootView=null;
		private Thread threadScorriTempo = null;
		private Thread threadaccendispegni = null;

		public PlanetFragment()
		{
			threadaccendispegni = new Thread()
			{
				public void run()
				{
				}
			};
			threadaccendispegni.start();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState)
		{
			int position = getArguments().getInt(POSITION);
			int subposition = getArguments().getInt(SUBPOSITION);

			ActionBar actionBar = ma.getSupportActionBar();

			ma.currentLayout = R.layout.servizio_caldaia_casa;

			switch(position)
			{
				case 0:
					actionBar.setIcon(R.drawable.icona_servizi_50);
					ma.currentLayout = R.layout.servizio_caldaia_casa;
					break;

				case 1:
					actionBar.setIcon(R.drawable.icona_impostazioni_50);
					ma.currentLayout = R.layout.impostazioni;
					break;
			}

			rootView = inflater.inflate(ma.currentLayout, container, false);

			String planet = getResources().getStringArray(R.array.planets_array)[position];
			getActivity().setTitle(planet);


			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

			switch(ma.currentLayout)
			{
				case R.layout.servizio_caldaia_casa:
					faiGraficaSolareTermico();
					break;

				case R.layout.impostazioni:
					faiGraficaImpostazioni();
					break;
			}

			return rootView;
		}


		private void staiCalmo()
		{
			final TextView avvisi = (TextView) rootView.findViewById(R.id.avvisi);
			setTextEColor(avvisi, "Stai Calmo, sto lavorando per te!", Color.BLUE);
			ma.eseguiSuono("staicalmo.mp3");
		}

		private void setTextEColor(final TextView avvisi, final String testo, final int color)
		{
			ma.runOnUiThread(new Runnable()
			{
				public void run()
				{
					avvisi.setTextColor(color);
					avvisi.setText(testo);
				}
			});
		}

		private void faiGraficaSolareTermico()
		{
			final ImageView refresh = (ImageView) rootView.findViewById(R.id.refresh);

			final TextView avvisi = (TextView) rootView.findViewById(R.id.avvisi);
			final RadioGroup rb_tipoattivazione = (RadioGroup) rootView.findViewById(R.id.rb_tipoattivazione);
			final StatoServizi statoservizi = new StatoServizi();
			final Button salva = (Button) rootView.findViewById(R.id.salva);

			final EditText maxTempAcquaSolare = (EditText) rootView.findViewById(R.id.maxTempAcquaSolare);
			final EditText minTempPannelli = (EditText) rootView.findViewById(R.id.minTempPannelli);
			final EditText minTempStacco = (EditText) rootView.findViewById(R.id.minTempStacco);

			threadaccendispegni = new Thread()
			{
				public void run()
				{
					ma.runOnUiThread(new Thread()
					{
						public void run()
						{
							avvisi.setTextColor(Color.BLUE);
							avvisi.setText("Attendi...");
							ma.eseguiAnimazione(refresh, R.anim.ruota);
						}
					});

					accendi_spegni_servizi_ar4(statoservizi, "");

					ma.runOnUiThread(new Thread()
					{
						public void run()
						{
							ma.stopAnimazione(refresh);
							aggiornaSolareTermico(statoservizi);
						}
					});
				}
			};
			threadaccendispegni.start();


			refresh.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					if(threadaccendispegni.isAlive())
					{
						staiCalmo();
						return;
					}

					threadaccendispegni = new Thread()
					{
						public void run()
						{
							ma.runOnUiThread(new Thread()
							{
								public void run()
								{
									avvisi.setTextColor(Color.BLUE);
									avvisi.setText("Attendi...");
									ma.eseguiAnimazione(refresh, R.anim.ruota);
								}
							});

							accendi_spegni_servizi_ar4(statoservizi, "");

							ma.runOnUiThread(new Thread()
							{
								public void run()
								{
									ma.stopAnimazione(refresh);
									aggiornaSolareTermico(statoservizi);
								}
							});
						}
					};
					threadaccendispegni.start();
				}
			});

			salva.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view)
				{
					if(statoservizi.getEsito_ar4()!=1)
						return;

					threadaccendispegni = new Thread()
					{
						public void run()
						{
							setTextEColor(avvisi, "Attendi!", Color.BLUE);

							ma.eseguiAnimazione(refresh, R.anim.ruota);

							int minTempStaccoI = Integer.parseInt(minTempStacco.getText().toString());
							int minTempPannelliI = Integer.parseInt(minTempPannelli.getText().toString());
							int maxTempAcquaSolareI = Integer.parseInt(maxTempAcquaSolare.getText().toString());


							accendi_spegni_servizi_ar4(statoservizi,
									"minTempStacco="+minTempStaccoI+
									"&minTempPannelli="+minTempPannelliI+
									"&maxTempAcquaSolare="+maxTempAcquaSolareI
							);

							ma.stopAnimazione(refresh);

							ma.runOnUiThread(new Thread()
							{
								public void run()
								{
									aggiornaSolareTermico(statoservizi);
								}
							});

							if(statoservizi.getEsito_ar4()==1) ma.eseguiSuono("aperturaok.mp3");
						}
					};
					threadaccendispegni.start();
				}
			});



			rb_tipoattivazione.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId)
				{
					int radioButtonID = rb_tipoattivazione.getCheckedRadioButtonId();
					View radioButton = rb_tipoattivazione.findViewById(radioButtonID);
					final int idx = rb_tipoattivazione.indexOfChild(radioButton);

					if(idx==Integer.parseInt(statoservizi.getAttManualePompeSolare_casa())) return;

					if(threadaccendispegni.isAlive())
					{
						staiCalmo();
						return;
					}



					threadaccendispegni = new Thread()
					{
						public void run()
						{
							if(statoservizi.getEsito_ar4()!=1)
								return;

							setTextEColor(avvisi, "Attendi!", Color.BLUE);

							ma.eseguiAnimazione(refresh, R.anim.ruota);

							accendi_spegni_servizi_ar4(statoservizi, "attManualePompeSolare="+idx);

							ma.stopAnimazione(refresh);

							ma.runOnUiThread(new Thread()
							{
								public void run()
								{
									aggiornaSolareTermico(statoservizi);
								}
							});

							if(statoservizi.getEsito_ar4()==1) ma.eseguiSuono("aperturaok.mp3");
						}
					};
					threadaccendispegni.start();
				}
			});

		}

		private void aggiornaSolareTermico(final StatoServizi statoservizi)
		{
			if(ma.currentLayout!=R.layout.servizio_caldaia_casa) return;

			final TextView avvisi = (TextView) rootView.findViewById(R.id.avvisi);
            final ImageView pompeSolare1 = (ImageView) rootView.findViewById(R.id.pompasolare_1);
			final ImageView pompeSolare2 = (ImageView) rootView.findViewById(R.id.pompasolare_2);

            final TextView temp_1 = (TextView) rootView.findViewById(R.id.temp_1);
            final TextView temp_2 = (TextView) rootView.findViewById(R.id.temp_2);
            final TextView temp_3 = (TextView) rootView.findViewById(R.id.temp_3);
            final TextView temp_4 = (TextView) rootView.findViewById(R.id.temp_4);
            final TextView temp_5 = (TextView) rootView.findViewById(R.id.temp_5);

			final RadioGroup rb_tipoattivazione = (RadioGroup) rootView.findViewById(R.id.rb_tipoattivazione);

			final EditText maxTempAcquaSolare = (EditText) rootView.findViewById(R.id.maxTempAcquaSolare);
			final EditText minTempPannelli = (EditText) rootView.findViewById(R.id.minTempPannelli);
			final EditText minTempStacco = (EditText) rootView.findViewById(R.id.minTempStacco);

            ma.runOnUiThread(new Thread()
			{
				public void run()
				{
					if(statoservizi.getEsito_ar4()!=1)
					{
						setTextEColor(avvisi, ">>> AR4 <<<", Color.RED);
						ma.eseguiSuono("errore.mp3");
						return;
					}

                    setTextEColor(avvisi, statoservizi.getDataora(), Color.GREEN);
                    ma.eseguiSuono("aperturaok.mp3");

                    if(statoservizi.getAttManualePompeSolare_casa().equals("1"))
                        pompeSolare1.setImageResource(R.drawable.pompaonm);
                    else
                    if(statoservizi.isPompa_solare1_casa())
                        pompeSolare1.setImageResource(R.drawable.pompaon);
                    else
                        pompeSolare1.setImageResource(R.drawable.pompaoff);

					if(statoservizi.getAttManualePompeSolare_casa().equals("1"))
						pompeSolare2.setImageResource(R.drawable.pompaonm);
					else
					if(statoservizi.isPompa_solare2_casa())
						pompeSolare2.setImageResource(R.drawable.pompaon);
					else
						pompeSolare2.setImageResource(R.drawable.pompaoff);


					RadioButton rb = (RadioButton)rb_tipoattivazione.getChildAt(Integer.parseInt(statoservizi.getAttManualePompeSolare_casa()));
					rb.setChecked(true);

					temp_1.setText(statoservizi.getTemp_1_casa()+"°");
                    temp_2.setText(statoservizi.getTemp_2_casa());
                    temp_3.setText(statoservizi.getTemp_3_casa());
                    temp_4.setText(statoservizi.getTemp_4_casa());
                    temp_5.setText(statoservizi.getTemp_5_casa());

					maxTempAcquaSolare.setText(statoservizi.getMaxTempAcquaSolare_casa());
					minTempPannelli.setText(statoservizi.getMinTempPannelli_casa());
					minTempStacco.setText(statoservizi.getMinTempStacco_casa());


				}
			});

			if(statoservizi.getEsito_ar4()!=1) return;
		}


		public void faiGraficaImpostazioni()
		{
			Log.d("Carlo", "faiGraficaImpostazioni()");

			final Impostazioni imp = Impostazioni.getInstance();

			final EditText pwdArduino = (EditText) rootView.findViewById(R.id.pwdArduino);
			final EditText hostname = (EditText) rootView.findViewById(R.id.hostname);


			final RadioButton tcp = (RadioButton) rootView.findViewById(R.id.tcp);
			final RadioButton udp = (RadioButton) rootView.findViewById(R.id.udp);

			hostname.setText(imp.getHostname());
			pwdArduino.setText(imp.getPwdArduino());

			hostname.setOnFocusChangeListener(new View.OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus)
				{
					if(!hasFocus)
						imp.setHostname(hostname.getText().toString());
				}
			});

			pwdArduino.setOnFocusChangeListener(new View.OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus)
				{
					if(!hasFocus)
						imp.setPwdArduino(pwdArduino.getText().toString());
				}
			});

			if(imp.isUdp())
				udp.setChecked(true);
			else
				tcp.setChecked(true);

			udp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					imp.setUdp(isChecked);
				}
			});

		}


		private synchronized void accendi_spegni_servizi_ar4(StatoServizi statoservizi, String attributi)
		{
            final Impostazioni imp = Impostazioni.getInstance();

            if(imp.isUdp())
            {
                accendi_spegni_servizi_ar4_UDP(statoservizi, attributi);
                return;
            }

            String dataora = new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date());

            InputStream in = null;
			String strURL = "http://"+imp.getHostname()+":5150/?"+attributi+"&time="+dataora+"&pwd="+imp.getPwdArduino();

			int tipoConn = ma.isConnessoARete(ma);

			for(int i=0 ; i<3 ; i++ )
			{
				Log.d("Carlo", strURL);
				try {
					URLConnection connection = (new URL(strURL)).openConnection();
					connection.setConnectTimeout(2000);
					connection.setReadTimeout(5000);
					connection.connect();

					in = connection.getInputStream();
	    	    /*
	    	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    	    StringBuilder html = new StringBuilder();
	    	    for (String line; (line = reader.readLine()) != null; ) {
	    	        html.append(line);
	    	    }
	    	    in.close();

	    	    String xml = html.toString();
	    	    */

					// XML START
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(in);
					doc.getDocumentElement().normalize();

					elaboraXML_ar4(doc, statoservizi);

					statoservizi.setEsito_ar4(1);
					return;
				} catch (Exception e)
				{
					try {Thread.sleep(1000);}
					catch (InterruptedException e1) {}
					statoservizi.setEsito_ar4(-1);
					Log.e("Carlo", e.toString());
					e.printStackTrace();
				} finally {
					try {
						if (in != null) in.close();
					} catch (IOException e) {
					}
				}
			}

		}

		private synchronized void accendi_spegni_servizi_ar4_UDP(StatoServizi statoservizi, String attributi)
		{
			Log.d("Carlo", "accendi_spegni_temperature_ar6_UDP(...)");
			DatagramSocket serverSocket = null;
			try
			{
				final Impostazioni imp = Impostazioni.getInstance();
				String dataora = new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date());

				String addParametri = "&time=" + dataora + "&pwd=" + imp.getPwdArduino();
				attributi += addParametri;

				serverSocket = new DatagramSocket(55150); //
				serverSocket.setSoTimeout(10000);

				Log.d("Carlo", attributi);

				String xml = null;
				byte buf[] = new byte[2048];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);

				DatagramPacket sendPacket_Internet = new DatagramPacket(attributi.getBytes(), attributi.length(), InetAddress.getByName(imp.getHostname()), 5150);
				serverSocket.send(sendPacket_Internet);

				try
				{
					serverSocket.receive(packet);
					xml = new String(packet.getData(), 0, packet.getLength());
				} catch (SocketTimeoutException e) {
					Log.e("Carlo", e.getMessage());
					statoservizi.setEsito_ar4(-1);
				}

				if (xml == null) {
					statoservizi.setEsito_ar4(-1);
					return;
				}

				// XML START
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				InputSource is = new InputSource(new StringReader(xml));
				Document doc = dBuilder.parse(is);
				doc.getDocumentElement().normalize();

				elaboraXML_ar4(doc, statoservizi);

				statoservizi.setEsito_ar4(1);
				return;
			} catch (Exception e)
			{
				Log.e("Carlo", "Exception", e);
				statoservizi.setEsito_ar4(-1);
			}
			finally
			{
				if(serverSocket!=null)
					serverSocket.close();
			}

		}


		private void elaboraXML_ar4(Document doc, StatoServizi statoservizi) throws Exception
		{
			NodeList nList = doc.getElementsByTagName("dataora");
			Element elemento = (Element) nList.item(0);
			statoservizi.setDataora(elemento.getTextContent());

			nList = doc.getElementsByTagName("pompa_solare1");
			elemento = (Element) nList.item(0);
			statoservizi.setPompa_solare1_casa(elemento.getTextContent().equals("1"));

			nList = doc.getElementsByTagName("pompa_solare2");
			elemento = (Element) nList.item(0);
			statoservizi.setPompa_solare2_casa(elemento.getTextContent().equals("1"));

			nList = doc.getElementsByTagName("attManualePompeSolare");
			elemento = (Element) nList.item(0);
			statoservizi.setAttManualePompeSolare_casa(elemento.getTextContent());

			nList = doc.getElementsByTagName("minTempPannelli");
			elemento = (Element) nList.item(0);
			statoservizi.setMinTempPannelli_casa(elemento.getTextContent());

			nList = doc.getElementsByTagName("maxTempAcquaSolare");
			elemento = (Element) nList.item(0);
			statoservizi.setMaxTempAcquaSolare_casa(elemento.getTextContent());

			nList = doc.getElementsByTagName("minTempStacco");
			elemento = (Element) nList.item(0);
			statoservizi.setMinTempStacco_casa(elemento.getTextContent());

			nList = doc.getElementsByTagName("temp_1");
			elemento = (Element) nList.item(0);
			statoservizi.setTemp_1_casa(elemento.getTextContent());

			nList = doc.getElementsByTagName("temp_2");
			elemento = (Element) nList.item(0);
			statoservizi.setTemp_2_casa(elemento.getTextContent());

			nList = doc.getElementsByTagName("temp_3");
			elemento = (Element) nList.item(0);
			statoservizi.setTemp_3_casa(elemento.getTextContent());

			nList = doc.getElementsByTagName("temp_4");
			elemento = (Element) nList.item(0);
			statoservizi.setTemp_4_casa(elemento.getTextContent());

			nList = doc.getElementsByTagName("temp_5");
			elemento = (Element) nList.item(0);
			statoservizi.setTemp_5_casa(elemento.getTextContent());
		}
	}

	public void nascondiTastiera()
	{
		InputMethodManager inputMethodManager = (InputMethodManager) ma.getSystemService(INPUT_METHOD_SERVICE);
		if(inputMethodManager.isActive() && ma.getCurrentFocus()!=null)
			inputMethodManager.hideSoftInputFromWindow(ma.getCurrentFocus().getWindowToken(), 0);
	}

	protected void onStop()
	{
		super.onStop();
		Log.d("Carlo", "STOP");
	}

	protected void onResume()
	{
		super.onResume();
		Log.d("Carlo", "RESUME " + positionStartApp);

		if(positionStartApp !=-1)
		{
			switch(positionStartApp)
			{
				case 0:
					selectItem(0, 0);
					break;
				case 1:
					selectItem(1, 0);
					break;
				default:
					selectItem(0, 0);
			}
			positionStartApp =-1;
		}
	}

	public void eseguiSuono(final String mp3Url)
	{
		Log.d("Carlo", "start: eseguiSuono()");

		ma.runOnUiThread(new Runnable()
		{
			public void run()
			{
				if (mp != null && mp.isPlaying())
				{
					mp.stop();
					mp.reset();
				}

				try
				{
					AssetFileDescriptor file = ma.getAssets().openFd("audio/"+mp3Url);
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
			}
		});



		Log.d("Carlo", "stop: eseguiSuono()");
	}

	private void eseguiAnimazione(final View v, final int ranim)
	{
		final Animation anim = AnimationUtils.loadAnimation(ma.getApplicationContext(), ranim);
		ma.runOnUiThread(
				new Runnable()
				{
					public void run()
					{
						v.startAnimation(anim);
					}
				});
	}

	private void stopAnimazione(final View v)
	{
		ma.runOnUiThread(
				new Runnable()
				{
					public void run()
					{
						v.clearAnimation();
					}
				});
	}

	private void speak(String testo)
	{
		ma.tts.speak(testo, TextToSpeech.QUEUE_FLUSH, null, null);
	}
}