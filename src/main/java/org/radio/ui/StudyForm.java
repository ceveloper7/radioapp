package org.radio.ui;

import org.jdatepicker.JDatePanel;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.UtilDateModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

public class StudyForm extends JDialog {
    private JSeparator separator;
    private static final int	FIELDLENGTH = 15;
    private JLabel lblPatientDni = new JLabel("D.N.I.");
    private JTextField txtPatientDni = new JTextField(FIELDLENGTH);
    private JLabel lblPatientName = new JLabel("Nombres");
    private JTextField txtPatientName = new JTextField(FIELDLENGTH);

    private JLabel lblStudyDate = new JLabel("Fecha");
    UtilDateModel dateModel = new UtilDateModel();
    JDatePanel datePanel = new JDatePanel(dateModel);
    JDatePicker datePicker = new JDatePicker(dateModel);

    private JLabel lblStudyName = new JLabel("Estudio");
    private JComboBox cboStudy = new JComboBox();
    private JLabel lblSerie = new JLabel("Serie");
    private JComboBox cboSerie = new JComboBox();
    private JLabel lblctdi = new JLabel("Dosis ctdi");
    private JTextField txtCtdi = new JTextField(FIELDLENGTH);
    private JLabel lblDlp = new JLabel("Dosis dlp");
    private JTextField txtDlp = new JTextField(FIELDLENGTH);
    private JLabel lblEffect = new JLabel("D. Efectiva");
    private JTextField txtEffect = new JTextField(FIELDLENGTH);


    private JTextArea txtObs = new JTextArea();
    private boolean okSelected = false;

    private Map<Integer, String> patientMap;
    private Map<Integer, String> studyMap;
    private Map<Integer, String> serieMap;

    private Integer patientIndex[];
    private Integer studyIndex[];
    private Integer serieIndex[];

    public StudyForm(Window owner){
        initComponents();
        setModal(true);
    }
    public StudyForm(Window owner, Map<Integer, String> patientMap, Map<Integer, String> studyMap, Map<Integer, String> serieMap) {
        super(owner, "Estudio Info");
        this.patientMap = patientMap;
        this.studyMap = studyMap;
        this.serieMap = serieMap;

        patientIndex = new Integer[patientMap.size()];
        int p = 0;
        for(Integer i : patientMap.keySet()) {
            patientIndex[p++] = i;
        }

        studyIndex = new Integer[studyMap.size()];
        int z = 0;
        for(Integer i : studyMap.keySet()) {
            studyIndex[z++] = i;
        }

        serieIndex = new Integer[serieMap.size()];
        int s = 0;
        for(Integer i : serieMap.keySet()) {
            serieIndex[s++] = i;
        }
        initComponents();
        setModal(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        TitledBorder titledBorder = new TitledBorder("dummy");

        GridBagLayout gl = new GridBagLayout();
        JPanel mainPanel = new JPanel(gl);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel sectionLabel = new JLabel("Datos del estudio");
        sectionLabel.setForeground(titledBorder.getTitleColor());
        separator = new JSeparator();

        mainPanel.add(sectionLabel,    new GridBagConstraints(0, 12, 6, 1, 0.0, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 5, 0, 0), 0, 0));
        mainPanel.add(separator,    new GridBagConstraints(0, 13, 7, 1, 1.0, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 10), 0, 0));
        mainPanel.add(lblPatientDni,  new GridBagConstraints(0, 14, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 2, 5), 0, 0));
        mainPanel.add(txtPatientDni,  new GridBagConstraints(1, 14, 1, 1, 0.0, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 2, 5), 0, 0));
        mainPanel.add(lblPatientName,	new GridBagConstraints(0, 15, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 2, 5), 0, 0));
        txtPatientName.setEditable(false);
        mainPanel.add(txtPatientName,   new GridBagConstraints(1, 15, 1, 1, 0.5, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 0), 0, 0));
        mainPanel.add(lblctdi,		new GridBagConstraints(4, 15, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 2, 5), 0, 0));
        mainPanel.add(txtCtdi,     new GridBagConstraints(5, 15, 1, 1, 0.0, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 2, 0), 0, 0));
        mainPanel.add(lblStudyDate,		new GridBagConstraints(0, 16, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 5, 2, 5), 0, 0));
        mainPanel.add(datePicker,		new GridBagConstraints(1, 16, 1, 1, 0.5, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 5, 2, 0), 0, 0));
        mainPanel.add(lblDlp,  	new GridBagConstraints(4, 16, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 0, 2, 5), 0, 0));
        mainPanel.add(txtDlp,  	new GridBagConstraints(5, 16, 1, 1, 0.5, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 5, 2, 0), 0, 0));
        mainPanel.add(lblStudyName,		new GridBagConstraints(0, 17, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 5, 2, 5), 0, 0));
        mainPanel.add(cboStudy,     new GridBagConstraints(1, 17, 1, 1, 0.5, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 5, 2, 0), 0, 0));
        mainPanel.add(lblEffect,   new GridBagConstraints(4, 17, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 5, 2, 5), 0, 0));
        mainPanel.add(txtEffect,   new GridBagConstraints(5, 17, 1, 1, 0.5, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 0), 0, 0));
        mainPanel.add(lblSerie,     new GridBagConstraints(0, 18, 1, 1, 0.0, 0.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 5, 2, 5), 0, 0));
        mainPanel.add(cboSerie,		new GridBagConstraints(1, 18, 1, 1, 0.5, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 5, 2, 0), 0, 0));

        sectionLabel = new JLabel("Observaciones");
        sectionLabel.setForeground(titledBorder.getTitleColor());
        separator = new JSeparator();
        mainPanel.add(sectionLabel,    new GridBagConstraints(0, 19, 6, 1, 0.0, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 5, 0, 0), 0, 0));
        mainPanel.add(separator,    new GridBagConstraints(0, 20, 7, 1, 1.0, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 10), 0, 0));

        mainPanel.add(txtObs,   new GridBagConstraints(1, 21, 1, 1, 0.5, 0.0
                ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 0), 0, 0));

        add(mainPanel, BorderLayout.CENTER);
        //add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }

}
