package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MixedReportConfig implements Config{

    private Collection subreportNames;
    private String subreportConfigFilePath;


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
}
