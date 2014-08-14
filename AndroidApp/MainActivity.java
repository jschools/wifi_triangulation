package com.example.myfirstapp;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
	
	WifiManager wifi; 
	List<ScanResult> results;
	StringBuilder sb;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = wifi.getScanResults();
        sb = new StringBuilder();
       try
       {
           for(int i = 0; i < results.size(); i++){
               //sb.append(new Integer(i+1).toString() + ".");
               //sb.append("\n\nSSID:-").append((results.get(i).SSID).toString());
               sb.append((results.get(i).BSSID).toString());
               sb.append("|").append((results.get(i).level));
               //sb.append("\nFrequency:-").append((results.get(i).frequency));
               if (i<results.size()-1){
            	   sb.append("|");
               }
           }
           appendLog(sb.toString());
       } finally {
    	   
       }
        
        finish();
        return;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void appendLog(String text)
    {       
       File logFile = new File("sdcard/log.file");
       if (!logFile.exists())
       {
          try
          {
             logFile.createNewFile();
          } 
          catch (IOException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
       try
       {
          //BufferedWriter for performance, true to set append to file flag
          BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, false)); 
          buf.append(text);
          buf.newLine();
          buf.close();
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }
}
