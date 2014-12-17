::Example 1: Post using curl command only with form parameters
curl -u admin:admin -F src=/var/aem-importer/import -F transformer=com.adobe.aem.importer.impl.DITATransformerXSLTImpl -F master=mcloud.ditamap -F target=/content/pando http://localhost:4502/content/resources/help/en/upload-content/_jcr_content
::Example 2: Post using curl command only with a zip file
::curl -u admin:admin -F fileselect=@C:\example.zip http://localhost:4502/content/resources/help/en/upload-content/_jcr_content