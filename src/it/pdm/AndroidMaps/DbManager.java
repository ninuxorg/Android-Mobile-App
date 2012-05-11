package it.pdm.AndroidMaps;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;


class DbManager extends AsyncTask<Object,String,Object> {
    
    private MapPoint mp; //
    private ArrayList<MapPoint> map;
    private DbHelper dbHelp;
    private Context current;
       
    DbManager(DbHelper helper,Context context){
        this.mp=null;
        this.map=null;
        this.dbHelp=helper;
        this.current=context;

}
    
    
  //----------------GESTIONE DB ------------------------------------------//

    /* setChoice(Integer doSomething)
       0: getNodes();
       1: getNodeByPosition(lat, lng, grade_lat, grade_lng);
       2: getNodeByPosition(lat, lng);
       3: getNodeByName(name);
       4: insertNodes(map);
       5: insertNode(mp);
       6: getNodeById(id);
    */

    @Override
    protected Object doInBackground(Object... params) throws MapPointsException,PointException {
        // TODO Auto-generated method stub

    	Integer lat;
    	Integer lng;
    	Integer grade_lat;
    	Integer grade_lng;
    	
    	switch((Integer)params[0]){

    	case 0:
    		
			
			try {
				ArrayList<MapPoint> m;
				m = getNodes();
				return m;
			} catch (DBOpenException e) {
				Log.e("DB_ERROR - OPEN DATABASE",e.getMessage());
			}
    	
    	case 1:
    		
    		lat=(Integer)params[1];
    		lng=(Integer)params[2];
    		grade_lat=(Integer)params[3];
    		grade_lng=(Integer)params[4];
			try {
				ArrayList<MapPoint> m;
				m = getNodeByPosition(lat, lng, grade_lat, grade_lng);
				return m;
			} catch (DBOpenException e) {
				Log.e("DB_ERROR - OPEN DATABASE",e.getMessage());
			}
    			
    		
    		
    	case 2:

    		lat=(Integer)params[1];
    		lng=(Integer)params[2];
    		
    		try {
				return getNodeByPosition(lat, lng);
			} catch (DBOpenException e) {
				Log.e("DB_ERROR - OPEN DATABASE",e.getMessage());
			}
    		
    	case 3:

    		String tosearch=(String)params[1];
    		
    		try {
				return getNodeByName(tosearch);
			} catch (DBOpenException e) {
				Log.e("DB_ERROR - OPEN DATABASE",e.getMessage());
			}	
    		
    	case 4:
    		
    		
    		if(map.equals(null)){
    		
    			throw new MapPointsException();
    		
    		}else{
    			
        		try {
					insertNodes(map);
				} catch (DBOpenException e) {
					Log.e("DB_ERROR - OPEN DATABASE",e.getMessage());
				}
    		}    		
    	break;
    	
    	
    	case 5:
    		
    		if(mp.equals(null)){
    	
    			throw new PointException();
    		
    		}else{
    		
    			try {
					insertNode(mp);
				} catch (DBOpenException e) {
					Log.e("DB_ERROR - OPEN DATABASE",e.getMessage());
				}
    		
    		}
        	break;
    	case 6:
    			
    		
    			try {
    				Integer id=(Integer)params[1];
    				getNodesById(id);
    			} catch (DBOpenException e) {
    				Log.e("DB_ERROR - OPEN DATABASE",e.getMessage());
    			}
    			
    	    	break;	
    	default:
    		return null;
    	}
    	
    	
		return null;
    	
        
        
       
    }
    
    public SQLiteDatabase openDBW() throws DBOpenException{
    	
    	SQLiteDatabase db;
    	try{
    		db=dbHelp.getWritableDatabase();
    		return db;
    	}catch (SQLiteException e) {
        	throw new DBOpenException();
        }
    }
    
    public SQLiteDatabase openDBR() throws DBOpenException{
    	
    	SQLiteDatabase db;
        
    	try{
    		db=dbHelp.getReadableDatabase();
    		return db;
    	}catch (SQLiteException e) {
        	throw new DBOpenException();
        }
    }
    
    public void truncateTable(String table) throws DBOpenException{
    	SQLiteDatabase db=null;
    	db=openDBW();
    	db.execSQL("DELETE FROM "+table);    //cancello il contenuto della tabella nodes
        db.close();
    }
    
    public void deleteTable(String table) throws DBOpenException{
    	SQLiteDatabase db=null;
    	db=openDBW();
    	db.execSQL("DROP TABLE "+table);    //cancello il contenuto della tabella nodes
        db.close();
    }
    
    public void setPoint(MapPoint point){
    	this.mp=point;
    }
    
    public void setListPoint(ArrayList<MapPoint> m){
    	this.map=m;
    }
    
    private void insertNodes(ArrayList<MapPoint> map) throws DBOpenException{
    	deleteNodes();
    	for(MapPoint point : map){
    		insertNode(point);
    	}
    	insertUpdate();
    }
    
    private ArrayList<MapPoint> getNodes() throws DBOpenException{
    	
    	
    	 ArrayList<MapPoint> valori=new ArrayList<MapPoint>();
         Cursor cursor;
         SQLiteDatabase db=null;

         db=openDBR();
            
         //select * from Nodes
         
         cursor= db.query(DbHelper.TABLE_NAME1, null, null, null, null, null, null); //ottengo un cursore che punta alle entry ottenute dalla query
         cursor.moveToFirst();
         do{
             MapPoint node=new MapPoint();
             node.setId(cursor.getInt(cursor.getColumnIndex("_id")));
             node.setName(cursor.getString(cursor.getColumnIndex("name")));
             node.setStatus(cursor.getString(cursor.getColumnIndex("status")));
             node.setSlug(cursor.getString(cursor.getColumnIndex("slug")));
             node.setLatitude(cursor.getInt(cursor.getColumnIndex("lat")));
             node.setLongitude(cursor.getInt(cursor.getColumnIndex("lng")));
             node.setJslug(cursor.getString(cursor.getColumnIndex("jslug")));
             
             valori.add(node);
             cursor.moveToNext();
         }while(!cursor.isAfterLast());

             cursor.close();
             db.close();
         
     
         return valori;
    	
    	
    	
    }
    
    private ArrayList<MapPoint> getNodeByPosition(Integer lat, Integer lng, Integer grade_lat, Integer grade_lng) throws DBOpenException{
        
        ArrayList<MapPoint> valori=new ArrayList<MapPoint>();
        Cursor cursor;
        SQLiteDatabase db=null;
        Integer min_lat=lat-grade_lat;
        Integer max_lat=lat+grade_lat;
        Integer min_lng=lng-grade_lng;
        Integer max_lng=lng+grade_lng;
        db=openDBR();
           
        //select * from Nodes where (lat between [min_lat] AND [max_lat]) and (lng between [min_lng] AND [max_lng]));
        String sql="SELECT * FROM "+DbHelper.TABLE_NAME1+" WHERE (lat BETWEEN " +min_lat.toString()+ " AND "+max_lat.toString()+") " +
        		"AND (lng BETWEEN "+min_lng.toString()+ " AND "+max_lng.toString()+")";
        cursor= db.rawQuery(sql,null); //ottengo un cursore che punta alle entry ottenute dalla query
        cursor.moveToFirst();
        do{
        	if(cursor!=null && cursor.getCount()>0){
            	MapPoint node=new MapPoint();
            	node.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            	node.setName(cursor.getString(cursor.getColumnIndex("name")));
            	node.setStatus(cursor.getString(cursor.getColumnIndex("status")));
            	node.setSlug(cursor.getString(cursor.getColumnIndex("slug")));
            	node.setLatitude(cursor.getInt(cursor.getColumnIndex("lat")));
            	node.setLongitude(cursor.getInt(cursor.getColumnIndex("lng")));
            	node.setJslug(cursor.getString(cursor.getColumnIndex("jslug")));
                valori.add(node);
                cursor.moveToNext();
            }
            
        }while(!cursor.isAfterLast());

        cursor.close();
        db.close();
        
    
        return valori;
       
    }
   
    private ArrayList<MapPoint> getNodeByPosition(Integer lat, Integer lng) throws DBOpenException{
        
    	ArrayList<MapPoint> valori=new ArrayList<MapPoint>();
        Cursor cursor=null;
        SQLiteDatabase db=null;
        
        db=openDBR();
           
         //select * from Nodes where lat=lat AND lng=lng                //"SELECT * FROM Nodes WHERE (? = ?) AND (? = ?)";
         String sql="(? = ?) AND (? = ?)";
         String values[]=new String[]{"lat",lat.toString(),"lng",lng.toString()};
         cursor= db.query(DbHelper.TABLE_NAME1, null, sql, values, null, null, null); //ottengo un cursore che punta alle entry ottenute dalla query
         cursor.moveToFirst();
         do{
                MapPoint node=new MapPoint();

            node.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            node.setName(cursor.getString(cursor.getColumnIndex("name")));
            node.setStatus(cursor.getString(cursor.getColumnIndex("status")));
            node.setSlug(cursor.getString(cursor.getColumnIndex("slug")));
            node.setLatitude(cursor.getInt(cursor.getColumnIndex("lat")));
            node.setLongitude(cursor.getInt(cursor.getColumnIndex("lng")));
            node.setJslug(cursor.getString(cursor.getColumnIndex("jslug")));
            
            valori.add(node);

                cursor.moveToNext();
         }while(!cursor.isAfterLast());
           
        
        return valori;
       
    }
    
    private ArrayList<MapPoint> getNodeByName(String name) throws DBOpenException{
        
        ArrayList<MapPoint> valori=new ArrayList<MapPoint>();
        Cursor cursor;
        SQLiteDatabase db=null;
        db=openDBR();
           
        //select * from Nodes where (lat between [min_lat] AND [max_lat]) and (lng between [min_lng] AND [max_lng]));
        String sql="SELECT * FROM "+DbHelper.TABLE_NAME1+" WHERE name like '%"+name+"%'";
        cursor= db.rawQuery(sql,null); //ottengo un cursore che punta alle entry ottenute dalla query
        cursor.moveToFirst();
        do{
        	if(cursor!=null && cursor.getCount()>0){
            	MapPoint node=new MapPoint();
            	node.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            	node.setName(cursor.getString(cursor.getColumnIndex("name")));
            	node.setStatus(cursor.getString(cursor.getColumnIndex("status")));
            	node.setSlug(cursor.getString(cursor.getColumnIndex("slug")));
            	node.setLatitude(cursor.getInt(cursor.getColumnIndex("lat")));
            	node.setLongitude(cursor.getInt(cursor.getColumnIndex("lng")));
            	node.setJslug(cursor.getString(cursor.getColumnIndex("jslug")));
                valori.add(node);
                cursor.moveToNext();
            }
            
        }while(!cursor.isAfterLast());

        cursor.close();
        db.close();
        
    
        return valori;
       
    }
    
    private ArrayList<MapPoint> getNodesById(Integer id) throws DBOpenException{
    	
    	
   	 ArrayList<MapPoint> valori=new ArrayList<MapPoint>();
        Cursor cursor;
        SQLiteDatabase db=openDBR();
        String sql="SELECT * FROM "+DbHelper.TABLE_NAME1+" WHERE _id='"+id+"'";
        Log.v("Sql1", sql);
        //select * from Nodes
        
        cursor= db.rawQuery(sql,null); //ottengo un cursore che punta alle entry ottenute dalla query
        cursor.moveToFirst();
        do{
            MapPoint node=new MapPoint();
            node.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            node.setName(cursor.getString(cursor.getColumnIndex("name")));
            node.setStatus(cursor.getString(cursor.getColumnIndex("status")));
            node.setSlug(cursor.getString(cursor.getColumnIndex("slug")));
            node.setLatitude(cursor.getInt(cursor.getColumnIndex("lat")));
            node.setLongitude(cursor.getInt(cursor.getColumnIndex("lng")));
            node.setJslug(cursor.getString(cursor.getColumnIndex("jslug")));
            
            valori.add(node);
            cursor.moveToNext();
        }while(!cursor.isAfterLast());

            cursor.close();
            db.close();
        
    
        return valori;
   	
   	
   	
   }
    
    public boolean isEmptyTableNodes() throws DBOpenException{
    	return isEmpty(DbHelper.TABLE_NAME1);
    }
    
    public boolean isEmptyTableUpdates() throws DBOpenException{
    	return isEmpty(DbHelper.TABLE_NAME2);
    }
    
    public int countRows(String tableName) throws DBOpenException{
		
    	SQLiteDatabase db;
		
		db=openDBR();
		Cursor iterator=db.rawQuery("SELECT COUNT(*) FROM "+tableName,null);    //scandisco il contenuto della tabella passata in input
		iterator.moveToFirst();
		int count= iterator.getInt(0);
		iterator.close();
		db.close();
		
		return count;
    }
    
    public boolean isEmpty(String tableName) throws DBOpenException{

    		int tot=countRows(tableName);
    		
    		Log.v("Totale delle righe in nodes = ", ""+tot);
    		
    		if(tot !=0){
    			return false;
    		}
    		else{
    			return true;
    		}
    	
    }
    public void insertUpdate() throws DBOpenException{
    	SQLiteDatabase db=null;
    	
    		db=openDBW();
    		String datetime=CalendarManager.getCurrentDateTime();
    		String forDatabase=CalendarManager.formatDateTimeForDatabase(datetime);
    
    		ContentValues values=new ContentValues();
    		values.put("number_nodes", map.size());
    		values.put("type", "automatic");
    		values.put("datetimeC",forDatabase);
           	db.insertOrThrow(DbHelper.TABLE_NAME2, null, values);    //inserisco il messaggio nel DB
           	db.close();
    	
    }
    
    public void deleteNodes() throws DBOpenException{
    	
    	truncateTable(DbHelper.TABLE_NAME1);
    	
        
    }
    
    
    
    private void insertNode(MapPoint mp) throws DBOpenException {
       
    	SQLiteDatabase db=null;
    	
    	db=openDBW();
    	ContentValues values=new ContentValues();
    	values.put("_id", mp.getId());
    	values.put("name", mp.getName());
    	values.put("status", mp.getStatus());
    	values.put("slug", mp.getSlug());
    	values.put("lat", mp.getLatitude());
    	values.put("lng", mp.getLongitude());
    	values.put("jslug", mp.getJslug());
    	db.insertOrThrow(DbHelper.TABLE_NAME1, null, values);    //inserisco il messaggio nel DB
    	db.close();   
        
    	
    }
    
    public String getLastUpdate() throws DBOpenException{
        
        Cursor cursor=null;
        SQLiteDatabase db=null;
        
        if(isEmptyTableUpdates()){
        	return "0000/00/00 00:00:00";
        }
        
        try{
        	db=openDBR();
           
            //select datetimeC from Updates order by datetimeC DESC limit 1
        	//ottengo un cursore che punta alle entry ottenute dalla query
            cursor= db.rawQuery("SELECT datetimeC FROM "+DbHelper.TABLE_NAME2+" ORDER BY _id DESC LIMIT 1",null); 
            cursor.moveToFirst();
           	return cursor.getString(cursor.getColumnIndex("datetimeC"));
           
        }catch(SQLException e){
        	Log.e("DB_ERROR - SELECT update","Impossibile trovare ultimo aggiornamento - "+e.getMessage());
        }
        finally{
            cursor.close();
            db.close();
        }

        return "";
    }
       
    
  
}
