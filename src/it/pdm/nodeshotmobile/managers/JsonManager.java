package it.pdm.nodeshotmobile.managers;

import it.pdm.nodeshotmobile.entities.MapPoint;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonManager {
	
	private JSONObject jObject;
	private FileManager fmng;
	private ArrayList<MapPoint> parsedData;
	
	public JsonManager() {
		parsedData=new ArrayList<MapPoint>();
		fmng=new FileManager();
	}
	
	/**
     * A debug method that write on LogCat a vDebug given string
     * @param		debugString the string to be written
     */
	static void vDebug(String debugString){         //metodi di convenienza
        Log.v("DomParsing", debugString+"\n");
	}
	
	/**
     * A debug method that write on LogCat an eDebug given string
     * @param		debugString the string to be written
     */
    static void eDebug(String debugString){
            Log.e("DomParsing", debugString+"\n");
    }
	
	public ArrayList<MapPoint> getParsedData() {  //metodo di accesso alla struttura dati
        return parsedData;
	}
	
	/**
     * This method obtains all nodes from json file using json parsing JSON Parser. 
     * @param	jString string who contains a JSON Document
     */
	public void parseJson(String jString){
			JSONObject mapObject;
			try {
			
				jObject = new JSONObject(jString);	
				mapObject = jObject.getJSONObject("active");
				parseTreeJson(mapObject);
				
				jObject = new JSONObject(jString);
				mapObject = jObject.getJSONObject("potential");
				parseTreeJson(mapObject);
				
				jObject = new JSONObject(jString);
				mapObject = jObject.getJSONObject("hotspot");
				parseTreeJson(mapObject);
				
				
			
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				eDebug(e.toString());
			}
		
	}
	
	public void parseTreeJson(JSONObject obj){
		//recupero tutti i nomi dei campi(a loro volta sono JSONObject oppure JSONArray) dell'oggetto 
		//radice dell'albero
	    Iterator<String> myIter = obj.keys();
	    List<String> names = new ArrayList<String>();

	    while(myIter.hasNext()){
	        names.add(myIter.next());
	    }
	    
	    for(int i=0;i<names.size();i++){
	    	JSONObject entryObject = obj.optJSONObject(names.get(i));
	    	MapPoint point=new MapPoint();
	    	
	    	try{
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
	    	
	    	}catch(NullPointerException e){} catch (JSONException e) {
				Log.e("JsonManager", e.toString());
			}
	    	}

	}
	
	//created mainly for mixare plugin
	/**
     * This method is used to creare a JSON Document that will be used by Mixare plug-in (AUgmented Reality).
     * @param		map an ArrayList that contains MapPoint nodes
     * @return		a result String
     */
	public String createJson(ArrayList<MapPoint> map){
		
		String comma=",";
		int count=map.size();
		StringBuilder builder = new StringBuilder();

		builder.append("{" +
				"\"status\": \"OK\"," +
				"\"num_results\": "+count+"," +
				"\"results\": [");
		for(MapPoint point : map){
			count--;
			builder.append("{" +
					"\"id\":\""+point.getId()+"\","+
					"\"lat\":\""+((double)point.getLatitude())/1E6+"\","+
					"\"lng\":\""+((double)point.getLongitude())/1E6+"\","+
					"\"elevation\":\""+0+"\","+
					"\"title\":\""+point.getName()+"\","+
					"\"distance\":\"\","+
					"\"has_detail_page\":\""+0+"\","+
					"\"webpage\":\"\"}");
			if(count>0){
				builder.append(comma);
			}
		}
		builder.append("]}");
		
		String result=builder.toString();
		
		try {
			fmng.writeFile("/sdcard/download/nma_mixare.json", result);
		} catch (FileNotFoundException e) {
			eDebug(e.toString());
		}
		
		
		
		vDebug(result);
		return result; 
	}
	
	/**
     * this method is used to fill an ArrayList of MapPoint used by the main activity with the nodes parsed from the given
     * JSON Document.
     * @param		json_list a String that contains a JSON well-formatted Document
     */
	public void getNodes(String json_list){
	       parseJson(json_list);//usiamo il parser per scandire il contenuto fornito
	       MapPoint.points=getParsedData(); //preleva la lista dei nodi parsati e popola quella della activity.
	}
	
}
