package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MixedReportConfig implements Config{

    private List<String> conceptNames;
    private List<String> subreportNames;


    public List<String> getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(List<String> conceptNames) {
        this.conceptNames = conceptNames;
    }

    public List<String> getSubreportNames() {
        return subreportNames;
    }

    public void setSubreportNames(List<String> subreportNames) {
        this.subreportNames = subreportNames;
    }

}
