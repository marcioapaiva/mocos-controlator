package com.marcioapf.mocos.data;

public class SubjectMemo {
	private String m1stBimSum;
    private String m2ndBimSum;
    private String mExam;
	private long mSqlID;

	public SubjectMemo(String s1bim, String s2bim, String sExame, long sqlID){
		m1stBimSum = s1bim;
		m2ndBimSum = s2bim;
		mExam = sExame;
		mSqlID = sqlID;
	}
	
	public void setAll(String s1bim, String s2bim, String sExame){
		m1stBimSum = s1bim;
		m2ndBimSum = s2bim;
		mExam = sExame;
	}
	
	public String get1stBimSum() {
		return m1stBimSum;
	}

	public void set1stBimSum(String a1stBimSum) {
		this.m1stBimSum = a1stBimSum;
	}

	public String get2ndBimSum() {
		return m2ndBimSum;
	}

	public void set2ndBimSum(String a2ndBimSum) {
		this.m2ndBimSum = a2ndBimSum;
	}

	public String getExam() {
		return mExam;
	}

	public void setExam(String exam) {
		this.mExam = exam;
	}

    public long getSqlID() {
        return mSqlID;
    }

    public void setSqlID(long sqlID) {
        sqlID = sqlID;
    }
}
