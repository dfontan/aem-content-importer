package com.adobe.aem.importer.xml;

public class Config {
	
	private String transformer;
	private String src;
	private String target;
	private String masterFile;
	public String getTransformer() {
		return transformer;
	}
	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getMasterFile() {
		return masterFile;
	}
	public void setMasterFile(String masterFile) {
		this.masterFile = masterFile;
	}

}
