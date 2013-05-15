package com.marcioapf.mocos.data;

public class MateriaData {

    private int mAulasSemanais;
    private int mAtrasos;
    private long mSqlID;
    private String mStrNome;
    private boolean mCheckNeeded;

	public static int calculateMaxAtrasos(int aulasSemanais){
		return (2*(int)Math.ceil((float)0.15f*16*aulasSemanais));
	}
	
	public MateriaData(){
		this(4, 0, "Nova", true);
	}
	
	public MateriaData(int aulasSemanais, int atrasos, String strNome, boolean checkNeeded) {
		mAulasSemanais = aulasSemanais;
		mAtrasos = atrasos;
		mStrNome = strNome;
		mCheckNeeded = checkNeeded;
		mSqlID = -1;
	}

	public long getSqlID() {
		return mSqlID;
	}

	public void setSqlID(long sqlID) {
		mSqlID = sqlID;
	}
	
	public int getAtrasos() {
		return mAtrasos;
	}

	public void setAtrasos(int atrasos) {
		mAtrasos = atrasos;
	}

	public boolean isCheckNeeded() {
		return mCheckNeeded;
	}

	public void setCheckNeeded(boolean checkNeeded) {
		mCheckNeeded = checkNeeded;
	}

	public String getStrNome() {
		return mStrNome;
	}

	public void setStrNome(String strNome) {
		mStrNome = strNome;
	}

	public int getAulasSemanais() {
		return mAulasSemanais;
	}

	public void setAulasSemanais(int aulasSemanais) {
		mAulasSemanais = aulasSemanais;
	}
}
