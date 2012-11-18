package com.marcioapf.mocos;

public class MateriaData {
	
	
	public MateriaData(){
		this(4, 0, "Nova", true);
	}
	
	public MateriaData(int aulasSemanais, int atrasos, String strNome, boolean checkNeeded) {
		super();
		this.aulasSemanais = aulasSemanais;
		this.atrasos = atrasos;
		this.strNome = strNome;
		this.checkNeeded = checkNeeded;
	}
	int aulasSemanais, atrasos;
	String strNome;
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
}