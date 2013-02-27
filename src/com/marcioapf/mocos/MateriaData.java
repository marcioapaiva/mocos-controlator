package com.marcioapf.mocos;

public class MateriaData {
	
	
	 public static int calculateMaxAtrasos(int aulasSemanais){
		return (2*(int)Math.ceil((float)0.15f*16*aulasSemanais));
	}
	
	public MateriaData(){
		this(4, 0, "Nova", true);
	}
	
	public MateriaData(int aulasSemanais, int atrasos, String strNome, boolean checkNeeded) {
		super();
		this.aulasSemanais = aulasSemanais;
		this.atrasos = atrasos;
		this.strNome = strNome;
		this.checkNeeded = checkNeeded;
		sqlID = -1;
	}
	public long getSqlID() {
		return sqlID;
	}

	public void setSqlID(long sqlID) {
		this.sqlID = sqlID;
	}
	int aulasSemanais, atrasos;
	long sqlID;
	String strNome;
	String notas1b, notas2b, notasExame;
	boolean checkNeeded;
	
	public int getAtrasos() {
		return atrasos;
	}
	public void setAtrasos(int atrasos) {
		this.atrasos = atrasos;
	}
	public boolean isCheckNeeded() {
		return checkNeeded;
	}
	public void setCheckNeeded(boolean checkNeeded) {
		this.checkNeeded = checkNeeded;
	}
	public String getStrNome() {
		return strNome;
	}
	public void setStrNome(String strNome) {
		this.strNome = strNome;
	}
	public int getAulasSemanais() {
		return aulasSemanais;
	}
	public void setAulasSemanais(int aulasSemanais) {
		this.aulasSemanais = aulasSemanais;
	}

	public String getNotas1b() {
		return notas1b;
	}

	public void setNotas1b(String notas1b) {
		this.notas1b = notas1b;
	}

	public String getNotas2b() {
		return notas2b;
	}

	public void setNotas2b(String notas2b) {
		this.notas2b = notas2b;
	}

	public String getNotasExame() {
		return notasExame;
	}

	public void setNotasExame(String notasExame) {
		this.notasExame = notasExame;
	}
}