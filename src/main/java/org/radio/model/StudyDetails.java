package org.radio.model;

public record StudyDetails(
        int studyDetailId,
        int studyId,
        Zone zone,
        Serie serie,
        double ctdi,
        double dlp,
        double effective,
        String observations
) {


}
