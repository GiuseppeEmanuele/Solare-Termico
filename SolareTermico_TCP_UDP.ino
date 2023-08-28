#include <ESP8266WiFi.h>
#include <OneWire.h>
#include <ESP8266HTTPClient.h>
#include <DallasTemperature.h>
#include <WiFiUDP.h>
#include <WiFiClient.h>
#include "Time.h"

void verificaWebServer();
void verificaTemperature();
void comandiHtml(String);
String rispostaHtml();
void verificaWebServerUDP();
void salvaSuIntenetStato();

#define PASSWORD 456123
#define MYLOW HIGH
#define MYHIGH LOW
#define POMPA_SOLARE1 D1
#define POMPA_SOLARE2 D2
#define ONE_WIRE_BUS_1 D3 // DS18B20 pin PANNELLI
#define ONE_WIRE_BUS_2 D4 // DS18B20 pin 1T
#define ONE_WIRE_BUS_3 D5 // DS18B20 pin 1D
#define ONE_WIRE_BUS_4 D6 // DS18B20 pin 2T
#define ONE_WIRE_BUS_5 D7 // DS18B20 pin 2D

// START: TUTTO PER ETHERNET
const char* ssid = "giuseppe";
const char* password = "12345678";
byte ip_[] = {192, 168, 137, 10};
byte gateway_[] = {192, 168, 137, 1};
byte subnet_[] = {255, 255, 255, 0};
byte dns_[] = {192, 168, 137, 1};
int portServer = 5150;
WiFiServer server(portServer);
WiFiUDP Udp;
WiFiClient wifiClient;
// STOP: TUTTO PER ETHERNET


float temp_1, temp_2, temp_3, temp_4, temp_5;
unsigned long richiestatempo_time=0;
int attManualePompeSolare=2;
float minTempPannelli=30; // è la differenza tra la temperatura del pannello e quella del boiler sotto 1D
float maxTempAcquaSolare = 70;
float minTempStacco=5;
unsigned long salvastato_time;



// START: TUTTO PER TEMPERATURA
OneWire oneWire_1(ONE_WIRE_BUS_1);
DallasTemperature DS18B20_1(&oneWire_1);

OneWire oneWire_2(ONE_WIRE_BUS_2);
DallasTemperature DS18B20_2(&oneWire_2);

OneWire oneWire_3(ONE_WIRE_BUS_3);
DallasTemperature DS18B20_3(&oneWire_3);

OneWire oneWire_4(ONE_WIRE_BUS_4);
DallasTemperature DS18B20_4(&oneWire_4);

OneWire oneWire_5(ONE_WIRE_BUS_5);
DallasTemperature DS18B20_5(&oneWire_5);

// STOP: TUTTO PER TEMPERATURA



String requestUDP;

void setup() 
{
  Serial.begin(115200);
  delay(10);

  Serial.println("Start");

  WiFi.mode(WIFI_STA);

  //WiFi.config(ip_, dns_, gateway_, subnet_);
  WiFi.begin(ssid, password);

  int provawifi=0;
  while (WiFi.status() != WL_CONNECTED) 
  {
    delay(500);
    if(++provawifi>30) break;
    //Serial.print(".");
  }

  if(WiFi.status() == WL_CONNECTED)
  {
    Serial.println("");
    Serial.println("----------");
    Serial.println("WiFi connected");
    Serial.println(WiFi.localIP());
    Serial.printf("New hostname: %s\n", WiFi.hostname().c_str());
    Serial.println("----------");
    
    
    // Start the server
    server.begin();
    //Serial.println("Server started");
    Udp.begin(portServer);
    //Serial.println("Server started UDP");
  }
  else
    Serial.println("don't connect");


  pinMode(POMPA_SOLARE1, OUTPUT);
  digitalWrite(POMPA_SOLARE1, MYLOW);

  pinMode(POMPA_SOLARE2, OUTPUT);
  digitalWrite(POMPA_SOLARE2, MYLOW);
  
  DS18B20_1.begin();
  DS18B20_2.begin();
  DS18B20_3.begin();
  DS18B20_4.begin();
  DS18B20_5.begin();

  temp_1=temp_2=temp_3=temp_4=temp_5=0.00;

  //richiestatempo_time=0;
  salvastato_time=millis()+10000;
  //maxTempSonda=70;

  requestUDP="";

  // Print the IP address
  //Serial.println(WiFi.localIP());
}

void loop()
{
  verificaWebServer();
  leggiTemperature();
  verificaTemperatureSolare();
  verificaWebServerUDP();
  salvaSuIntenetStato();
}

void verificaWebServerUDP()
{
  int noBytes = Udp.parsePacket();
  String received_command = "";
  byte packetBuffer[512];
  
  if ( noBytes ) 
  {
    Serial.print(millis() / 1000);
    Serial.print(":Packet of ");
    Serial.print(noBytes);
    Serial.print(" received from ");
    Serial.print(Udp.remoteIP());
    Serial.print(":");
    Serial.println(Udp.remotePort());
    // We've received a packet, read the data from it
    Udp.read(packetBuffer, noBytes); // read the packet into the buffer

    // display the packet contents in HEX
    for (int i=1;i<=noBytes;i++)
    {
      Serial.print(packetBuffer[i-1],HEX);
      received_command = received_command + char(packetBuffer[i - 1]);
      if (i % 32 == 0)
      {
        Serial.println();
      }
      else Serial.print(' ');
    } // end for
    Serial.println();

    if(requestUDP!=received_command && received_command.indexOf(String(PASSWORD))>0)
    {
        comandiHtml(received_command);
        String ris = rispostaHtml();
        
        Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
        Udp.println(ris);
    /*
        Udp.write("Answer from ESP8266 ChipID#");
        Udp.print(system_get_chip_id());
        Udp.write("#IP of ESP8266#");
        Udp.println(WiFi.localIP());
    */  
        requestUDP=received_command;
        Udp.endPacket();
    }
        
    Serial.println(received_command);
    Serial.println();
  } // end if
}

void leggiTemperature()
{
  unsigned long attesa = 10000;
  
  if(richiestatempo_time >= millis() && richiestatempo_time-millis()<=attesa)
    return;

  Serial.println("\nLeggo le temperature");
  
  float t_1=0, t_2=0, t_3=0, t_4=0, t_5=0;

  for(int i=0 ;i<1 ; i++)
  {
      DS18B20_1.requestTemperatures();
      t_1 = DS18B20_1.getTempCByIndex(0);
      if(t_1!=85 && t_1!=-127) break;
      t_1=0;
  }

  for(int i=0 ;i<1 ; i++)
  {
      DS18B20_2.requestTemperatures();
      t_2 = DS18B20_2.getTempCByIndex(0);
      if(t_2!=85 && t_2!=-127) break;
      t_2=0;
  }

  for(int i=0 ;i<1 ; i++)
  {
      DS18B20_3.requestTemperatures();
      t_3 = DS18B20_3.getTempCByIndex(0);
      if(t_3!=85 && t_3!=-127) break;
      t_3=0;
  }

  for(int i=0 ;i<1 ; i++)
  {
      DS18B20_4.requestTemperatures();
      t_4 = DS18B20_4.getTempCByIndex(0);
      if(t_4!=85 && t_4!=-127) break;
      t_4=0;
  }

  for(int i=0 ;i<1 ; i++)
  {
      DS18B20_5.requestTemperatures();
      t_5 = DS18B20_5.getTempCByIndex(0);
      if(t_5!=85 && t_5!=-127) break;
      t_5=0;
  }

  if(t_5!=0) temp_5=t_5;
  if(t_4!=0) temp_4=t_4;
  if(t_3!=0) temp_3=t_3;
  if(t_2!=0) temp_2=t_2;
  if(t_1!=0) temp_1=t_1;

Serial.println(String(temp_1));
Serial.println(String(temp_2));
Serial.println(String(temp_3));
Serial.println(String(temp_4));
Serial.println(String(temp_5));


  //salvaStatoOnWeb();
  richiestatempo_time = millis()+attesa;
  
}


void verificaTemperatureSolare()
{
  //Serial.println("verificaTemperatureSolare()");
  
//attManualePompeSolare = 0 pompe off
//attManualePompeSolare = 1 pompe on
//attManualePompeSolare = 2 pompe auto
//attManualePompeSolare = 3 pompe auto con solo pompa 2 attiva

  if(attManualePompeSolare==1)
  {
    if(digitalRead(POMPA_SOLARE1)==MYLOW || digitalRead(POMPA_SOLARE2)==MYLOW)
    {
      digitalWrite(POMPA_SOLARE1, MYHIGH);
      digitalWrite(POMPA_SOLARE2, MYHIGH);
    }
    return;
  } 
  else
  if(attManualePompeSolare==0)
  {
    if(digitalRead(POMPA_SOLARE1)==MYHIGH || digitalRead(POMPA_SOLARE2)==MYHIGH)
    {
      digitalWrite(POMPA_SOLARE1, MYLOW);
      digitalWrite(POMPA_SOLARE2, MYLOW);
    }
    return;
  }


  if(temp_2>=maxTempAcquaSolare && (digitalRead(POMPA_SOLARE1)==MYHIGH || digitalRead(POMPA_SOLARE2)==MYHIGH))
  {
       digitalWrite(POMPA_SOLARE1, MYLOW);
       digitalWrite(POMPA_SOLARE2, MYLOW);
       return;
  }
  else
  if(temp_2>=maxTempAcquaSolare-5 && digitalRead(POMPA_SOLARE1)==MYLOW && digitalRead(POMPA_SOLARE2)==MYLOW)
       return;



  if(digitalRead(POMPA_SOLARE2)==MYLOW && temp_1>=(temp_5+minTempStacco) && temp_1>=minTempPannelli)
  {
     if(attManualePompeSolare==2)
       digitalWrite(POMPA_SOLARE1, MYHIGH);
     else
     if(attManualePompeSolare==3)
       digitalWrite(POMPA_SOLARE1, MYLOW);
     
     digitalWrite(POMPA_SOLARE2, MYHIGH);
  }
  else
  if(digitalRead(POMPA_SOLARE2)==MYHIGH && temp_1<=temp_5+minTempStacco)
  {
     digitalWrite(POMPA_SOLARE1, MYLOW);
     digitalWrite(POMPA_SOLARE2, MYLOW);
  }
}

void verificaWebServer()
{
  WiFiClient client = server.available();
  if (!client) 
  {
    return;
  }
  
  //Serial.println("new client");
  
  for(int i=0 ; !client.available() ; i++)
  {
    if(i>=100)
    {
      //Serial.println("client.available "+String(i));
      client.stop();
      return;
    }
    delay(10);
  }
  String req = client.readStringUntil('\r');
  //client.flush();
  if(req.indexOf("favicon") == -1  && req.indexOf(String(PASSWORD))!=-1 )
  {
    comandiHtml(req);
    String ris = "HTTP/1.1 200 OK\r\nContent-Type: text/xml\r\n\r\n"
                 +rispostaHtml();
    client.println(ris);
  }
  client.println();
  //client.flush();
  delay(50);
  //client.stop();
  //Serial.println("Client disonnected");
}

void comandiHtml(String req)
{
     //Serial.println("comandiHtml");

   //START setTime
   int inizio=0;
   if(year() < 2018 && (inizio = req.indexOf(String("time=")))!=-1)
   {
      inizio+=5;

      String tutto = req.substring(inizio, inizio+14);
      int giorno =  tutto.substring(0, 2).toInt();
      int mese =  tutto.substring(2, 4).toInt();
      int anno =  tutto.substring(4, 8).toInt();

      int ora =  tutto.substring(8, 10).toInt();
      int minuti =  tutto.substring(10, 12).toInt();
      int secondi =  tutto.substring(12, 14).toInt();


      setTime(ora, minuti, secondi, giorno, mese, anno);
   }
   //STOP setTime

   //START maxTempAcquaSolare
   if(req.indexOf("maxTempAcquaSolare=")!=-1)
   {
      int start = req.indexOf("maxTempAcquaSolare=")+19;
      String intero = "";
      for( int i=start ; i<req.length() ; i++)
      {
        if(isDigit(req.charAt(i)))
          intero.concat(req.charAt(i));
        else
          break;
      }
      
      if(intero.length()>0 && intero.toInt()>9)
        maxTempAcquaSolare = intero.toInt();
   }
   //STOP maxTempAcquaSolare
   

   //START minTempPannelli
   if(req.indexOf("minTempPannelli=")!=-1)
   {
      int start = req.indexOf("minTempPannelli=")+16;
      String intero = "";
      for( int i=start ; i<req.length() ; i++)
      {
        if(isDigit(req.charAt(i)))
          intero.concat(req.charAt(i));
        else
          break;
      }

      
      if(intero.length()>0 && intero.toInt()>9)
        minTempPannelli = intero.toInt();
   }
   //STOP minTempPannelli

   //START minTempStacco
   if(req.indexOf("minTempStacco=")!=-1)
   {
      int start = req.indexOf("minTempStacco=")+14;
      String intero = "";
      for( int i=start ; i<req.length() ; i++)
      {
        if(isDigit(req.charAt(i)))
          intero.concat(req.charAt(i));
        else
          break;
      }

      
      if(intero.length()>0 && intero.toInt()>0)
        minTempStacco = intero.toInt();
   }
   //STOP minTempStacco

   //START attManualePompeSolare
   if(req.indexOf("attManualePompeSolare=")!=-1)
   {
      int start = req.indexOf("attManualePompeSolare=")+22;
      String intero = "";
      for( int i=start ; i<req.length() ; i++)
      {
        if(isDigit(req.charAt(i)))
          intero.concat(req.charAt(i));
        else
          break;
      }
      
      if(intero.length()>0)
      {
        int tmp = intero.toInt();
        if(tmp>=0 && tmp<=3)
        {
          attManualePompeSolare = intero.toInt();
          if(attManualePompeSolare==3 && digitalRead(POMPA_SOLARE1)==MYHIGH)
            digitalWrite(POMPA_SOLARE1, MYLOW);
          else
          if(attManualePompeSolare==2 && digitalRead(POMPA_SOLARE2)==MYHIGH)
            digitalWrite(POMPA_SOLARE1, MYHIGH);
          
        }
      }
   }
   //STOP attManualePompeSolare


}


String rispostaHtml()
{
  //Serial.println("rispostaHtml");
  
  String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  s+="<domotica>\n";
  s+=String("<dataora>"+String(day())+"/"+String(month())+"/"+String(year())+" "+String(hour())+":"+String(minute())+":"+String(second())+"</dataora>\n");

  s+="\t<pompa_solare1>"+String(!digitalRead(POMPA_SOLARE1))+"</pompa_solare1>\n";
  s+="\t<pompa_solare2>"+String(!digitalRead(POMPA_SOLARE2))+"</pompa_solare2>\n";
  s+="\t<minTempStacco>"+String((int)minTempStacco)+"</minTempStacco>\n";

  s+="\t<attManualePompeSolare>"+String(attManualePompeSolare)+"</attManualePompeSolare>\n";
  s+="\t<minTempPannelli>"+String((int)minTempPannelli)+"</minTempPannelli>\n";
  s+="\t<maxTempAcquaSolare>"+String((int)maxTempAcquaSolare)+"</maxTempAcquaSolare>\n";

  s+="\t<temp_1>"+String(temp_1)+"</temp_1>\n";
  s+="\t<temp_2>"+String(temp_2)+"</temp_2>\n";
  s+="\t<temp_3>"+String(temp_3)+"</temp_3>\n";
  s+="\t<temp_4>"+String(temp_4)+"</temp_4>\n";
  s+="\t<temp_5>"+String(temp_5)+"</temp_5>\n";
  
  s+="</domotica>\n";
  return s;
}

void salvaSuIntenetStato()
{
  if(salvastato_time>millis()) return;

  String parameters="stato_solaretermico="+rispostaHtml();
  
    if (WiFi.status() == WL_CONNECTED)
    {
      HTTPClient http;  //Declare an object of class HTTPClient
      http.begin(wifiClient, "http://raiuno.altervista.org/antifurtopezzillo/salva.php");
      
      http.addHeader( "Host", "raiuno.altervista.org");
      http.addHeader( "User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
      http.addHeader( "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
      http.addHeader( "Accept-Language", "it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3");
      http.addHeader( "Accept-Encoding", "identity;q=1,chunked;q=0.1,*;q=0");
      http.addHeader( "Connection", "keep-alive");
      http.addHeader( "Content-Type", "application/x-www-form-urlencoded");
      http.addHeader( "Content-Length", String(parameters.length()));
      http.addHeader( "Upgrade-Insecure-Requests", "1");

      int httpCode = http.POST(parameters);
      //int httpCode = http.GET();
   
      if (httpCode == HTTP_CODE_OK) //Check the returning code
      {
        delay(1000);
        String payload = http.getString();
        //Serial.println(payload);
        salvastato_time=millis()+300000;

      }
      else
      {
         salvastato_time=millis()+15000;
        //Serial.println(http.errorToString(httpCode));
      }
      http.end();
  }
  else
    salvastato_time=millis()+30000;

}
