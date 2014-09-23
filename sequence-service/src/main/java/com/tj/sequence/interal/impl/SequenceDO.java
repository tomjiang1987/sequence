package com.tj.sequence.interal.impl;

public class SequenceDO {
	private String name;
	private Long value;
	private Long min;
	private Long max;
	private Integer size;
	private Boolean cycle;
	private Integer step;
	private Long start;
	
	public boolean checkValue(){
		if(value < min) return false;
		if(start < min) return false;
		if(value > max) return false;
		if(size < 1) return false;
		if(step < 1) return false;
		
		return true;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getValue() {
		return value;
	}
	public void setValue(Long value) {
		this.value = value;
	}
	public Long getMin() {
		return min;
	}
	public void setMin(Long min) {
		this.min = min;
	}
	public Long getMax() {
		return max;
	}
	public void setMax(Long max) {
		this.max = max;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Boolean getCycle() {
		return cycle;
	}
	public void setCycle(Boolean cycle) {
		this.cycle = cycle;
	}
	public Integer getStep() {
		return step;
	}
	public void setStep(Integer step) {
		this.step = step;
	}
	public Long getStart() {
		return start;
	}
	public void setStart(Long start) {
		this.start = start;
	}

}
