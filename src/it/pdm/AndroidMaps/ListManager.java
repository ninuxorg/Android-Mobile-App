package it.pdm.AndroidMaps;


import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ListManager {
    
    private String[] from={"id","name"}; //dai valori contenuti in queste chiavi
    private int[] to={R.id.id,R.id.name};//agli id delle view
	
    private SimpleAdapter notesAdapter;
    private ArrayList<HashMap<String, Object>> items;

    private ListView listaView;
    private Context generic;

    private Integer idItem;
    private Integer idRow;
    
    public ListManager(Context generic, ListView l) {
    	this.generic=generic;
    	items=new ArrayList<HashMap<String, Object>>();
    	listaView=l;
    	refresh();
    	idItem=0;
    	idRow=0;
    }
    
    public void setIdItem(Integer id){
    	idItem=id;
    }
    
    public void setIdRow(Integer id){
    	idRow=id;
    }
    
    public Integer getIdItem(){
    	return idItem;
    }
    
    public Integer getIdRow(){
    	return idRow;
    }
    
    public ArrayList<HashMap<String, Object>> getMap(){
    	return items;
    }
    
    public void append(Integer id,String name){
    	HashMap<String, Object> map=new HashMap<String, Object>();
    	map.put("id", id);
    	map.put("name", name);
    	items.add(0,map);
    	refresh();
    }
    
    
    
    public void load(ArrayList<MapPoint> list){
    	items.clear();
    	
    	for(MapPoint point : list){
    		String name=point.getName();
    		Integer id=point.getId();
    		
			HashMap<String, Object> map=new HashMap<String, Object>();
	    	map.put("id", id);
	    	map.put("name", name);
	    	items.add(map);
    	}
    	
    	refresh();
    }
    
    
    public void refresh(){
    	
    	notesAdapter=new SimpleAdapter(generic,items,R.layout.row,from,to);
    	listaView.setAdapter(notesAdapter);
    	listaView.setItemsCanFocus(false);

    	
    }
    
    public void delete(Integer id){
    	
    	items.remove(id.intValue());
    	
    	refresh();
    	
    }
	
}
