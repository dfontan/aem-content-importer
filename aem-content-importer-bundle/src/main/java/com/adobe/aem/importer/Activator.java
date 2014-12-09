/**
 * CodeBay Innovation SL 2014
 * aem-content-importer-bundle
 * com.adobe.aem.importer / Activator.java 
 * Dec 9, 2014
 * @author Gaetano
 */
package com.adobe.aem.importer;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		DITATransformerHelper.init(context.getBundle());
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
