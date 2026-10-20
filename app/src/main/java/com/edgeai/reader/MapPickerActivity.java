
package com.edgeai.reader;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.webkit.*;
import android.widget.*;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Locale;

/** OpenStreetMap/Leaflet tap-to-pick map; no Google Maps API key required. */
public class MapPickerActivity extends AppCompatActivity {
    private WebView map;
    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        try { FirebaseManager.getInstance(this).logScreenView("MapPicker"); } catch(Throwable ignored) {}
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.WHITE);
        TextView title=new TextView(this);title.setText("Choose an address · tap the map");title.setTextSize(20);title.setTextColor(Color.rgb(38,39,75));title.setPadding(20,18,20,12);root.addView(title);
        TextView hint=new TextView(this);hint.setText("OpenStreetMap map. Tap a location, then use the selected coordinates.");hint.setTextSize(13);hint.setTextColor(Color.DKGRAY);hint.setPadding(20,0,20,10);root.addView(hint);
        map=new WebView(this);map.getSettings().setJavaScriptEnabled(true);map.getSettings().setDomStorageEnabled(false);map.setWebViewClient(new WebViewClient());map.addJavascriptInterface(new PickerBridge(),"AndroidPicker");
        root.addView(map,new LinearLayout.LayoutParams(-1,0,1));
        Button done=new Button(this);done.setText("Use selected location");done.setOnClickListener(v->map.evaluateJavascript("window.edgeaiSelected || ''",value->{
            if(isFinishing()||isDestroyed()) return;
            String raw=value==null?"":value.replace("\"","");String[] parts=raw.split(",");
            if(parts.length==2){try{double lat=Double.parseDouble(parts[0]),lon=Double.parseDouble(parts[1]);String address=lat+", "+lon;
                try{Geocoder geocoder=new Geocoder(this,Locale.getDefault());List<Address> a=geocoder.getFromLocation(lat,lon,1);if(a!=null&&!a.isEmpty()&&a.get(0).getAddressLine(0)!=null)address=a.get(0).getAddressLine(0)+" ("+lat+", "+lon+")";}catch(Exception ignored){}
                setResult(RESULT_OK,getIntent().putExtra("address",address).putExtra("latitude",lat).putExtra("longitude",lon));finish();
            }catch(Exception e){Toast.makeText(this,"Tap a location on the map first.",Toast.LENGTH_SHORT).show();}}
            else Toast.makeText(this,"Tap a location on the map first.",Toast.LENGTH_SHORT).show();
        }));root.addView(done);setContentView(root);
        map.loadDataWithBaseURL("https://openstreetmap.org",html(),"text/html","UTF-8",null);
    }
    private String html(){return "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'><link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/><style>html,body,#map{height:100%;margin:0}</style></head><body><div id='map'></div><script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script><script>var map=L.map('map').setView([19.0760,72.8777],11);L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'© OpenStreetMap'}).addTo(map);var marker=null;window.edgeaiSelected='';map.on('click',function(e){window.edgeaiSelected=e.latlng.lat.toFixed(6)+','+e.latlng.lng.toFixed(6);if(marker)map.removeLayer(marker);marker=L.marker(e.latlng).addTo(map).bindPopup(window.edgeaiSelected).openPopup();});</script></body></html>";}
    private class PickerBridge { @JavascriptInterface public void unused(){} }
}
