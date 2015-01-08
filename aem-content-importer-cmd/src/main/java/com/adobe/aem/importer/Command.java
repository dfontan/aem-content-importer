/*******************************************************************************
* Copyright (c) 2014 Adobe Systems Incorporated. All rights reserved.
*
* Licensed under the Apache License 2.0.
* http://www.apache.org/licenses/LICENSE-2.0
******************************************************************************/

package com.adobe.aem.importer;

public class Command {
	
	 public static void main (String args[]) {
		 
		 if (args.length == 0) {
			 System.out.println("Usage:");
			 System.out.println("-src <value> => Source folder in JCR where are located the all necessary files to generate content");
			 System.out.println("-target <value> => Target folder in JCR where it's going to be allocated generated content");
			 System.out.println("-transformer <value> => Sort of transformer to apply using files inside src folder");
			 System.out.println("-masterFile <value> => The root file to start transformation if it's needed");
			 System.out.println("-customProps <value> => Custom properties to add to the configuration process");
			 System.out.println("-file <value> => A zip file with all source files necessaries for making the transformation");
			 System.out.println("-folder <value> => A local folder that contains all source files necessaries for making the transformation");
		 }
		 
		 int i = 0;
		 String arg = "";
		 while (i < args.length && args[i].startsWith("-")) {
	          arg = args[i++];
		 }
		 
	            
	 }

}
