package it.pdm.AndroidMaps;


import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ListNodesActivity extends ListActivity {
		
		ArrayList<MapPoint> points;
		MapsParserJson parserJson;
		ListView listNodes;
		ListManager listmng;
		Integer current_item;
		private final String TAG = "ListNodesActivity - ";
		static private final int ID_SHOW_NODE= 0;
		
		private DbHelper dbh;
		private Context contx;
		private DbManager dbmanager;
		private EditText field_search;
        
        public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	
        	try{
             	checkConnection();
             	setContentView(R.layout.list_nodes);
             	
        		dbh=new DbHelper(getApplicationContext());
        		contx=getApplicationContext();
            	dbmanager= new DbManager(dbh,getApplicationContext());
        	
             	parserJson= new MapsParserJson();
             	listNodes=getListView();
             	listNodes.setTextFilterEnabled(true);


             	field_search=(EditText)findViewById(R.id.name_to_search);
             	listmng=new ListManager(this,listNodes);
        	
             	String lastDateTime=getLastUpdate();
				String currentDateTime=getDateTimeSystem();
				
				double difference=CalendarManager.differenceDates(currentDateTime, lastDateTime);
			
				if(difference>HomeActivity.MAX_DAYS_FOR_UPDATE || difference<0 || isEmptyTableNodes()){
		        	
					getJson(getResponse()); //parsa il file Json recuperato tramite richiesta GET 
					//HTTP utilizzando architettura REST, popola quindi la lista di nodi principale
		   	    	fillListNodes(); //riempie la lista principale con gli id e i nomi dei Nodi esistenti
		       		insertNodesDB(); //sovrascrive i nodi nella tabella nodes in DB con i nuovi nodi.
		        	
		        //----------------------------------------------------------------------//
				}else{
				
					getNodesDB();//legge sul DB e popola la lista di nodi principale.
					fillListNodes(); //crea i punti sulla mappa a partire dalla lista di MapPoint
				}
				
				
             	//setListAdapter(new ArrayAdapter<String>(contx, R.layout.row,prova));
				
				
				field_search.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						Log.v("String taken", s.toString());
						listmng.filter(s.toString());
					}
				});
				
				listNodes.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {
						String idItem=""+(Integer)listmng.getMap().get(arg2).get("id");
						current_item=Integer.parseInt(idItem);
						Log.v("Current Item in click:", ""+current_item);
						showDialog(0);
						
					}
		        	
				});
				
        	}catch (ConnectionException e) {
        		showToastLong(e.getMessage());
    			closeApplication();
			}catch (DBOpenException e) {
        		Log.e(TAG, e.getMessage());
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			} catch (ExecutionException e) {
				Log.e(TAG, e.getMessage());
			}
        	
        	
        }
        
        
        
        public void showToastLong(String text){
        	Toast t=Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        	t.setGravity(Gravity.CENTER,0,0);
        	t.show();
        }
        
        
        public void showToastShort(String text){
        	Toast t=Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        	t.show();
        }
        
        public boolean isEmptyTableNodes() throws DBOpenException {
        	DbHelper dbh=new DbHelper(getApplicationContext()); 
        	DbManager dbmanager= new DbManager(dbh,getApplicationContext());
        	return dbmanager.isEmptyTableNodes();
        }
    	private String getLastUpdate() throws DBOpenException{
    		return dbmanager.getLastUpdate();
    	}
    	
    	public String getResponse(){
    		
    		AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>(){

    			@Override
    			protected String doInBackground(Void...voids) {
    		
    		RestClient client = new RestClient(HomeActivity.STRING_CONNECTION);

    		try {
    		    client.Execute(RequestMethod.GET);
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    			Log.v("Response from MapServer: ",client.getResponse());
    			return client.getResponse();
    		}
    	};
    	
    	try {
    		
    			String result=asyncTask.execute().get();
    			return result;
    	
    	}catch (InterruptedException e) {
    		Log.e(TAG, e.toString());
    		return null;
    	} catch (ExecutionException e) {
    		Log.e(TAG, e.toString());
    		return null;
    	}
    	
    	
    	}
    	
    	private String getDateTimeSystem(){
    		return CalendarManager.formatDateTimeForDatabase(CalendarManager.getCurrentDateTime());
    	}
        
    	public void getJson(String list){
    	       parserJson.parseJson(list);//usiamo il parser per scandire il documento Json fornito
    	       points=parserJson.getParsedData(); //popoliamo la lista 
    	}
        
    	private void getNodesDB() throws InterruptedException, ExecutionException{
    		DbHelper dbh=new DbHelper(getApplicationContext()); 
        	DbManager dbmanager= new DbManager(dbh,getApplicationContext());
        	@SuppressWarnings("unchecked")
    		ArrayList<MapPoint> map=(ArrayList<MapPoint>)dbmanager.execute(0).get();
        	points=map;
    	}
    	
    	private void insertNodesDB(){
    		DbHelper dbh=new DbHelper(getApplicationContext()); 
        	DbManager dbmanager= new DbManager(dbh,getApplicationContext());
        	dbmanager.setListPoint(points);
        	dbmanager.execute(4);
    	}
    	
    	private void fillListNodes(){
    		listmng.load(points);
    	}
    	
        public void checkConnection() throws ConnectionException{
        	
        	if(!isOnline()){
        		throw new ConnectionException();
        	}
        	
        }
        
        public void closeApplication(){
        	
            Thread thread = new Thread(){
                @Override
               public void run() {
                    try {
                       Thread.sleep(3500); // As I am using LENGTH_LONG in Toast
                       ListNodesActivity.this.finish();
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
                }  
              };
              
              thread.start();

        	
        }
        
        public boolean isOnline() throws ConnectionException {
        	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        	    NetworkInfo ni = cm.getActiveNetworkInfo();
        	    if (ni!=null && ni.isAvailable() && ni.isConnected()) {
        	        return true;
        	    } else {
        	        return false;  
        	    }	
        }
        
        @Override
        protected void onPrepareDialog(int id, Dialog dialog) {
        	// TODO Auto-generated method stub
        	super.onPrepareDialog(id, dialog);
        	
        	DbManager dbg=new DbManager(dbh,contx);
        	
        	dialog.setTitle("Info Nodo");
			dialog.setContentView(R.layout.show_node);
			
			TextView data=(TextView) dialog.findViewById(R.id.show_node);
		 	
		ArrayList<MapPoint> map;
		try {
			Log.v("Current id=", ""+current_item);
			map = (ArrayList<MapPoint>)dbg.execute(6,current_item).get();
			MapPoint unique=map.get(0);
			String body="Id: "+unique.getId()+"\nName: "+unique.getName()+"\nLat: "+unique.getLatitude()+"\n" +
					"Long: "+unique.getLongitude()+"\nStatus: "+unique.getStatus()+"\n";
			data.setText(body);
		} catch (InterruptedException e) {
	   		Log.e(TAG, e.toString());
		} catch (ExecutionException e) {
	   		Log.e(TAG, e.toString());
		}
        	
        }
        
        @SuppressWarnings("unchecked")
		protected Dialog onCreateDialog(int id) {
        	
        	
        	switch(id){
        		case ID_SHOW_NODE:
        			Dialog dialog_show = new Dialog(this);
        			
				 		
        		return dialog_show;
        		default:
        			return null;
        		
        	}
	
        }
        

        
}
