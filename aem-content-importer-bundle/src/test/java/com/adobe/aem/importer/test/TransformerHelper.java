package com.adobe.aem.importer.test;

import com.adobe.aem.importer.XMLTransformer;
import com.adobe.aem.importer.XMLTransformerHelper;

public class TransformerHelper extends XMLTransformerHelper {
	
	public void addTransformer(final XMLTransformer xmlTransformer) {
		bindAvailableTransformers(xmlTransformer);
	}
	
	public static XMLTransformer getTransformer(String className) throws Exception {
		return XMLTransformerHelper.getXMLTransformer(className);
	}
	
}
