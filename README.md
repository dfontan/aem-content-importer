AEM Content Importer
==============
branch 'master' of https://github.com/Adobe-Marketing-Cloud/aem-content-importer.git

Maven Builds:

- Complete (content+bundle+saxon)
Using profile "distribution" from either root or aem-content-importer-content

- Standard (only the selected project)
With no profiles it installs only the selected project in a separate way.
Pay attention to aem-content-importer-bundle, the /apps/${project.folder}/install crx folder must exist
