package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.component.MultiPageListBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.MixedReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Reports;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.util.CommonComponents;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;

@UsingDatasource("openmrs")
public class MixedReportTemplate extends BaseReportTemplate<MixedReportConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<MixedReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);
        return jasperReport;
    }

}