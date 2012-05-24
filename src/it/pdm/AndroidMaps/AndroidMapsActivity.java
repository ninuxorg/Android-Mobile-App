package it.pdm.AndroidMaps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class AndroidMapsActivity extends MapActivity {
    /** Called when the activity is first created. */
	
	private List<Overlay> listPoints;
	private ArrayList<MapPoint> points; //lista dei punti presi tramite Jsonparser
	private MapsOverlay marker;
	private MapsOverlay marker_special;
	private MapView mapView;
	private GeoPoint centerMap= new GeoPoint(41872225,12582114); //coordinate per centro in Roma
	//private MapsParserXml parserSimple;
	private MapsParserJson parserJson;
	private GpsManager local;
	private  final String TAG = "AndroidMapsActivity - ";
	static private final int ID_CONFIRM_GPS= 0;
	static private final int ID_SEARCH_NODES_POSITION= 1;
	static private final int ID_SEARCH_NODES_BY_NAME= 2;
	static private final int ID_SEARCH_NODES= 3;
	/*ASSOLUTAMENTE DA OTTIMIZZARE*/
	int prec=0;
	/******************************/
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        	
        try{
        	checkConnection();
        	setContentView(R.layout.main);
   
        	mapView = (MapView) findViewById(R.id.mapview);
        	mapView.setBuiltInZoomControls(true);
        	mapView.getController().setCenter(centerMap); //setto il centro in Roma
        	mapView.getController().setZoom(11);
        
        	points=new ArrayList<MapPoint>();
        
        	listPoints = mapView.getOverlays();
        	Drawable drawable = this.getResources().getDrawable(R.drawable.antenna);
        	Drawable drawable2 = this.getResources().getDrawable(R.drawable.red_marker);
        	marker = new MapsOverlay(drawable, this);
        	marker_special = new MapsOverlay(drawable2, this);
        	
        	parserJson= new MapsParserJson();
        	
        	//batterymng=new BatteryManager(getApplicationContext());
        	//batterymng.showBatteryLevel();
        	
        	String lastDateTime=getLastUpdate();
			String currentDateTime=getDateTimeSystem();
				
			/*se ho superato il numero massimo di giorni definiti(controllo vedendo 
			* lo scarto in giorni tra l'ultimo aggiornamento nel DB e la data corrente) 
			* tento l'aggiornamento del DB. Altrimenti carico i dati da DB.*/
			double difference=CalendarManager.differenceDates(currentDateTime, lastDateTime);
			/*Log.v("Stringa 1: ", currentDateTime);
			Log.v("Stringa 2: ", lastDateTime);
			Log.v("Differenza tra i giorni: ", ""+difference);
			Log.v("La tabella dei nodi è vuota? Risposta: ",""+isEmptyTableNodes());*/
			
			if(difference>HomeActivity.MAX_DAYS_FOR_UPDATE || difference<0 || isEmptyTableNodes()){
		        	
				getNodes(getJsonFromUrl()); //parsa il file Json recuperato tramite richiesta GET 
		       	//HTTP utilizzando architettura REST, popola quindi la lista di nodi principale
				drawNodes(points); //crea i punti sulla mappa a partire dalla lista di MapPoint
		       	insertNodesDB(); //sovrascrive i nodi nella tabella nodes in DB con i nuovi nodi.
		        	
		        //----------------------------------------------------------------------//
			}else{
				
				getNodesFromDB();//legge sul DB e popola la lista di nodi principale.
				drawNodes(points); //crea i punti sulla mappa a partire dalla lista di MapPoint
			}
						
				
		}        	
        catch (ConnectionException e){
			showToastLong(e.getMessage());
			closeApplication();
		}
        catch (DBOpenException e) {
        	Log.e("DB_ERROR - OPEN DATABASE",e.getMessage());
		} catch (InterruptedException e) {
			Log.e("Error Interruption: ",e.getMessage());
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			Log.e("Error Execution: ",e.getMessage());
		}

            
        
        
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
                   AndroidMapsActivity.this.finish();
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
    
    public boolean isEmptyTableNodes() throws DBOpenException {
    	DbHelper dbh=new DbHelper(getApplicationContext()); 
    	DbManager dbmanager= new DbManager(dbh,getApplicationContext());
    	return dbmanager.isEmptyTableNodes();
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

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public GeoPoint getCenterMap(){
		return centerMap;
	}
	
	public void drawNodes(final ArrayList<MapPoint> m){
		
		
		AsyncTask<String, Void, Void> creationPoints = new AsyncTask<String, Void, Void>(){

			@Override
			protected Void doInBackground(String ...strings) {
				
				for(int i=0;i<m.size();i++){
					
					MapPoint info=m.get(i);
					GeoPoint point = new GeoPoint(info.getLatitude(), info.getLongitude());
					OverlayItem overlayitem = new OverlayItem(point, info.getName(), "Lat: "+info.getLatitude()+"\n" +
							"Long: "+info.getLongitude()+"\n"+"Status: "+info.getStatus()+"\n"+"Slug: "+info.getSlug()+"\n"
							+"Jslug: "+info.getJslug()+"\n");
					marker.addOverlay(overlayitem);
				}
				
				listPoints.add(marker);
				
				return null;
			}
			
		};
		
		creationPoints.execute();
		
	}
	
	public void clearNodes(){
		points.clear();
		Drawable drawable = this.getResources().getDrawable(R.drawable.antenna);
    	marker = new MapsOverlay(drawable, this);
	    mapView.getOverlays().clear();
	    mapView.invalidate();
	    

	}
	
	/*public void getXmlSimple(int source){
         //otteniamo un istanza del nostro parser
		InputStream input=getApplicationContext().getResources().openRawResource(source);
        parserSimple.parseXml(input);//usiamo il parser per scandire il documento xml fornito
        points=parserSimple.getParsedData();
	}*/
	
	public void getNodes(String json_list){
       parserJson.parseJson(json_list);//usiamo il parser per scandire il contenuto fornito
       points=parserJson.getParsedData(); //preleva la lista dei nodi parsati e popola quella della activity.
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
        MenuItem h=menu.add(0,1,0,"Rileva Posizione");
        h.setIcon(R.drawable.gps);
        
        h.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				showDialog(ID_CONFIRM_GPS);
				return true;
			}
		});
        
        MenuItem search=menu.add(0,1,0,"Cerca Nodi");
        search.setIcon(R.drawable.ic_menu_search);
        
        search.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				showDialog(ID_SEARCH_NODES);
				return true;
			}
		});
        
        /*MenuItem search_by_name=menu.add(0,1,0,"Cerca Nodi Per Nome");
        search_by_name.setIcon(R.drawable.ic_menu_search);
        
        search_by_name.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				showDialog(ID_SEARCH_NODES_BY_NAME);
				return true;
			}
		});*/
        
        return true;
        
	}
	
	private void insertNodesDB(){
		DbHelper dbh=new DbHelper(getApplicationContext()); 
    	DbManager dbmanager= new DbManager(dbh,getApplicationContext());
    	dbmanager.setListPoint(points);
    	dbmanager.execute(4); //this inserts nodes into database
	}
	
	private void getNodesFromDB() throws InterruptedException, ExecutionException{
		DbHelper dbh=new DbHelper(getApplicationContext()); 
    	DbManager dbmanager= new DbManager(dbh,getApplicationContext());
    	@SuppressWarnings("unchecked")
		ArrayList<MapPoint> map=(ArrayList<MapPoint>)dbmanager.execute(0).get();
    	points=map;
	}
	
	private String getLastUpdate() throws DBOpenException{
		DbHelper dbh=new DbHelper(getApplicationContext()); 
    	DbManager dbmanager= new DbManager(dbh,getApplicationContext());
    	return dbmanager.getLastUpdate();
	}
	
	private String getDateTimeSystem(){
		return CalendarManager.formatDateTimeForDatabase(CalendarManager.getCurrentDateTime());
	}
	
    protected Dialog onCreateDialog(int id) {
    	
		final DbHelper dbh=new DbHelper(getApplicationContext());
		final Context contx=getApplicationContext();
    	
    	switch(id){
    		case ID_CONFIRM_GPS:
    			final Dialog dialog_Gps = new Dialog(this);
    			dialog_Gps.setContentView(R.layout.search_gps);
    			dialog_Gps.setTitle("Localizza la tua posizione");
    			
				Button ok=(Button)dialog_Gps.findViewById(R.id.confirm_gps);
				Button undo=(Button)dialog_Gps.findViewById(R.id.undo_request);
				
				ok.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
		    			getPosition();
		    			dialog_Gps.dismiss();
					}
				});
    		
    			undo.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
					 	dialog_Gps.dismiss();
					}
				});
    			
    		return dialog_Gps;
    		
    	    case ID_SEARCH_NODES_BY_NAME:
    			final Dialog dialog_Search_Node_By_Name = new Dialog(this);
    			dialog_Search_Node_By_Name.setContentView(R.layout.search_nodes_by_name);
    			dialog_Search_Node_By_Name.setTitle("Cerca un nodo per Nome");
    			
    			Button search_by_name=(Button)dialog_Search_Node_By_Name.findViewById(R.id.confirm_search_by_name);
    			Button undo_by_name=(Button)dialog_Search_Node_By_Name.findViewById(R.id.undo_request_by_name);
    			final EditText name=(EditText)dialog_Search_Node_By_Name.findViewById(R.id.name_to_search);
    			
    			search_by_name.setOnClickListener(new OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
    					dialog_Search_Node_By_Name.dismiss();
    			    	ArrayList<MapPoint> searched;
    			    	DbManager dbmanager= new DbManager(dbh,contx);
    			    	
    			    	String name_to_search;
    			    	
    			    	try {
    			    		name_to_search=name.getText().toString();
    			    		Log.v("","Fields\n\nname="+name);
    						
    					}catch (NullPointerException e) {
    						showToastLong("All fields are required.");
    						return;
    					}
    			    	
    					try {
    						searched = (ArrayList<MapPoint>) dbmanager.execute(3,name_to_search).get();
    						Log.v("Searched what's?", searched.toString());
    						if(searched.isEmpty()){
    							showToastShort("The search hasn't produced results!");
    							return;
    						}
    						clearNodes();//cancello tutti gli Overlay e rispettivi piani dalla mappa.
    						drawNodes(searched);
    					}catch (IndexOutOfBoundsException e ){
    						Log.e("ERROR DB NULL POINTER LIST: ",e.getMessage());
    						showToastShort("The search hasn't produced results!");
    					}
    					catch(NullPointerException e){
    						Log.e("ERROR DB NULL POINTER LIST: ",e.getMessage());
    						showToastShort("The search hasn't produced results!");
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						//Log.e("",e.getMessage());
    					} catch (ExecutionException e) {
    						// TODO Auto-generated catch block
    						//Log.e("",e.getMessage());
    					}
    				}
    			}); 
    			
    			undo_by_name.setOnClickListener(new OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
    					dialog_Search_Node_By_Name.dismiss();
    				}
    			});
    			
    		return dialog_Search_Node_By_Name;		
    
    case ID_SEARCH_NODES:
    	return createAlertDialog();
    		
    case ID_SEARCH_NODES_POSITION:
    	
		final Dialog dialog_Search_Node = new Dialog(this);
		dialog_Search_Node.setContentView(R.layout.search_nodes_position);
		dialog_Search_Node.setTitle("Cerca un nodo per coordinate");
		
		Button search=(Button)dialog_Search_Node.findViewById(R.id.confirm_search);
		Button undo2=(Button)dialog_Search_Node.findViewById(R.id.undo_request);
		final EditText latitude=(EditText)dialog_Search_Node.findViewById(R.id.latitude);
		final EditText longitude=(EditText)dialog_Search_Node.findViewById(R.id.longitude);
		final SeekBar precision=(SeekBar)dialog_Search_Node.findViewById(R.id.precision);
		final TextView label_precision=(TextView)dialog_Search_Node.findViewById(R.id.label_precision2);
		
		precision.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				if(precision!=null){
					prec=precision.getProgress();
				}
				
				
				label_precision.setText(""+prec);
				
				
			}
		});

		
		search.setOnClickListener(new OnClickListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v) {
				
		    	ArrayList<MapPoint> searched;
		    	DbManager dbmanager= new DbManager(dbh,contx);
		    	Integer lat=0;
		    	Integer lng=0;
		    	try {
		    		lat=Integer.parseInt(latitude.getText().toString());
		    		lng=Integer.parseInt(longitude.getText().toString());
		    		Log.v("","Fields\n\nlat="+lat+"\nlng="+lng+"\nprec="+prec);
					
				}catch (NumberFormatException e) {
					showToastLong("All fields are required.");
					return;
				}
		    	
				try {
					searched = (ArrayList<MapPoint>) dbmanager.execute(1,lat,lng,prec,prec).get();
					if(searched.isEmpty()){
						showToastShort("The search hasn't produced results!");
						return;
					}
					clearNodes();//cancello tutti gli Overlay e rispettivi piani dalla mappa.
					drawNodes(searched);
					dialog_Search_Node.dismiss();
				}catch (IndexOutOfBoundsException e ){
					Log.e("ERROR DB NULL POINTER LIST: ",e.getMessage());
					showToastShort("The search hasn't produced results!");
				}
				catch(NullPointerException e){
					Log.e("ERROR DB NULL POINTER LIST: ",e.getMessage());
					showToastShort("The search hasn't produced results!");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//Log.e("",e.getMessage());
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					//Log.e("",e.getMessage());
				}
			}
		}); 
		
		undo2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			 	dialog_Search_Node.dismiss();
			}
		});
		
	return dialog_Search_Node;
    }
    	
    		return null;
}
    
    
    public AlertDialog createAlertDialog(){
		
    	final CharSequence[] items=new CharSequence []{"Per Nome","Per Posizione"};
    	AlertDialog.Builder builder=new AlertDialog.Builder(this);
    	builder.setTitle("Ricerca Nodi");

    	builder.setItems(items, new DialogInterface.OnClickListener() {

    	@Override
    	public void onClick(DialogInterface dialog, int which) {
    		switch(which){
    		case 0:
    			showDialog(ID_SEARCH_NODES_BY_NAME);
    		break;
    		case 1:
    			showDialog(ID_SEARCH_NODES_POSITION);
    		break;
    		}
    	}
    	});

    	return builder.show();
    }
    
	
	public String getJsonFromUrl(){
		
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
	
	public void getPosition(){
		/* Use the LocationManager class to obtain GPS locations */

		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	local= new GpsManager();
    	mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, local);
		//Location loc=mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		//return loc;
	}
			

	
	
	
	private class GpsManager implements LocationListener 
	  {
	    double longitudine;
	    double latitudine;
	    final private double factor=1E6; 

	    public double setLongitudine(double longitudine) {
	        return this.longitudine = longitudine;
	    }

	    public double setLatitudine(double latitudine) {
	        return this.latitudine = latitudine ;
	    }

	    public void onLocationChanged(Location location) {
	      if (location != null) {
	    	  
	    	  setLatitudine(location.getLatitude());
	    	  setLongitudine(location.getLongitude());


	        GeoPoint point = new GeoPoint(
	            (int) (location.getLatitude()*factor), 
	            (int) (location.getLongitude()*factor));
	        
	        MapController mapController=mapView.getController();
	        mapController.animateTo(point);
	        mapController.setZoom(10);

	       
	        //listOfOverlays.clear();
	        
	        OverlayItem overlayitem = new OverlayItem(point, "Tu sei qui!", "Lat: "+(int)latitudine*factor+"\n" +
					"Long: "+(int)longitudine*factor+"\n");
			marker_special.addOverlay(overlayitem);
			listPoints.add(marker_special);
	      }
	        

	      }

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

	    }

	
	
	









}





