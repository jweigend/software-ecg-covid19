# Software-EKG Distribution for macOS

This module builds a macOS application bundle `Software-EKG.app` for the COVID-19 Edition.

## Build

Build from the project root:

```bash
mvn install -f install.xml
mvn clean install
```

The distribution will be created at:
`target/Software-EKG_AWB_6.2.3-COVID-EDITION-SNAPSHOT_mac64.zip`

## Start

```bash
cd target/
unzip -q Software-EKG_AWB_6.2.3-COVID-EDITION-SNAPSHOT_mac64.zip
Software-EKG.app/Contents/MacOS/start-software-ekg
```

Or double-click `Software-EKG.app` in Finder.

**Note:** On first launch, macOS Gatekeeper may block the unsigned app. 
Right-click the app and select "Open" to bypass this.

## Special Configuration for macOS

The storage paths differ from the default behavior:
- Solr core: `$HOME/Library/Application Support/Software-EKG/embeddedSolr/`
- Log files: `$HOME/Library/Logs/Software-EKG`
 