package org.bahmni.reports.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.*;
import org.bahmni.reports.template.BaseReportTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;


@Controller
public class MainReportController {

    private static final Logger logger = Logger.getLogger(MainReportController.class);
    private JasperResponseConverter converter;
    private BahmniReportsProperties bahmniReportsProperties;
    private AllDatasources allDatasources;

    @Autowired
    public MainReportController(JasperResponseConverter converter,
                                BahmniReportsProperties bahmniReportsProperties,
                                AllDatasources allDatasources) {
        this.converter = converter;
        this.bahmniReportsProperties = bahmniReportsProperties;
        this.allDatasources = allDatasources;
    }

    //TODO: Better way to handle the response.
    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public void getReport(HttpServletRequest request, HttpServletResponse response) {
        Connection connection = null;
        ArrayList<AutoCloseable> resources = new ArrayList<>();
        try {
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String reportName = request.getParameter("name");
            String responseType = request.getParameter("responseType");
            String macroTemplateLocation = request.getParameter("macroTemplateLocation");
            PageType pageType = "A3".equals(request.getParameter("paperSize")) ? PageType.A3 : PageType.A4;

            Report report = findReport(reportName);
            BaseReportTemplate reportTemplate = getReportTemplate(report);
            connection = getConnectionFromDatasource(reportTemplate);

            JasperReportBuilder jasperReport = report();

            jasperReport = new ReportHeader().add(jasperReport, reportName, startDate, endDate);

            JasperReportBuilder reportBuilder = reportTemplate.build(connection, jasperReport, report, startDate, endDate, resources,
                    pageType);

            if(isMixedReport(report)) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("startDate", startDate);
                param.put("endDate", endDate);
                param.put("resources", resources);
                param.put("pageType", pageType);
                reportBuilder = addJasperSubreports(reportBuilder, report, param);
            }

            convertToResponse(responseType, reportBuilder, response, reportName, macroTemplateLocation);

            resources.add(connection);
        } catch (Throwable e) {
            logger.error("Error running report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            try {
                response.flushBuffer();
                response.getOutputStream().close();
                if(null != connection) {
                    connection.rollback();
                }
            } catch (IOException e) {
                logger.error(e);
            } catch (SQLException e) {
                logger.error(e);
            }

            for (AutoCloseable resource : resources) {
                try {
                    if (resource != null) {
                        resource.close();
                    }
                } catch (Exception e) {
                    logger.error("Could not close resource.", e);
                }
            }
        }
    }

    private Connection getConnectionFromDatasource(BaseReportTemplate reportTemplate) {
        return allDatasources.getConnectionFromDatasource(reportTemplate);
    }

    private BaseReportTemplate getReportTemplate(Report report) {
        return report.getTemplate(bahmniReportsProperties);
    }

    private Report findReport(String reportName) throws IOException {
        return Reports.find(reportName, bahmniReportsProperties.getConfigFilePath());
    }

    private boolean isMixedReport(Report report){
        boolean mixedReport = false;
        if(report.getType().equals("mixedReport")){
            mixedReport = true;
        }
        return mixedReport;
    }

    private JasperReportBuilder addJasperSubreports(JasperReportBuilder masterJasperReport, Report masterReport, Map<String, Object> reportParameters) {
        HorizontalListBuilder builder = cmp.horizontalList();
        ArrayList<SubreportBuilder> subreports = new ArrayList<>();

        MixedReportConfig mixedReportConfig = ((MixedReportConfig)masterReport.getConfig());

        subreports.addAll(addSubreports(reportParameters, mixedReportConfig));
        subreports.addAll(addCustomSubreports(reportParameters, mixedReportConfig));

        for(SubreportBuilder subreport : subreports){
            builder.add(subreport);
        }
        masterJasperReport.title(builder);

        return masterJasperReport;
    }

    private ArrayList<SubreportBuilder> addCustomSubreports(Map<String, Object> reportParameters, MixedReportConfig mixedReportConfig) {
        String subreportConfigFilePath = mixedReportConfig.getSubreportConfigFilePath();
        ArrayList<SubreportBuilder> subreports = new ArrayList<>();

        if (StringUtils.isNotBlank(subreportConfigFilePath)){
            try{
                ObjectMapper objectMapper = new ObjectMapper();
                Reports reports = objectMapper.readValue(new File(subreportConfigFilePath), Reports.class);
                for (Report subreportConfig : reports.values()) {
                    subreports.add(cmp.subreport(buildSubreport(subreportConfig.getName(), reportParameters, subreportConfig.getConfig())));
                }
            } catch (IOException e){
                logger.error("Error adding subreport", e);
            }
        }
        return subreports;
    }

    private ArrayList<SubreportBuilder> addSubreports(Map<String, Object> reportParameters, MixedReportConfig mixedReportConfig) {
        Collection<String> subreportNames = mixedReportConfig.getSubreportNames();
        ArrayList<SubreportBuilder> subreports = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(subreportNames)){
            SubreportBuilder subreport = null;
            for(String subreportName : subreportNames){
                subreport = cmp.subreport(buildSubreport(subreportName, reportParameters, null));
                subreports.add(subreport);
            }
        }
        return subreports;
    }

    private JasperReportBuilder buildSubreport(String subreportName, Map<String, Object> reportParameters, Config reportConfigTobeMerged) {
        Connection connection = null;
        JasperReportBuilder subreportBuilder = null;

        try{
            Report subreport = findReport(subreportName);

            mergeReportConfig(subreport, reportConfigTobeMerged);

            BaseReportTemplate subreportTemplate = subreport.getTemplate(bahmniReportsProperties);
            connection = allDatasources.getConnectionFromDatasource(subreportTemplate);
            JasperReportBuilder jasperReport = report();

            String startDate = (String)reportParameters.get("startDate");
            String endDate = (String)reportParameters.get("endDate");
            PageType pageType = (PageType)reportParameters.get("pageType");
            ArrayList<AutoCloseable> resources = (ArrayList<AutoCloseable>)reportParameters.get("resources");

            subreportBuilder = subreportTemplate.build(connection, jasperReport, subreport, startDate, endDate, resources, pageType);

            resources.add(connection);

        } catch (IOException e){
            logger.error("Error running subreport", e);
        }

        return subreportBuilder;
    }

    private void mergeReportConfig(Report subreport, Config reportConfigTobeMerged) throws IOException {
        if (subreport.getConfig() != null && reportConfigTobeMerged != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            ObjectReader updater = objectMapper.readerForUpdating(subreport.getConfig());
            Config merged = updater.readValue(objectMapper.writeValueAsString(reportConfigTobeMerged));
        }
    }

    private void convertToResponse(String responseType, JasperReportBuilder reportBuilder, HttpServletResponse response, String fileName, String macroTemplateLocation)
            throws Exception {
        try {
            converter.convert(responseType, reportBuilder, response, fileName, macroTemplateLocation);
        } catch (DRException | IOException e) {
            logger.error("Could not convert response", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
