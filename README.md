# AEM Content Importer - Build

Maven Builds:

* Complete (content+bundle+saxon)
  Using profile `distribution` from either root or `aem-content-importer-content`.
  Assumes AEM 6.0 running on `localhost:4502`.

  For example:

  ```
  $ cd ~/my-code/aem-content-importer
  $ mvn -P distribution clean install
  ```

* Standard (only the selected project)
  With no profile specified maven installs only the selected project.
  Pay attention to aem-content-importer-bundle, the `/apps/${project.folder}/install` crx folder must exist
  
  IMPORTANT: First install has to be done using profile `distribution` 


# AEM Content Importer -  Upload Page
Go to the upload page: `http://<host>:<port>/content/resources/help/en/upload-content.html`

You have two options:

* Fill out form parameters:
    * *Transformer*: Select from list (currently only one, `DITATransformerXSLT`). The value saved it'll be the whole package(`com.adobe.aem.importer.impl.DITATransformerXSLTImpl`).
	* *Source folder*: Specify the source path in the repository. The folder must be already created and populated with
	  all the source files (including the `ditamap` file), for example, through dragging and dropping the files into
	  the repository using a WebDAV client. A typical location for the source directory is `/var/aem-importer/import`.
	* *Destination folder*: Specify the destination folder in the repository. Whether destination directory doesn't exist it will be created. i.e `content/dita-import`. 
	* *Master File*: The name of the master file. For example, `mcloud.ditamap`.

* Upload a zip file containing all source files (contents+master+config). Note that the configuration file, which contains at least the same properties available in the form, must be the first file in the `zip` package (alphabetical order).
  The upload can be performed either by dragging and dropping the `zip` file or by clicking the *Upload* button.
  Notice that config file values override the corresponding form parameters if they're filled them out.

  An example `config_params.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
 <entry key="dita-transformer">com.adobe.aem.importer.impl.DITATransformerXSLTImpl</entry>
 <entry key="src">/var/aem-importer/import2</entry>
 <entry key="target">/content/pando</entry>
 <entry key="masterFile">mcloud.ditamap</entry>
 .....
 .....
</properties>
```


# DITATransformerXSLT

The DITATransformerXSLT transformer is currently the only one available and permits to import contents using an XSLT schema.
Here shown below the available parameters to set either in the form (custom properties input) or in zip (config_params.xml):
* xslt-transformer: then class name of xslt transformer to use (i.e. net.sf.saxon.TransformerFactoryImpl). It's assume to be exported by an osgi bundle (it's available after first installation with `distribution` profile)
* xslt-file: the xslt file to use
* packageTpl: the path of package template to use by Jcr Archive Importer
* tempFolder: `optional, default '/var/aem-importer/tmp'` the temporary folder to use during processing
* graphicFolders: `optional, default 'images,graphics,Graphics'` the folder list separated by comma where to look up graphic resource to copy

An example of custom properties input in the form:
    xslt-transformer=net.sf.saxon.TransformerFactoryImpl
    xslt-file=/apps/aem-importer/resources/dita-to-content.xsl
    tempFolder=/var/aem-importer/tmp
    packageTpl=/apps/aem-importer/resources/package-tpl
    graphicFolders=images,graphics,Graphics
    
An example of `config_params.xml` containing DITATransformerXSLT properties also:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
 <entry key="dita-transformer">com.adobe.aem.importer.impl.DITATransformerXSLTImpl</entry>
 <entry key="src">/var/aem-importer/import2</entry>
 <entry key="target">/content/pando</entry>
 <entry key="masterFile">mcloud.ditamap</entry>
 <entry key="xslt-transformer">net.sf.saxon.TransformerFactoryImpl</entry>
 <entry key="xslt-file">/apps/aem-importer/resources/dita-to-content.xsl</entry>
 <entry key="tempFolder">/var/aem-importer/tmp</entry>
 <entry key="packageTpl">/apps/aem-importer/resources/package-tpl</entry>
 <entry key="graphicFolders">images,graphics,Graphics</entry>
</properties>
```


