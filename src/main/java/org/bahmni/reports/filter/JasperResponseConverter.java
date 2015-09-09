package org.bahmni.reports.filter;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsxExporterBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.bahmni.reports.template.Templates;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@Component
public class JasperResponseConverter {

    public void convert(String responseType, JasperReportBuilder report, HttpServletResponse response, final String fileName, String macroTemplateLocation) throws
            IOException, DRException, SQLException, JRException {

        response.setContentType("application/vnd.ms-excel");
        ServletOutputStream outputStream = response.getOutputStream();
        switch (responseType) {
            case "text/html":
                response.setContentType("text/html");
                report.toHtml(outputStream);
                break;
            case "application/vnd.ms-excel":
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
                report.setTemplate(Templates.excelReportTemplate);
                report.toXlsx(outputStream);
                break;
            case "application/vnd.ms-excel-custom":
                // below code is to generate in xlsm format (xlsx is used from Excel 2007 but it does not support macros. Macro enabled excel will be stored with extension '.xlsm'
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsm");
                report.setTemplate(Templates.excelReportTemplate);
                JasperXlsxExporterBuilder exporterBuilder = Exporters.xlsxExporter(outputStream).setDetectCellType(true);
                exporterBuilder.setKeepWorkbookTemplateSheets(true);
                exporterBuilder.setMacroTemplate(macroTemplateLocation);
                exporterBuilder.addSheetName("Report");
                report.toXlsx(exporterBuilder);
                File templateFile = new File(macroTemplateLocation);
                templateFile.delete();
                // below code is to generate in xls format
//                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
//                report.setTemplate(Templates.excelReportTemplate);
//                JasperXlsExporterBuilder exporterBuilder = Exporters.xlsExporter(outputStream).setDetectCellType(true);
//                exporterBuilder.setKeepWorkbookTemplateSheets(false);
//                exporterBuilder.setWorkbookTemplate(macroTemplateLocation);
//                exporterBuilder.addSheetName("Report");
//                report.toXls(exporterBuilder);
                break;

            case "application/pdf":
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".pdf");
                report.toPdf(outputStream);
        }
    }
}
