# Software ECG Covid-19 Edition 6.2.3

The Software-ECG is also called Software-EKG. 
EKG stands for Elektrokardiogramm in german which is Electrocardiography or ECG in english.

![Software ECG COVID-19 Edition Screenshot](web/images/covid19-new-cases-positive-rate.png)

What is the Software-ECG?

> Software-EKG is a powerful tool for time series analysis developed by Johannes Weigend and others at QAware GmbH
> Utilizing a highly efficient search index and optimized algorithms, 
> the tool enables you to both visualize and analyze time series containing billions of values.

Source: http://covid19.weigend.de

Technology used for building:
* Java 17
* Maven 3.6

## Build

```
mvn install -f install.xml
mvn clean install
```

## Start

### Linux

```
# if you build the project yourself, then change directory to the zipped files
cd Software-ECG-COVID-19-Edition/ekg-awb-application/awb-application-linux/target/
unzip -q -d Software-EKG.app Software-EKG_AWB_6.2.3-COVID-EDITION-SNAPSHOT_linux64.zip
Software-EKG.app/start-software-ekg.sh
```

### MacOS

```
# if you build the project yourself, then change directory to the zipped files
cd Software-ECG-COVID-19-Edition/ekg-awb-application/awb-application-osx/target/
unzip -q Software-EKG_AWB_6.2.3-COVID-EDITION-SNAPSHOT_mac64.zip
Software-EKG.app/Contents/MacOS/start-software-ekg
```

or double click `Software-EKG.app` after the unzipping command. 

### Windows

Execute `awb-application-win/target/Software-EKG.exe`, if you build the project.
Or unzip `Software-EKG_AWB_6.2.3-COVID-EDITION-SNAPSHOT_mac64.zip` into a folder and execute `Software-EKG.exe`. 

## FAQ

Q: Why is there an `install.xml`? <br>
A: The `install.xml` contains maven directives to install dependencies from `libs/` folder into the local maven repository.
If these directives are in the root `pom.xml`, then each `mvn install` call to the nested `pom.xml` files trigger the installation.

Q: Is there a 32 bit version? <br>
A: There is only a 64 bit version of the workbench. We use the  64 bit JVM Runtime.
