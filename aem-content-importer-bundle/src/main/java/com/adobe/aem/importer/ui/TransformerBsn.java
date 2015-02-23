/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/
package com.adobe.aem.importer.ui;

import java.util.Set;

import com.adobe.aem.importer.XMLTransformerHelper;
import com.adobe.cq.sightly.WCMUse;

public class TransformerBsn extends WCMUse{
	
	private Transformer[] list = {};

	@Override
	public void activate() throws Exception {
		Set<Class<?>> items = XMLTransformerHelper.getAvailableTransformers();
		
		list = new Transformer[items.size()];
		int i = 0;
		for (Class<?> item : items) {
			Transformer transformer = new Transformer();
			transformer.setName(item.getName());
			transformer.setSimpleName(item.getSimpleName().replace("Impl", ""));
			list[i++] = transformer;
		}
		
	}

	
	public Transformer[] getList() {
		return list;
	}
	
	public class Transformer {
		String name;
		String simpleName;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getSimpleName() {
			return simpleName;
		}
		public void setSimpleName(String simpleName) {
			this.simpleName = simpleName;
		}
	}

}
