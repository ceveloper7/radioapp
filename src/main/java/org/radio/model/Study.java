package org.radio.model;

import java.time.LocalDate;
import java.util.Set;

public record Study(int studyId, LocalDate date, Patient patient) {

}
