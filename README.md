AEM Content Importer - Build
==============
branch 'master' of https://github.com/Adobe-Marketing-Cloud/aem-content-importer.git

Maven Builds:

- Complete (content+bundle+saxon)
Using profile "distribution" from either root or aem-content-importer-content

- Standard (only the selected project)
With no profiles it installs only the selected project in a separate way.
Pay attention to aem-content-importer-bundle, the /apps/${project.folder}/install crx folder must exist


AEM Content Importer -  Upload Page
===================================
- Upload page: http://HOST:PORT/content/resources/help/en/upload-content.html
- You have two options: 
	* Upload a zip file with all information in it. Source files,config file (transformer, src, target, master file name) clicking the button or dragging and dropping. Config file 
	  has to be the first file in the zip and has priority over form params.
	* Fill out form params
  It's important to mention that src and target must exist in repository
- Format of config file in zip:
<?xml version="1.0" encoding="utf-8"?>
<config>
<transformer>package path of transformer to be executed</transformer>
<src>Repository path to upload source files inside zip</src>
<target>Repository path to import transformation result</target>
<masterFile>master file if transformation type needs it</masterFile>
</config>

