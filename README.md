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

1) Upload page: http://<HOST>:<PORT>/content/resources/help/en/upload-content.html
2) You have two options: upload a zip file with all information, source files,config file (transformer, src, target) and master file) clicking the button or dragging and dropping. If
you don't want to create a zip file, you are able to fill out form params indicating transformer, repository source files (it must exist), repository target(it must exist)
and master file name if it's necessary.

