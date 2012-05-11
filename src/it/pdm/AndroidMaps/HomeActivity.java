package it.pdm.AndroidMaps;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {
	

		static final String STRING_CONNECTION = "http://map.ninux.org/nodes.json";
		static final double MAX_DAYS_FOR_UPDATE=7.0;
        
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.home);

            Resources res = getResources(); // Resource object to get Drawables
            TabHost tabHost = getTabHost();  // The activity TabHost
            TabHost.TabSpec spec;  // Resusable TabSpec for each tab
            Intent intent;  // Reusable Intent for each tab

            // Create an Intent to launch an Activity for the tab (to be reused)
            intent = new Intent().setClass(this, AndroidMapsActivity.class);

            // Initialize a TabSpec for each tab and add it to the TabHost
            spec = tabHost.newTabSpec("map").setIndicator("Google Map",
                              res.getDrawable(R.drawable.icon_map)).setContent(intent);
            tabHost.addTab(spec);

            // Do the same for the other tabs
            intent = new Intent().setClass(this, AugmentedRealityActivity.class);
            spec = tabHost.newTabSpec("ar").setIndicator("AR",
                              res.getDrawable(R.drawable.ic_tab_artists_white))
                          .setContent(intent);
            tabHost.addTab(spec);

            intent = new Intent().setClass(this, ListNodesActivity.class);
            spec = tabHost.newTabSpec("list").setIndicator("Lista Nodi",
                              res.getDrawable(R.drawable.icon_list))
                          .setContent(intent);
            tabHost.addTab(spec);

            tabHost.setCurrentTab(2);
        }
	
}
