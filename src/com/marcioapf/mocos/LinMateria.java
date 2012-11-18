package com.marcioapf.mocos;

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
	
	LinMatBtm linMatBtm;
	LinMatTop linMatTop;
	Context context;
	CheckBox cbChecked;
	
	Handler handlerTimer;
	TimerHelper timerHelper;
	ProgressBar pBarFaltas;
	Button btnAddAtraso, btnRemAtraso, btnAddFalta, btnRemFalta, useless1, useless2;
	TextView tvFaltas;
	
	
	MateriaData data;
	
	public boolean isCheckNeeded() {
		return data.isCheckNeeded();
	}
	
	public void setCheckNeeded(boolean checkNeeded) {
		this.getData().setCheckNeeded(checkNeeded);
	}

	public void setAtrasos(int atrasos) {
		data.setAtrasos(atrasos);
		update();
	}
	
	public int getAtrasos(){
		return data.getAtrasos();
	}

	public String getStrNome(){
		return data.getStrNome();
	}
	
	public void setStrNome(String strNome){
		data.setStrNome(strNome);
		linMatTop.tvMateria.setText(strNome);
	}

	public LinMateria(Context context, MateriaData materiaData){
		this(context, materiaData.getStrNome(), materiaData.getAulasSemanais(), materiaData.isCheckNeeded());
		this.setAtrasos(materiaData.getAtrasos());
		update();
	}
	public LinMateria(Context context, String strMateria, int aulasSemanais, boolean checkNeeded) {
		super(context);
		this.context = context;
		this.setOrientation(VERTICAL);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		data = new MateriaData();
		
		linMatBtm = new LinMatBtm(context);
		linMatTop = new LinMatTop(context);
		
		this.setStrNome(strMateria);
		data.setAulasSemanais(aulasSemanais);
		data.setAtrasos(0);
		data.setCheckNeeded(checkNeeded);
		
		this.addView(linMatTop, 0);
		this.addView(linMatBtm, 1);
		
		tvFaltas.setText((float)data.getAtrasos()/2 + "/" + ((int)Math.ceil((float)0.15f*16*aulasSemanais)));	
		
		timerHelper = new TimerHelper();
		update();
	}
	
	public void update() {
		pBarFaltas.setProgress(data.getAtrasos());
		tvFaltas.setText((float)data.getAtrasos()/2 + "/" + ((int)Math.ceil((float)0.15f*16*data.getAulasSemanais())));
		linMatTop.tvMateria.setText(data.getStrNome());
		pBarFaltas.setMax(2*(int)Math.ceil((float)0.15f*16*data.getAulasSemanais()));
		
		if(2*(int)Math.ceil((float)0.15f*16*data.getAulasSemanais()) - data.getAtrasos() <= 4){
			tvFaltas.setTextColor(Color.RED);
			linMatTop.tvMateria.setTextColor(Color.RED);
		} else {
			tvFaltas.setTextColor(Color.LTGRAY);
			linMatTop.tvMateria.setTextColor(Color.LTGRAY);
		}
		System.out.println("updated");
		
		if(data.isCheckNeeded()){
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
			tvMateria.setText(data.getStrNome());
			tvMateria.setTextSize(22);
			tvMateria.setMinimumWidth((int) (100 * getContext().getResources().getDisplayMetrics().density + 0.5f));
			tvMateria.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			

			int dps = 60;
			final float scale = getContext().getResources().getDisplayMetrics().density;
			int pixels = (int) (dps * scale + 0.5f);
			
			
			//this.setWeightSum(1.0f);
			this.setOrientation(HORIZONTAL);
			this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, pixels));
			
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
						
						data.setCheckNeeded(false);
						System.out.println("is checked");
					}
					else {
						System.out.println("not checked");
						handlerTimer.removeCallbacks(timerHelper);
						data.setCheckNeeded(true);
					}
				}
			});
			this.addView(cbChecked, 5);
			
			
			
			((Activity)context).registerForContextMenu(tvMateria);
			
			
			class ModifyFaltas implements OnClickListener {
				public void onClick(View v){
					if (v == btnAddAtraso){
						setAtrasos(data.getAtrasos()+1);
					}
					else if (v == btnRemAtraso){
						if(data.getAtrasos()>=1)
							setAtrasos(data.getAtrasos()-1);
					}
					else if (v == btnAddFalta){
						setAtrasos(data.getAtrasos()+2);
					}
					else if (v == btnRemFalta){
						if(data.getAtrasos()>=2)
							setAtrasos(data.getAtrasos()-2);
					}
					
					update();
					((WelcomeActivity)context).updateTotal();
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
			this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			pBarFaltas = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
			pBarFaltas.setMax(2*(int)Math.ceil((float)0.15f*16*data.getAulasSemanais()));
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

	public MateriaData getData() {
		return data;
	}

	public int getAulasSemanais() {
		return data.getAulasSemanais();
	}

	public void setAulasSemanais(int aulasSemanais) {
		data.setAulasSemanais(aulasSemanais);
	}
}
