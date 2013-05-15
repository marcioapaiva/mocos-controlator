package com.marcioapf.mocos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import com.marcioapf.mocos.data.MateriaMemos;
import com.marcioapf.mocos.data.SQLHelper;

public class NotasActivity extends Activity {

    private long mMateriaID = 10;
    private EditText mEt1bim;
    private EditText mEt2bim;
    private EditText mEtExame;
    private TextView mTvNomeMateria;
    private MateriaMemos mMemos;
    private SQLHelper mSqlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mMateriaID = bundle.getLong("MateriaID", -1);

        mSqlHelper = new SQLHelper(this);

        mTvNomeMateria = (TextView)findViewById(R.id.tvNomeMateria);
        mEt1bim = (EditText)findViewById(R.id.et1bim);
        mEt2bim = (EditText)findViewById(R.id.et2bim);
        mEtExame = (EditText)findViewById(R.id.etExame);

        mMemos = mSqlHelper.retrieveMateriaMemosByID(mMateriaID);

        String strNomeMateria = mSqlHelper.retrieveMateriaDataById(mMateriaID).getStrNome();
        mTvNomeMateria.setText(strNomeMateria + " - Notas");
        mEt1bim.setText(mMemos.getS1bim());
        mEt2bim.setText(mMemos.getS2bim());
        mEtExame.setText(mMemos.getSExame());
    }

    @Override
    protected void onPause(){
        mMemos.setAll(mEt1bim.getText().toString(), mEt2bim.getText().toString(),
            mEtExame.getText().toString());
        mSqlHelper.updateMemos(mMemos);

        super.onPause();
    }
}
