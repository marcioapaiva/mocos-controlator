package com.welcome.first;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LinMateria extends LinearLayout {
	String strMateria;
	LinMatBtm linMatBtm;
	LinMatTop linMatTop;
	Context context;
	CheckBox cbChecked;
	boolean checkNeeded;
	Handler handlerTimer;
	TimerHelper timerHelper;
	
	public boolean isCheckNeeded() {
		return checkNeeded;
	}

	public void setCheckNeeded(boolean checkNeeded) {
		this.checkNeeded = checkNeeded;
	}

	public int getAtrasos() {
		return atrasos;
	}

	public void setAtrasos(int atrasos) {
		this.atrasos = atrasos;
		update();
	}

	int aulasSemanais, atrasos;
	Button btnAddAtraso, btnRemAtraso, btnAddFalta, btnRemFalta, useless1, useless2;
	TextView tvFaltas;
	ProgressBar pBarFaltas;
	
	public LinMateria(Context context, String materia, int aulasSemanais, boolean checkNeeded) {
		super(context);
		this.context = context;
		this.setOrientation(VERTICAL);
		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		
		this.strMateria = materia;
		this.aulasSemanais = aulasSemanais;
		this.atrasos = 0;
		this.checkNeeded = checkNeeded;
		
		linMatBtm = new LinMatBtm(context);
		linMatTop = new LinMatTop(context);
		
		this.addView(linMatTop, 0);
		this.addView(linMatBtm, 1);
		
		tvFaltas.setText((float)atrasos/2 + "/" + ((int)Math.ceil((float)0.15f*16*aulasSemanais)));	
		
		timerHelper = new TimerHelper();
		update();
	}
	
	public void update() {
		pBarFaltas.setProgress(atrasos);
		tvFaltas.setText((float)atrasos/2 + "/" + ((int)Math.ceil((float)0.15f*16*aulasSemanais)));
		linMatTop.tvMateria.setText(strMateria);
		pBarFaltas.setMax(2*(int)Math.ceil((float)0.15f*16*aulasSemanais));
		
		if(2*(int)Math.ceil((float)0.15f*16*aulasSemanais) - atrasos <= 4){
			tvFaltas.setTextColor(Color.RED);
			linMatTop.tvMateria.setTextColor(Color.RED);
		} else {
			tvFaltas.setTextColor(Color.LTGRAY);
			linMatTop.tvMateria.setTextColor(Color.LTGRAY);
		}
		System.out.println("updated");
		
		if(checkNeeded){
			cbChecked.setVisibility(VISIBLE);
			cbChecked.setChecked(false);
		}
		else {
			cbChecked.setVisibility(INVISIBLE);
			cbChecked.setChecked(true);
		}
	}
	
	class LinMatTop extends LinearLayout {
		TextView tvMateria;
		
		public LinMatTop(final Context context) {
			super(context);
			
			tvMateria = new TextView(context);
			tvMateria.setText(strMateria);
			tvMateria.setTextSize(22);
			tvMateria.setMinimumWidth((int) (100 * getContext().getResources().getDisplayMetrics().density + 0.5f));
			tvMateria.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			

			int dps = 60;
			final float scale = getContext().getResources().getDisplayMetrics().density;
			int pixels = (int) (dps * scale + 0.5f);
			
			
			//this.setWeightSum(1.0f);
			this.setOrientation(HORIZONTAL);
			this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, pixels));
			
			this.addView(tvMateria, 0);
			
			
			btnAddAtraso = new Button(context);
			btnRemAtraso = new Button(context);
			btnAddFalta  = new Button(context);
			btnRemFalta = new Button(context);
			
			dps = 40;
			pixels = (int) (dps * scale + 0.5f);
			
			btnAddAtraso.setText("+A");
			btnAddAtraso.setLayoutParams(new LayoutParams(pixels, LayoutParams.MATCH_PARENT));
			btnRemAtraso.setText("-A");
			btnRemAtraso.setLayoutParams(new LayoutParams(pixels, LayoutParams.MATCH_PARENT));
			btnAddFalta.setText("+F");
			btnAddFalta.setLayoutParams(new LayoutParams(pixels, LayoutParams.MATCH_PARENT));
			btnRemFalta.setText("-F");
			btnRemFalta.setLayoutParams(new LayoutParams(pixels, LayoutParams.MATCH_PARENT));
			
			this.addView(btnRemFalta, 1);
			this.addView(btnRemAtraso, 2);
			this.addView(btnAddAtraso, 3);
			this.addView(btnAddFalta, 4);
			
			cbChecked = new CheckBox(context);
			cbChecked.setTextSize(22);
			cbChecked.setText("(!)");
			cbChecked.setTextColor(Color.YELLOW);
			

			
			cbChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
					if (isChecked){
						handlerTimer = new Handler();
						handlerTimer.postDelayed(timerHelper, 3000);
						
						checkNeeded = false;
						System.out.println("is checked");
					}
					else {
						System.out.println("not checked");
						handlerTimer.removeCallbacks(timerHelper);
						checkNeeded = true;
					}
				}
			});
			this.addView(cbChecked, 5);
			
			
			
			((Activity)context).registerForContextMenu(tvMateria);
			
			
			class ModifyFaltas implements OnClickListener {
				public void onClick(View v){
					if (v == btnAddAtraso){
						atrasos++;
					}
					else if (v == btnRemAtraso){
						if(atrasos>=1)
							atrasos--;
					}
					else if (v == btnAddFalta){
						atrasos+=2;;
					}
					else if (v == btnRemFalta){
						if(atrasos>=2)
							atrasos-=2;;
					}
					
					update();
					((WelcomeActivity)context).update();
				}
			}
			btnAddAtraso.setOnClickListener(new ModifyFaltas());
			btnRemAtraso.setOnClickListener(new ModifyFaltas());
			btnRemFalta.setOnClickListener(new ModifyFaltas());
			btnAddFalta.setOnClickListener(new ModifyFaltas());
			
		}
	}
	
	class LinMatBtm extends LinearLayout {
		
		public LinMatBtm(Context context) {
			super(context);
			this.setOrientation(HORIZONTAL);
			this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			
			pBarFaltas = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
			pBarFaltas.setMax(2*(int)Math.ceil((float)0.15f*16*aulasSemanais));
			pBarFaltas.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.8f*10.0f/20.0f));
			
			tvFaltas = new TextView(context);
			tvFaltas.setMinimumWidth((int) (70 * getContext().getResources().getDisplayMetrics().density + 0.5f));
			tvFaltas.setGravity(Gravity.CENTER);
			tvFaltas.setTextSize(15);
			
			this.addView(pBarFaltas, 0);
			this.addView(tvFaltas, 1);
		}	
	}
	
	class TimerHelper implements Runnable {
		public void run() {
			System.out.println("Timer Finished");
			update();
		}
	}
}
