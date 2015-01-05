package com.adobe.aem.importer.test.transformer;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.importer.XMLTranformer;
import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.aem.importer.process.TransformerWorkflowProcess;



public class TestRetrieveTransformer {
	
	private static final Logger log = LoggerFactory.getLogger(TestRetrieveTransformer.class);
	
	private final String DITATRANSFORMER_CLASS = "com.adobe.aem.importer.impl.DITATransformerXSLTImpl";
	
	
	
//	@BeforeClass
	public static void init() {
		
	}
	
//	@Test
	public void retrieveDITATransformerXLST() {
		
		try {
			XMLTranformer ditaTransformer = XMLTransformerHelper.getXMLTransformer(DITATRANSFORMER_CLASS);
			
			boolean exist = false;
			if (ditaTransformer != null) {
				exist = true;
			}
			assertTrue(exist);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			assertTrue(false);
		}
		
	}
	
	
	
	

}
