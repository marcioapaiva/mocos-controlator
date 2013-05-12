package com.marcioapf.mocos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint("ViewConstructor")
public class LinMateria extends LinearLayout {

    private final CheckBox cbChecked;
    private final TextView tvMateria;
    private final ProgressBar pBarFaltas;
    private final Button btnAddAtraso, btnRemAtraso, btnAddFalta, btnRemFalta;
    private final TextView tvFaltas;

    private MateriaData data;
    private final Handler handlerTimer = new Handler(Looper.myLooper());
    private final Runnable timerHelper = new Runnable() {
        @Override
        public void run() {
            System.out.println("Timer Finished");
            update();
        }
    };

    public LinMateria(Context context, MateriaData materiaData){
        this(context, materiaData.getStrNome(), materiaData.getAulasSemanais(), materiaData.isCheckNeeded());
        this.setAtrasos(materiaData.getAtrasos());
        this.getData().setSqlID(materiaData.getSqlID());
        update();
    }
    public LinMateria(Context context, String strMateria, int aulasSemanais, boolean checkNeeded) {
        super(context);
        inflate(context, R.layout.linha_materia, this);
        tvMateria = getView(R.id.tv_materia);
        tvFaltas = getView(R.id.tv_faltas);
        pBarFaltas = getView(R.id.pbar_faltas);
        cbChecked = getView(R.id.cb_checked);
        btnRemFalta = getView(R.id.rem_falta);
        btnRemAtraso = getView(R.id.rem_atraso);
        btnAddAtraso = getView(R.id.add_atraso);
        btnAddFalta = getView(R.id.add_falta);

        data = new MateriaData();

        this.setStrNome(strMateria);
        data.setAulasSemanais(aulasSemanais);
        data.setAtrasos(0);
        data.setCheckNeeded(checkNeeded);

        ((Activity)context).registerForContextMenu(this);
        configureViews();
        update();
    }

    private void configureViews() {
        tvFaltas.setText((float)data.getAtrasos()/2 + "/" + ((int)Math.ceil((float)0.15f*16*data.getAulasSemanais())));

        cbChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
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

        OnClickListener buttonListener = new OnClickListener() {
            public void onClick(View v){
                switch (v.getId()) {
                    case R.id.rem_falta:
                        if(data.getAtrasos()>=2)
                            setAtrasos(data.getAtrasos()-2);
                        break;
                    case R.id.rem_atraso:
                        if(data.getAtrasos()>=1)
                            setAtrasos(data.getAtrasos()-1);
                        break;
                    case R.id.add_atraso:
                        setAtrasos(data.getAtrasos()+1);
                        break;
                    case R.id.add_falta:
                        setAtrasos(data.getAtrasos()+2);
                        break;
                }
                update();
                ((WelcomeActivity)getContext()).updateTotal();
            }
        };
        btnAddAtraso.setOnClickListener(buttonListener);
        btnRemAtraso.setOnClickListener(buttonListener);
        btnRemFalta.setOnClickListener(buttonListener);
        btnAddFalta.setOnClickListener(buttonListener);
    }

    public void update() {
        pBarFaltas.setProgress(data.getAtrasos());
        tvFaltas.setText((float) data.getAtrasos() / 2 + "/" + ((int) Math.ceil(0.15f * 16 * data.getAulasSemanais())));
        tvMateria.setText(data.getStrNome());
        pBarFaltas.setMax(2 * (int) Math.ceil(0.15f * 16 * data.getAulasSemanais()));

        if(2*(int)Math.ceil(0.15f*16*data.getAulasSemanais()) - data.getAtrasos() <= 4){
            tvFaltas.setTextColor(Color.RED);
            tvMateria.setTextColor(Color.RED);
        } else {
            tvFaltas.setTextColor(Color.DKGRAY);
            tvMateria.setTextColor(Color.DKGRAY);
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
        tvMateria.setText(strNome);
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

    public <Type extends View> Type getView(int id) {
        return (Type)findViewById(id);
    }
}
