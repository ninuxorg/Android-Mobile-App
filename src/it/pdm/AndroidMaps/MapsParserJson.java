package it.pdm.AndroidMaps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MapsParserJson {
	
	private JSONObject jObject;
	private ArrayList<MapPoint> parsedData=new ArrayList<MapPoint>();
	
	public MapsParserJson() {
		// TODO Auto-generated constructor stub
	}
	
	static void vDebug(String debugString){         //metodi di convenienza
        Log.v("DomParsing", debugString+"\n");
	}
	

    static void eDebug(String debugString){
            Log.e("DomParsing", debugString+"\n");
    }
	
	public ArrayList<MapPoint> getParsedData() {  //metodo di accesso alla struttura dati
        return parsedData;
	}
	
	public void parseJson(String jString){
		
			

			JSONObject mapObject;
			try {
			
			jObject = new JSONObject(jString);	
				
			mapObject = jObject.getJSONObject("active");
			
			//recupero tutti i nomi dei campi(a loro volta sono JSONObject oppure JSONArray) dell'oggetto 
			//radice dell'albero
		    Iterator<String> myIter = mapObject.keys();
		    List<String> names = new ArrayList<String>();

		    while(myIter.hasNext()){
		        names.add(myIter.next());
		    }
		    
		    for(int i=0;i<names.size();i++){
		    	JSONObject entryObject = mapObject.optJSONObject(names.get(i));
		    	MapPoint point=new MapPoint();
		    	
		    	String status=entryObject.getString("status");
		    	point.setStatus(status);
		    	
		    	int id=entryObject.getInt("id");
		    	point.setId(id);
		    	
		    	String name=entryObject.getString("name");
		    	point.setName(name);
		    	
		    	String slug=entryObject.getString("slug");
		    	point.setSlug(slug); 
		    	
		    	String jslug=entryObject.getString("jslug");
		    	point.setJslug(jslug); 
		    	
		    	double lat=entryObject.getDouble("lat");
		    	point.setLatitude((int)(lat*1000000));
		    	
		    	double lng=entryObject.getDouble("lng");
		    	point.setLongitude((int)(lng*1000000));
		    	
		    	parsedData.add(point);
		    	vDebug("---------------------------------------------------------");
		    	vDebug(point.toString());
		    	vDebug("---------------------------------------------------------");
		    	
		    }

		
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				eDebug(e.toString());
			}
		
	}
	
}
