package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;

public class CaptureCount implements Serializable {
	/**
	 * 本次查询的入参
	 */
	private String startTime;
	private String endTime;
	private String ipcId;

	/**
	 * 匹配到的查询结果
	 */
	private Long totalresultcount;

	private String lastcapturetime;

	public void setTotalresultcount(Long totalresultcount) {
		this.totalresultcount = totalresultcount;
	}

	public Long getTotalresultcount() {
		return totalresultcount;
	}

	public void setLastcapturetime(String lastcapturetime) {
		this.lastcapturetime = lastcapturetime;
	}

	public String getLastcapturetime() {
		return lastcapturetime;
	}



}
