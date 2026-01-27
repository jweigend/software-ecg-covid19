# Software-EKG Distribution for MacOSX
This module is a specialized distribution for MacOSX. It builds a OSX application ```Software-EKG.app``` with all features including time project and logfile analysis. If the application is built under OSX you get a DMG-File with a signed application in it. On Windows or Linux systems you get a ZIP-File with an unsigned application in it. In this case for the first start of the Software-EKG the controll-button must be pressed while open the application.

At the moment there is no maven build for this distribution.
## Special configuration for OSX
The storage pathes for the embedded solr core and the logfiles differs from the default behavour.
The solr core will be placed under ```$HOME/Library/Application Support/QAware/Software-EKG/embeddedSolr/```.
The logfiles are placed under ```$HOME/Library/Logs/Software-EKG```.
 