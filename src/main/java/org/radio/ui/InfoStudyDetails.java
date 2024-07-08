package org.radio.ui;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class InfoStudyDetails extends JDialog {
    private JButton btnAccept;
    private JButton btnCancel;
    private JSeparator separator;
    private static final int	FIELDLENGTH = 15;
    // patient info
    private JLabel lblPatientDni = new JLabel("D.N.I.");
    private JTextField txtPatientDni = new JTextField(FIELDLENGTH);
    private JLabel lblPatientName = new JLabel("Nombres");
    private JTextField txtPatientName = new JTextField(FIELDLENGTH);

    // study info
    private JLabel lblStudyDate = new JLabel("Fecha");
    UtilDateModel dateModel = new UtilDateModel();
    JDatePicker datePicker = new JDatePicker(dateModel);

    // study detail table
    private JTable table;
    //private TModel dataModel;

    private Map<Integer, Integer> patientStudyMap;

    public InfoStudyDetails(Window owner, Map<Integer, Integer> patientStudyMap) {
        super(owner);
        this.patientStudyMap = patientStudyMap;
    }
}
