package org.radio.ui;

import org.radio.db.CConnection;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class RadioDosis extends JPanel {
    JTable table;
    TModel dataModel;
    JButton btnAdd;
    JButton btnEdit;

    private StudyModel model;

    public RadioDosis(){
        super();
        model = new StudyModel();
        initComponents();
    }

    private class StudyData(){
        private int studyId;
        private int patientDni;
        private String patientName;
        private Date studyDate;
        private int zone;
        private int serie;
        private double ctdi;
        private double dlp;
        private double dosisEfect;
    }

    private interface StudyModelListener{
        public void dataInsert(StudyData data, int row);
        public void dataUpdate(StudyData data, int row);
    }

    private class StudyModel{
        private String[] columnNames = {
                "APELLIDOS Y  NOMBRES",
                "FECHA",
                "ESTUDIO",
                "SERIE",
                "CTDI",
                "DLP",
                "D-EFECT"
        };
        private Map<Integer, String> patientMap;
        private Map<Integer, String> zoneMap;
        private Map<Integer, String> serieMap;
        private List<StudyData> data;

        private List<StudyModelListener> listeners;

        private StudyModel(){
            data = new ArrayList<>();
            listeners = new ArrayList<>();
            patientMap = new HashMap<>();
            zoneMap = new HashMap<>();
            serieMap = new HashMap<>();
        }

        private void loadData()throws SQLException {
            String sql = "SELECT id, study_date, observations";
            try(Connection conn = CConnection.connection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)){

            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RadioDosis panel = new RadioDosis();
            JFrame frame = new JFrame("Radio dosis");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(panel);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    panel.loadData();
                }
            });
            frame.pack();
            frame.setMinimumSize(new Dimension(panel.getWidth(), panel.getHeight()));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
