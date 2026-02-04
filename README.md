# Software ECG Covid-19 Edition 6.2.3

The Software-ECG is also called Software-EKG. 
EKG stands for Elektrokardiogramm in german which is Electrocardiography or ECG in english.

![Software ECG COVID-19 Edition Screenshot](web/images/covid19-new-cases-positive-rate.png)

What is the Software-ECG?

> Software-EKG is a powerful tool for time series analysis developed by Johannes Weigend.
> Utilizing a highly efficient search index and optimized algorithms, 
> the tool enables you to both visualize and analyze time series containing billions of values.

Source: http://covid19.weigend.de

## Prerequisites

* **Java 17+** (JDK, not just JRE)
* **Maven 3.6+**

## Build

Clone the repository and build with Maven:

```bash
git clone https://github.com/weigend/Software-ECG-COVID-19-Edition.git
cd Software-ECG-COVID-19-Edition
mvn install -f install.xml
mvn clean install
```

## Start

After building, you can start the application directly from the build output.

### Linux

```bash
cd ekg-awb-application/awb-application-linux/target/
unzip -q Software-EKG_AWB_6.2.3-COVID-EDITION-SNAPSHOT_linux64.zip
./start-software-ekg.sh
```

### macOS

```bash
cd ekg-awb-application/awb-application-osx/target/
unzip -q Software-EKG_AWB_6.2.3-COVID-EDITION-SNAPSHOT_mac64.zip
Software-EKG.app/Contents/MacOS/start-software-ekg
```

Or double-click `Software-EKG.app` in Finder after unzipping.

**Note:** On first launch, macOS may block the app. Right-click and select "Open" to bypass Gatekeeper.

### Windows

```cmd
cd ekg-awb-application\awb-application-win\target
:: Unzip Software-EKG_AWB_6.2.3-COVID-EDITION-SNAPSHOT_win64.zip
start-software-ekg.bat
```

Or execute `Software-EKG.exe` after unzipping.

## Development Mode

For development, you can run directly without building the full distribution:

```bash
./run-dev.sh
```

## FAQ

Q: Why is there an `install.xml`? <br>
A: The `install.xml` contains maven directives to install dependencies from `libs/` folder into the local maven repository.
If these directives are in the root `pom.xml`, then each `mvn install` call to the nested `pom.xml` files trigger the installation.

Q: Is there a 32 bit version? <br>
A: There is only a 64 bit version of the workbench. We use the 64 bit JVM Runtime.

Q: Do I need to build for each platform separately? <br>
A: Yes, the build bundles your local JDK. Build on the target platform for best results, or ensure Java 17+ is installed on the target system.

## License

This software is licensed under the **GNU General Public License v3.0 (GPLv3)**.

The Software-ECG COVID-19 Edition is an extract of the larger Software-EKG product.
The intellectual property rights originally held by QAware GmbH have been transferred 
to **Weigend AM GmbH & Co KG**, the current owner and copyright holder of the software.

This edition, focused on COVID-19 data analysis, is released as open source under GPLv3 
(consistent with version 6.1.1).

See [LICENSE.md](LICENSE.md) for the full license text.

## Copyright

Copyright © 2009 - 2025 Weigend AM GmbH & Co KG. All rights reserved.
