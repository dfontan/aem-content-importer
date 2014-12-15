# AEM Content Importer - Build

Maven Builds:

* Complete (content+bundle+saxon)
  Using profile `distribution` from either root or aem-content-importer-content.
  Assumes AEM 6.0 running on `localhost:4502`.

  For example:

  ```
  $ cd ~/my-code/aem-content-importer
  $ mvn -P distribution clean install
  ```

* Standard (only the selected project)
  With no profile specified maven installs only the selected project.
  Pay attention to aem-content-importer-bundle, the `/apps/${project.folder}/install` crx folder must exist


# AEM Content Importer -  Upload Page
Go to the upload page: `http://<host>:<port>/content/resources/help/en/upload-content.html`

You have two options:

* Fill out form parameters:
    * *Transformer*: Select from list (currently only one, `DITATransformerXSLT`).
	* *Source folder*: Specify the source path in the repository. The folder must be already created and populated with
	  all the source files (including the `ditamap` file), for example, through dragging and dropping the files into
	  the repository using a WebDAV client. A typical location for the source directory is `/var/aem-importer/import`.
	* *Destination folder*: Specify the destination folder in the repository. The destination directory must already exist
	  in the repository. *Currently this path must be set to `content/pando`.*
	* *Master File*: The name of the master file. For example, `mcloud.ditamap`.

* Upload a zip file containing all source files (including the master `ditamap` file) and a
  `config_params.xml` file. Note that the configuration file must be the first file in the `zip` package.
  The upload can be performed either by dragging and dropping the `zip` file or by clicking the *Upload* button.

  An example `config_params.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<config>
    <transformer>com.adobe.aem.importer.impl.DITATransformerXSLTImpl</transformer>
    <src>/var/aem-importer/import</src>
    <target>/content/pando</target>
    <masterFile>mcloud.ditamap</masterFile>
</config>
```



