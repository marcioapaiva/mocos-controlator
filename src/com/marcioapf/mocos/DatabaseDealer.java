package com.marcioapf.mocos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class DatabaseDealer {
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	
	public static final String PREFS_NAME = "MyPrefsFile";
	
	
	public void save(ArrayList<MateriaData> materiasData){
        //SharedPreferences table = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences table = WelcomeActivity.sharedPrefTable;
        SharedPreferences.Editor editor = table.edit();
        
        editor.putInt("numMaterias", materiasData.size());
        for (int i=0; i<materiasData.size(); i++){
        	editor.putString("nomemateria"+i, materiasData.get(i).getStrNome());
        	editor.putInt("maxatrasosmateria"+i, materiasData.get(i).getAulasSemanais());
        	editor.putInt("atrasosmateria"+i, materiasData.get(i).getAtrasos());
        	editor.putBoolean("checkneededmateria"+i, materiasData.get(i).isCheckNeeded());
        }
        editor.commit();
	}
	
	public void addMemos(MateriaMemos mMemos, int num){
		SharedPreferences table = WelcomeActivity.sharedPrefTable;
        SharedPreferences.Editor editor = table.edit();
        
		editor.putString("nota1bmateria"+num, mMemos.getS1bim());
		editor.putString("nota2bmateria"+num, mMemos.getS2bim());
		editor.putString("notaExamemateria"+num, mMemos.getsExame());
		
		editor.commit();
	}
	
	public MateriaMemos getMemos(int num){
		SharedPreferences table = WelcomeActivity.sharedPrefTable;
		
		String s1bim = table.getString("nota1bmateria"+num, "");
		String s2bim = table.getString("nota2bmateria"+num, "");
		String sExame = table.getString("notaExamemateria"+num, "");
		
		return new MateriaMemos(s1bim, s2bim, sExame);		
	}
	
	public ArrayList<MateriaData> restoreData() {
		ArrayList<MateriaData> materiasData = new ArrayList<MateriaData>();
		
		SharedPreferences table = WelcomeActivity.sharedPrefTable;
        int numMaterias = table.getInt("numMaterias", 0);
        
        
        String nome;
        int aulasSemanais;
        int atrasos;
        boolean checkNeeded;
        
        for (int i=0; i< numMaterias; i++){
        	nome = table.getString("nomemateria"+i, "erro");
        	aulasSemanais = table.getInt("maxatrasosmateria"+i, 90);
        	atrasos = table.getInt("atrasosmateria"+i, 0);
        	checkNeeded = table.getBoolean("checkneededmateria"+i, false);
        	
        	materiasData.add(new MateriaData(aulasSemanais, atrasos, nome, checkNeeded));
        }
        
        //Testando o funcionamento:
        System.out.println("Imprimindo materiasData");
        for (MateriaData m : materiasData){
        	System.out.println(m.getStrNome() + " " + m.getAulasSemanais());
        }
        
        
        return materiasData;
	}
	
	//Apenas para testes
	void createExternalStoragePrivateFile() {
	    File file = new File(Environment.getExternalStorageDirectory(), "/Android/data/classesSchedule.txt");

	    try {	    	
	        OutputStream os = new FileOutputStream(file);
	        os.write("This is my first file!".getBytes());
	        os.close();
	    } catch (IOException e) {
	        Log.w("ExternalStorage", "Error writing " + file, e);
	    }
	}
	
	public boolean getWritable(){
		return mExternalStorageWriteable;
	}
	
	public static String getDefaultImportExportPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/classesSchedule.txt";
	}
	
	public void dataExport(ArrayList<MateriaData> materiasData, String filePath) throws FileNotFoundException, IOException{
		File file = new File(filePath);
		Log.v("DatabaseDealer.export()", file.getAbsolutePath());
		FileWriter fw = new FileWriter(file);
		
		for (MateriaData mData : materiasData){
			fw.write(mData.getStrNome() + "\n");
			fw.write(mData.getAulasSemanais() + " " + mData.getAtrasos() + " " + mData.isCheckNeeded() + "\n");
		}
		
		fw.close();
	}
	
	public void dataImport(String filePath) throws FileNotFoundException, IOException{
		File file = new File(filePath);
		Log.v("DatabaseDealer.import()", file.getAbsolutePath());
		Scanner input = new Scanner(file);
		
        //SharedPreferences table = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences table = WelcomeActivity.sharedPrefTable;
        SharedPreferences.Editor editor = table.edit();
        
		String s; int i=0;
		while(input.hasNext()){
			s = input.next();
			editor.putString("nomemateria"+i, s);
			s = input.next();
			editor.putInt("maxatrasosmateria"+i, Integer.parseInt(s));
			s = input.next();
			editor.putInt("atrasosmateria"+i, Integer.parseInt(s));
			s = input.next();
			editor.putBoolean("checkneededmateria"+i, Boolean.valueOf(false));
			i++;
		}
		editor.putInt("numMaterias", i);
		editor.commit();
		
		input.close();
	}
	
	public void dataImport() throws FileNotFoundException, IOException{
		this.dataImport(getDefaultImportExportPath());
	}
	
	public void dataExport(ArrayList<MateriaData> materiasData) throws FileNotFoundException, IOException{
		dataExport(materiasData, getDefaultImportExportPath());
	}
}
