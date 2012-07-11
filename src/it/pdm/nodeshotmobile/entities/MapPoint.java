package it.pdm.nodeshotmobile.entities;

import java.util.ArrayList;

public class MapPoint {
    private Integer id;
    private String name;
    private String status;
    private String slug;
	private Integer lat; 
	private Integer lng; 
	private String jslug;
    
	static public ArrayList<MapPoint> points; //lista dei punti presi tramite Jsonparser
	
    public MapPoint() {
    	id= 0;
    	status=""; 
    	name= ""; 
    	slug= ""; 
    	lat= 0; 
    	lng= 0; 
    	jslug= "";
		
	}
    
    public Integer getId() {
            return id;
    }

    public void setId(Integer id) {
            this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLatitude() {
            return lat;
    }

    public void setLatitude(Integer lat) {
            this.lat = lat;
    }

    public Integer getLongitude() {
            return lng;
    }

    public void setLongitude(Integer lon) {
            this.lng = lon;
    }

    public String getStatus() {
            return status;
    }

    public void setStatus(String stat) {
            this.status = stat;
    }

    public String getSlug() {
            return slug;
    }

    public void setSlug(String slug) {
    		this.slug=slug;
    }
    
    public String getJslug() {
        return jslug;
    }

    public void setJslug(String jslug) {
    	this.jslug=jslug;
    }

    @Override
    public String toString() {
            return "MapPoint [id=" + id +"\n, name=" + name + "\n, latitude=" + lat + "\n, longitude=" + lng +"\n, " +
            		"status="+ status + "\n, slug=" + slug+ "\n, jslug=" + jslug+"]";
    }
}
