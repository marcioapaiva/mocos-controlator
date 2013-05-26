package com.marcioapf.mocos.data;

public class SubjectData {

    private int mWeeklyClasses;
    private int mDelays;
    private long mSqlID;
    private String mName;
    private String mProfessorName;
    private boolean mCheckNeeded;

	public static int calculateMaxDelays(int weeklyClasses){
		return (2*(int)Math.ceil((float)0.15f*16*weeklyClasses));
	}
	
	public SubjectData(){
		this(4, 0, "Nova", "", false);
	}
	
	public SubjectData(int weeklyClasses, int delays, String name, String professorName, boolean checkNeeded) {
		mWeeklyClasses = weeklyClasses;
		mDelays = delays;
		mName = name;
        mProfessorName = professorName;
		mCheckNeeded = checkNeeded;
		mSqlID = -1;
	}

	public long getSqlID() {
		return mSqlID;
	}

	public void setSqlID(long sqlID) {
		mSqlID = sqlID;
	}
	
	public int getDelays() {
		return mDelays;
	}

	public void setDelays(int delays) {
		mDelays = delays;
	}

	public boolean isCheckNeeded() {
		return mCheckNeeded;
	}

	public void setCheckNeeded(boolean checkNeeded) {
		mCheckNeeded = checkNeeded;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

    public String getProfessorName() {
        return mProfessorName;
    }

    public void setProfessorName(String professorName) {
        mProfessorName = professorName;
    }

	public int getWeeklyClasses() {
		return mWeeklyClasses;
	}

	public void setWeeklyClasses(int weeklyClasses) {
		mWeeklyClasses = weeklyClasses;
	}
}
