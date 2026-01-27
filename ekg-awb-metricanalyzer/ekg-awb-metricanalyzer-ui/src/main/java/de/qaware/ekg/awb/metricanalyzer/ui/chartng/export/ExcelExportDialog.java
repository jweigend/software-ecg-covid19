package de.qaware.ekg.awb.metricanalyzer.ui.chartng.export;

import de.qaware.ekg.awb.sdk.core.resourceloader.CdiFxmlLoader;
import de.qaware.ekg.awb.sdk.core.resourceloader.ViewLoadResult;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 * Dedicated Export dialog to setup the
 * ExcelExport more detailed
 *
 * Note: This dialog is currently not ready and not in use!!!
 */
public class ExcelExportDialog extends Dialog<Void> {

    public ExcelExportDialog() {

        setTitle("Export chart data as Excel sheet");

        ViewLoadResult viewResult = CdiFxmlLoader.loadView("ExcelExportDialogView.fxml");
        setDialogPane(viewResult.getComponent());

        // bind controller to Dialog closed with apply
        setResultConverter(this::exportToExcelSheet);

        setHeaderText("Choose a path where the Excel sheet containing the chart data should be stored.");
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    }

    protected Void exportToExcelSheet(ButtonType buttonType) {
        return null;
    }
}
