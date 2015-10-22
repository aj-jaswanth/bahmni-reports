package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MixedReportConfig implements Config{

    private List<String> conceptNames;
    private Collection subreportNames;
    private String subreportConfigFilePath;


    public List<String> getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(List<String> conceptNames) {
        this.conceptNames = conceptNames;
    }

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
