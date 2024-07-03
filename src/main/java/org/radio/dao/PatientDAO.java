package org.radio.dao;

import org.radio.model.Patient;

import java.util.Set;

public interface PatientDAO {
    public static final String FIND_ALL = "SELECT dni, firstName, lastName, obs FROM patient";
    public static final String SELECT_BY_ID = "SELECT dni, firstName, lastName, obs FROM patient WHERE dni=?";
    public static final String SIMPLE_INSERT = "INSERT INTO patient(dni, firstName, lastName, obs) VALUES (?, ?, ?, ?)";
    public static final String SIMPLE_UPDATE = "UPDATE patient SET firstName = ?, lastName = ?, obs = ? WHERE id = ?";

    Set<Patient> findAll();
    Patient findById(int patientId);
    Patient save(Patient patient);
    void update(Patient patient);
    Patient findPatientWithStudies(int patientId);
}
