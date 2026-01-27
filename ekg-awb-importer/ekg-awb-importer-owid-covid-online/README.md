# ekg-awb-importer-owid-covid-online

Import module for Software-EKG 6.2.3 COVID-19 Edition that will read and import COVID-19 case data from Our-World-In-Data (OWID) project.
The online variant of the plugin reads the data directly from remote services and do not nead further manual interaction.

## What does it?
This plugin is not a normal Software-EKG plugin. It automatizes the download and importing of the data.
To automate this, it deletes and creates the project, where the data is stored.
This automation is applied on the start of the Software-EKG. 
Also a progress window is displayed while the import is excecuted. 

Furthermore, it manipulates the Software-EKG, to hide some features of the Software-EKG.
These changes should improve the usability for the Software-EKG COVID-19 Edition, because it simplifies the UI.

## How to use it
The master Branch requires a special version of Software-EKG. 
We forked the Software EKG, to change some behaviour for the COVID-19 Edition.  
The forked edition can be found here:
- https://gitlab.qaware.de/c.muenker/ekg-awb-covid-19-edition
- https://gitlab.qaware.de/c.muenker/ekg-awb-sdk-covid-19-edition

For the first build, please follow the following instructions:
- `cd ekg-awb-sdk-covid-19-edition; mvn clean install -DskipTests`
- `cd ekg-awb-covid-19-edition; mvn clean install -DskipTests` (will break, because of missing plugin)
- `cd ekg-awb-importer-owid-covid-online; mvn clean install` 
- `cd ekg-awb-covid-19-edition; mvn clean install -DskipTests`
- `cd ekg-awb-covid-19-edition/ekg-awb-application` Choose your system and look for the zip file in the target directory.
- Extract the zip file and you can start the Software-EKG COVID-19 Edition

## How to Release

The release plugin is not working, because we don't have permission to upload to release-artifactory.
But to create a Git-Tag and increase the Maven-Version, you can use the following steps:
```
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${RELEASE_VERSION}
mvn clean install
git commit -am "Set Release Version"
git tag ${RELEASE_VERSION}

mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${SNAPSHOT_VERSION}-SNAPSHOT
mvn clean install
git commit -am "Set Developer Version"
```
Please apply this to all three branches (SDK, AWB-EKG, COVID-19-Plugin). Don't forget to change the version in `EKGVersionInfo` of AWB-EKG project.

## Using plugin in normal Software-EKG
It is possible to use the normal Software-EKG Version with this plugin. 
Please compile the version in branch `software-ekg-plugin`, and copy the jar into the libs directory of the Software-EKG.
