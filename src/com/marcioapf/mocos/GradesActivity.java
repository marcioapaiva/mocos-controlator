package com.marcioapf.mocos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import com.marcioapf.mocos.data.SubjectMemo;
import com.marcioapf.mocos.data.SQLHelper;

public class GradesActivity extends Activity {

    private long mSubjectID = 10;
    private EditText m1stBimEditText;
    private EditText m2ndBimEditText;
    private EditText mExamEditText;
    private TextView mSubjectNameTextView;
    private SubjectMemo mMemos;
    private SQLHelper mSqlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mSubjectID = bundle.getLong("MateriaID", -1);

        mSqlHelper = new SQLHelper(this);

        mSubjectNameTextView = (TextView)findViewById(R.id.tvNomeMateria);
        m1stBimEditText = (EditText)findViewById(R.id.et1bim);
        m2ndBimEditText = (EditText)findViewById(R.id.et2bim);
        mExamEditText = (EditText)findViewById(R.id.etExame);

        mMemos = mSqlHelper.retrieveMateriaMemosByID(mSubjectID);

        String strNomeMateria = mSqlHelper.retrieveMateriaDataById(mSubjectID).getName();
        mSubjectNameTextView.setText(strNomeMateria + " - Notas");
        m1stBimEditText.setText(mMemos.get1stBimSum());
        m2ndBimEditText.setText(mMemos.get2ndBimSum());
        mExamEditText.setText(mMemos.getExam());
    }

    @Override
    protected void onPause(){
        mMemos.setAll(m1stBimEditText.getText().toString(), m2ndBimEditText.getText().toString(),
            mExamEditText.getText().toString());
        mSqlHelper.updateMemos(mMemos);

        super.onPause();
    }
}
