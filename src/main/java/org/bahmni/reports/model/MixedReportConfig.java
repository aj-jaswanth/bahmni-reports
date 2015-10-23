package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MixedReportConfig implements Config{

    private Collection subreportNames;
    private String subreportConfigFilePath;
    private String reportPosition;

    public static final String MIXEDREPORT = "mixedReport";
    public static final String HORIZONTAL = "horizontal";
    public static final String VERTICAL = "vertical";


    public Collection getSubreportNames() {
        return subreportNames;
    }

    public void setSubreportNames(Collection subreportNames) {
        this.subreportNames = subreportNames;
    }

    public String getSubreportConfigFilePath() {
        return subreportConfigFilePath;
    }

    public void setSubreportConfigFilePath(String subreportConfigFilePath) {
        this.subreportConfigFilePath = subreportConfigFilePath;
    }

    public String getReportPosition() {
        return reportPosition;
    }

    public void setReportPosition(String reportPosition) {
        this.reportPosition = reportPosition;
    }
}
