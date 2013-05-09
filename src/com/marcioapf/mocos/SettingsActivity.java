package com.marcioapf.mocos;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.settings_activity);
        
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
}
