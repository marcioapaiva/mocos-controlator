package com.marcioapf.mocos.data;

public class MateriaMemos {
	private String mS1bim;
    private String mS2bim;
    private String mSExame;
	private long mSqlID;

	public MateriaMemos (String s1bim, String s2bim, String sExame, long sqlID){
		mS1bim = s1bim;
		mS2bim = s2bim;
		mSExame = sExame;
		mSqlID = sqlID;
	}
	
	public void setAll(String s1bim, String s2bim, String sExame){
		mS1bim = s1bim;
		mS2bim = s2bim;
		mSExame = sExame;
	}
	
	public String getS1bim() {
		return mS1bim;
	}

	public void setS1bim(String s1bim) {
		this.mS1bim = s1bim;
	}

	public String getS2bim() {
		return mS2bim;
	}

	public void setS2bim(String s2bim) {
		this.mS2bim = s2bim;
	}

	public String getSExame() {
		return mSExame;
	}

	public void setSExame(String SExame) {
		this.mSExame = SExame;
	}

    public long getSqlID() {
        return mSqlID;
    }

    public void setSqlID(long sqlID) {
        sqlID = sqlID;
    }
}
