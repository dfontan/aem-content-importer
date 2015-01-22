package com.adobe.aem.importer.ui;

import java.util.ArrayList;
import java.util.List;
import com.adobe.aem.importer.impl.XMLTransformerDITAImpl;
import com.adobe.cq.sightly.WCMUse;

public class TransformerBsn extends WCMUse{
	
	private Class<?>[] list = {};

	@Override
	public void activate() throws Exception {
		List<Class<?>> items = new ArrayList<Class<?>>();
		items.add(XMLTransformerDITAImpl.class);
		list = items.toArray(new Class<?>[items.size()]);
	}

	
	public Class<?>[] getList() {
		return list;
	}

}
