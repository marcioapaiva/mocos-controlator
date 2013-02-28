package com.marcioapf.mocos;

public class MateriaMemos {
	String s1bim, s2bim, sExame;
	long sqlID;

	public long getSqlID() {
		return sqlID;
	}

	public void setSqlID(long sqlID) {
		this.sqlID = sqlID;
	}

	public MateriaMemos (String s1bim, String s2bim, String sExame, long sqlID){
		this.s1bim = s1bim;
		this.s2bim = s2bim;
		this.sExame = sExame;
		this.sqlID = sqlID;
	}
	
	public void setAll(String s1bim, String s2bim, String sExame){
		this.s1bim = s1bim;
		this.s2bim = s2bim;
		this.sExame = sExame;
	}
	
	public String getS1bim() {
		return s1bim;
	}

	public void setS1bim(String s1bim) {
		this.s1bim = s1bim;
	}

	public String getS2bim() {
		return s2bim;
	}

	public void setS2bim(String s2bim) {
		this.s2bim = s2bim;
	}

	public String getsExame() {
		return sExame;
	}

	public void setsExame(String sExame) {
		this.sExame = sExame;
	}
	
	
}
