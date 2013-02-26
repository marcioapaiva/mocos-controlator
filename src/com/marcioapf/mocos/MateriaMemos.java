package com.marcioapf.mocos;

public class MateriaMemos {
	String s1bim, s2bim, sExame;

	public MateriaMemos (String s1bim, String s2bim, String sExame){
		this.s1bim = s1bim;
		this.s2bim = s2bim;
		this.sExame = sExame;
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
