package com.discutiamo.solaretermico.beans;

public class StatoServizi
{
	private int esito_ar4;

	private String dataora;

	private boolean pompa_solare1_casa;
	private boolean pompa_solare2_casa;
	private String attManualePompeSolare_casa;
	private String minTempPannelli_casa;
	private String maxTempAcquaSolare_casa;
	private String minTempStacco_casa;
	private String temp_1_casa;
	private String temp_2_casa;
	private String temp_3_casa;
	private String temp_4_casa;
	private String temp_5_casa;

	public StatoServizi()
	{
		esito_ar4=0;
		dataora="";

		pompa_solare1_casa=false;
		pompa_solare2_casa=false;
		maxTempAcquaSolare_casa="";
		temp_1_casa="";
		temp_2_casa="";
		temp_3_casa="";
		temp_4_casa="";
		temp_5_casa="";
		attManualePompeSolare_casa="0";
		minTempPannelli_casa="";
		minTempStacco_casa="";
	}

	public int getEsito_ar4()
	{
		return esito_ar4;
	}

	public void setEsito_ar4(int esito_ar4)
	{
		this.esito_ar4 = esito_ar4;
	}

	public String getDataora() {
		return dataora;
	}

	public void setDataora(String dataora) {
		this.dataora = dataora;
	}

	public boolean isPompa_solare1_casa() {
		return pompa_solare1_casa;
	}

	public void setPompa_solare1_casa(boolean pompa_solare1_casa) {
		this.pompa_solare1_casa = pompa_solare1_casa;
	}

	public boolean isPompa_solare2_casa() {
		return pompa_solare2_casa;
	}

	public void setPompa_solare2_casa(boolean pompa_solare2_casa) {
		this.pompa_solare2_casa = pompa_solare2_casa;
	}

	public String getAttManualePompeSolare_casa() {
		return attManualePompeSolare_casa;
	}

	public void setAttManualePompeSolare_casa(String attManualePompeSolare_casa) {
		this.attManualePompeSolare_casa = attManualePompeSolare_casa;
	}

	public String getMinTempPannelli_casa() {
		return minTempPannelli_casa;
	}

	public void setMinTempPannelli_casa(String minTempPannelli_casa) {
		this.minTempPannelli_casa = minTempPannelli_casa;
	}

	public String getMaxTempAcquaSolare_casa() {
		return maxTempAcquaSolare_casa;
	}

	public void setMaxTempAcquaSolare_casa(String maxTempAcquaSolare_casa) {
		this.maxTempAcquaSolare_casa = maxTempAcquaSolare_casa;
	}

	public String getMinTempStacco_casa() {
		return minTempStacco_casa;
	}

	public void setMinTempStacco_casa(String minTempStacco_casa) {
		this.minTempStacco_casa = minTempStacco_casa;
	}

	public String getTemp_1_casa() {
		return temp_1_casa;
	}

	public void setTemp_1_casa(String temp_1_casa) {
		this.temp_1_casa = temp_1_casa;
	}

	public String getTemp_2_casa() {
		return temp_2_casa;
	}

	public void setTemp_2_casa(String temp_2_casa) {
		this.temp_2_casa = temp_2_casa;
	}

	public String getTemp_3_casa() {
		return temp_3_casa;
	}

	public void setTemp_3_casa(String temp_3_casa) {
		this.temp_3_casa = temp_3_casa;
	}

	public String getTemp_4_casa() {
		return temp_4_casa;
	}

	public void setTemp_4_casa(String temp_4_casa) {
		this.temp_4_casa = temp_4_casa;
	}

	public String getTemp_5_casa() {
		return temp_5_casa;
	}

	public void setTemp_5_casa(String temp_5_casa) {
		this.temp_5_casa = temp_5_casa;
	}

}
