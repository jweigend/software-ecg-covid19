# ekg-awb-importer-owid-covid-online

Import module for Software-EKG 6.2.3 COVID-19 Edition that reads and imports COVID-19 case data from Our-World-In-Data (OWID) project.
The online variant of the plugin reads the data directly from GitHub and does not need further manual interaction.

## What does it do?

This plugin automates the download and import of COVID-19 data:

- On startup, it automatically downloads the latest data from OWID GitHub repository
- Creates/recreates the project where the data is stored
- Shows a progress window during the import process
- Falls back to bundled data if the download fails (e.g., offline mode)

Additionally, it simplifies the Software-EKG UI by hiding features not relevant for COVID-19 data analysis.

## Data Sources

- **Primary**: https://raw.githubusercontent.com/owid/covid-19-data/master/public/data/owid-covid-data.csv
- **Fallback**: Bundled `covid-fallback-data.csv.gz` (included in distribution)

## Manual Import

You can also manually import CSV files via the menu:
- Select "Import CSV File..." from the menu
- Supports both `.csv` and `.csv.gz` files
- File must be in OWID COVID-19 format (containing columns: iso_code, location, date, etc.)
