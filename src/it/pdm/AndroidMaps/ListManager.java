package it.pdm.AndroidMaps;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ListManager {
    
    private String[] from={"id","name"}; //dai valori contenuti in queste chiavi
    private int[] to={R.id.id,R.id.name};//agli id delle view
	
    private SimpleAdapter nodesAdapter;
    private ArrayList<HashMap<String, Object>> items;
    private ArrayList<HashMap<String, Object>> items_result;

    private ListView listaView;
    private Context generic;
    
    public ListManager(Context generic, ListView l) {
    	this.generic=generic;
    	listaView=l;
    	items=new ArrayList<HashMap<String, Object>>();
    	items_result=new ArrayList<HashMap<String, Object>>();
    	refresh();
    	
    }
    
    public ArrayList<HashMap<String, Object>> getMap(){
    		return items_result;
    	
    }
    
    public void append(ArrayList<HashMap<String, Object>> arraymap,Integer id,String name){
    	HashMap<String, Object> map=new HashMap<String, Object>();
    	map.put("id", id);
    	map.put("name", name);
    	arraymap.add(0,map);
    }
    
    public void append(Integer id,String name){
    	append(items,id,name);
    }
    
    
    
    public void load(ArrayList<HashMap<String, Object>> arraymap,ArrayList<MapPoint> list){
    	arraymap.clear();
    	
    	for(MapPoint point : list){
    		String name=point.getName();
    		Integer id=point.getId();
    		
    		append(arraymap,id,name);
    	}
    }
    
    public void load(ArrayList<MapPoint> list){
    	load(items,list);
    	load(items_result,list);
    	refresh();
    }
    
    
    public void refresh(ArrayList<HashMap<String, Object>> items){
    	
    	nodesAdapter=new SimpleAdapter(generic,items,R.layout.row,from,to);
    	listaView.setAdapter(nodesAdapter);
    	listaView.setItemsCanFocus(false);

    	
    }
    
    public void refresh(){
    	refresh(this.items);
    }
    
    public void filter(String s){
    	
    	Iterator<HashMap<String, Object>> handle=items.iterator();
    	items_result.clear();
    	do{
    		
    		HashMap<String, Object> item=handle.next();
    		String name=(String)item.get("name");
    		
    		boolean match=Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE).matcher(name).find();
    		
    		if(match){
    			append(items_result, (Integer)item.get("id"), (String)item.get("name"));
    		}
    		
    	}while(handle.hasNext());
    	
    	
    	refresh(items_result);
    	
    }
    
    public void delete(Integer id){
    	
    	items.remove(id.intValue());
    	
    	refresh();
    	
    }
	
}


/*class mySimpleAdapter extends SimpleAdapter{
	
	public mySimpleAdapter(Context generic,ArrayList<HashMap<String, Object>> items,
			int id_layout,String[] from, int[] to) {
		super(generic,items,id_layout,from,to);
	}
	
	   @Override
	    public Filter getFilter() {
	        return new Filter() {
	            @SuppressWarnings("unchecked")
	            @Override
	            protected void publishResults(CharSequence constraint, FilterResults results) {
	                Log.d(Constants.TAG, "**** PUBLISHING RESULTS for: " + constraint);
	                myData = (List<MyDataType>) results.values;
	                MyCustomAdapter.this.notifyDataSetChanged();
	            }

	            @Override
	            protected FilterResults performFiltering(CharSequence constraint) {
	                Log.d(Constants.TAG, "**** PERFORM FILTERING for: " + constraint);
	                List<MyDataType> filteredResults = getFilteredResults(constraint);

	                FilterResults results = new FilterResults();
	                results.values = filteredResults;

	                return results;
	            }
	        };
	    }

	
	
	
}*/
