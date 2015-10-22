package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.MultiPageListBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
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
import org.bahmni.reports.web.ReportHeader;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;


import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UsingDatasource("openmrs")
public class MixedReportTemplate extends BaseReportTemplate<MixedReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;
    private final Logger logger = Logger.getLogger(MixedReportTemplate.class);

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<MixedReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);
        return jasperReport;
    }

    @Override
    public JasperReportBuilder addJasperSubreports(JasperReportBuilder masterJasperReport, Report report, Map<String, Object> param) {

        buildSubreportExpression(masterJasperReport, report, param);
//
//        SubreportBuilder subreport = cmp.subreport(new SubreportExpression());
//        SubreportBuilder subreport2 = cmp.subreport(new SubreportExpression());
//
//        masterJasperReport
//                .addDetail(
//                        subreport,
//                        cmp.verticalGap(20),
//                        subreport2)
//                .setDataSource(createDataSource());
        masterJasperReport.setDataSource(createDataSource());
        return  masterJasperReport;
    }

    private JasperReportBuilder buildSubreportExpression(JasperReportBuilder masterJasperReport, Report report, Map<String, Object> param) {
        MultiPageListBuilder builder = cmp.multiPageList();
        builder.setSplitType(SplitType.PREVENT);

        builder.add(cmp.subreport(buildNewSubreport(param)));
        builder.add(cmp.subreport(buildNewSubreport(param)));

        masterJasperReport.title(builder);

        return masterJasperReport;
    }

    private JRDataSource createDataSource() {
        return new JREmptyDataSource(1);
    }

    private JasperReportBuilder buildNewSubreport(Map<String, Object> reportParameters){
        Connection connection = null;
        JasperReportBuilder reportBuilder = null;

        String subreportName = (String)reportParameters.get("subreportName");
        bahmniReportsProperties = (BahmniReportsProperties)reportParameters.get("bahmniReportsProperties");
        String startDate = (String)reportParameters.get("startDate");
        String endDate = (String)reportParameters.get("endDate");
        PageType pageType = (PageType)reportParameters.get("pageType");
        ArrayList<AutoCloseable> resources = (ArrayList<AutoCloseable>)reportParameters.get("resources");
        connection = (Connection)reportParameters.get("connection");

        try{
            Report report = Reports.find(subreportName, bahmniReportsProperties.getConfigFilePath());
            BaseReportTemplate reportTemplate = report.getTemplate(bahmniReportsProperties);
//                connection = allDatasources.getConnectionFromDatasource(reportTemplate);
            JasperReportBuilder jasperReport = report();
            reportBuilder = reportTemplate.build(connection, jasperReport, report, startDate, endDate, resources,
                    pageType);

        } catch (IOException e){
            logger.error("Error running subreport", e);
        }

        return reportBuilder;

    }

    private class SubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {
        private static final long serialVersionUID = 1L;
        private BahmniReportsProperties bahmniReportsProperties;
        private final Logger logger = Logger.getLogger(SubreportExpression.class);


        @Override
        public JasperReportBuilder evaluate(ReportParameters reportParameters) {
            Connection connection = null;
            JasperReportBuilder reportBuilder = null;

            String subreportName = reportParameters.getParameterValue("subreportName");
            bahmniReportsProperties = reportParameters.getParameterValue("bahmniReportsProperties");
            String startDate = reportParameters.getParameterValue("startDate");
            String endDate = reportParameters.getParameterValue("endDate");
            PageType pageType = reportParameters.getParameterValue("pageType");
            ArrayList<AutoCloseable> resources = reportParameters.getParameterValue("resources");
            connection = reportParameters.getParameterValue("connection");

            try{
                Report report = Reports.find(subreportName, bahmniReportsProperties.getConfigFilePath());
                BaseReportTemplate reportTemplate = report.getTemplate(bahmniReportsProperties);
//                connection = allDatasources.getConnectionFromDatasource(reportTemplate);
                JasperReportBuilder jasperReport = report();
                reportBuilder = reportTemplate.build(connection, jasperReport, report, startDate, endDate, resources,
                        pageType);

            } catch (IOException e){
                logger.error("Error running subreport", e);
            }

            return reportBuilder;
        }
    }
}