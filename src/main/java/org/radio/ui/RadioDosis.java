package org.radio.ui;

import org.radio.db.CConnection;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
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
    JButton btnDelete;

    private StudyModel model;

    public RadioDosis(){
        super();
        model = new StudyModel();
        initComponents();
    }

    private class StudyData{
        private int studyId;
        private int patientDni;
        private String patientName;
        private Date studyDate;
        private int zoneId;
        private int serieId;
        private double ctdi;
        private double dlp;
        private double dosisEffect;
        private String observaions;
    }

    private interface StudyModelListener{
        public void dataInsert(StudyData data, int row);
        public void dataUpdate(StudyData data, int row);
        public void dataDelete(StudyData data, int row);
    }

    private class StudyModel{
        private String[] columnNames = {
                "DNI",
                "APELLIDOS Y  NOMBRES",
                "FECHA",
                "ESTUDIO",
                "SERIE",
                "CTDI",
                "DLP",
                "D-EFECT"
        };

        private List<StudyData> data;
        private Map<Integer, String> studyNameMap;
        private Map<Integer, String> serieNameMap;

        private List<StudyModelListener> listeners;

        private StudyModel(){
            data = new ArrayList<>();
            listeners = new ArrayList<>();
            studyNameMap = new HashMap<>();
            serieNameMap = new HashMap<>();
        }

        private void loadData()throws SQLException {
            String sqlZoneName = "SELECT id, name FROM zone";
            String sqlSerieName = "SELECT id, name FROM serie";
            String sqlStudies = """
                        SELECT 
                                s.id,p.dni,concat(p.last_name,' ',p.first_name) as name,
                                s.study_date,s.zone_id,s.serie_id,s.dosis_ctdi,s.dosis_dlp,s.dosis_effective,s.observations
                        FROM
                                patient p
                        RIGHT JOIN study s on s.patient_dni = p.dni
                        ORDER BY s.id
                        """;

            try(Connection conn = CConnection.connection()){
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlZoneName);
                while(rs.next()){
                    int id_ZoneName = rs.getInt("id");
                    String name = rs.getString("name");
                    studyNameMap.put(id_ZoneName, name);
                }
                rs.close();

                rs = stmt.executeQuery(sqlSerieName);
                while(rs.next()){
                    int id_SerieName = rs.getInt("id");
                    String name = rs.getString("name");
                    serieNameMap.put(id_SerieName, name);
                }
                rs.close();

                rs = stmt.executeQuery(sqlStudies);
                while(rs.next()){
                    StudyData study = new StudyData();
                    study.studyId = rs.getInt("id");
                    study.patientDni =  rs.getInt("dni");
                    study.patientName = rs.getString("name");
                    Date date = new Date();
                    date = rs.getDate("study_date");
                    study.studyDate = date;
                    study.zoneId = rs.getInt("zone_id");
                    study.serieId = rs.getInt("serie_id");
                    study.ctdi = rs.getDouble("dosis_ctdi");
                    study.dlp = rs.getDouble("dosis_dlp");
                    study.dosisEffect = rs.getDouble("dosis_effective");
                    String observations = rs.getString("observations");
                    if(observations == null){
                        study.observaions = "Sin descripcion";
                    }
                    data.add(study);
                }
                System.out.println(data.size() + " rows read");
                stmt.close();
            }
        }
        private String getColumnName(int index) {
            return columnNames[index];
        }

        private int getColumnCount() {
            return columnNames.length;
        }

        private int getRowCount() {
            return data.size();
        }

        private StudyData getRow(int index) {
            return data.get(index);
        }

        private int add(StudyData study) {
            data.add(study);
            return data.size()-1;
        }

        private int findRow(int id_study){
            int row = 0;
            for(StudyData sd : data){
                if(sd.studyId == id_study){
                    return row;
                }
                row++;
            }
            return -1;
        }

        private String getStudyZoneLabel(int id_studyZone){
            String studyZoneName = studyNameMap.get(id_studyZone);
            if(studyZoneName == null){
                studyZoneName = "N/A";
            }
            return studyZoneName;
        }

        private String getStudySerieLabel(int id_studySerie){
            String studySerieName = serieNameMap.get(id_studySerie);
            if(studySerieName == null){
                studySerieName = "N/A";
            }
            return studySerieName;
        }

        private boolean insertStudy(int dni, Date studyDate, int studyZoneId, int studySerieId, double ctdi, double dlp,
                double dosisEffective, String observations){
            return false;
        }

        private boolean updateStudy(int studyId, int dni, Date studyDate, int studyZoneId, int StudySerieId,
                                    double ctdi, double dlp, double dosisEffective, String observations){
            return false;
        }

        private boolean deleteStudy(int id_Study){
            return false;
        }

        private void addListener(StudyModelListener listener){
            listeners.add(listener);
        }
    }

    private class TModel extends AbstractTableModel{
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StudyData d = model.getRow(rowIndex);
            switch (columnIndex){
                case 0:
                    return d.studyId;
                case 1:
                    return d.patientDni;
                case 2:
                    return d.patientName;
                case 3:
                    return d.studyDate;
                case 4:
                    return model.getStudyZoneLabel(d.zoneId);
                case 5:
                    return model.getStudySerieLabel(d.serieId);
                case 6:
                    return d.ctdi;
                case 7:
                    return d.dlp;
                case 8:
                    return d.dosisEffect;
                default:
                    return "?";
            }
        }

        @Override
        public String getColumnName(int column) {
            return model.getColumnName(column);
        }

        @Override
        public int getColumnCount() {
            return model.getColumnCount();
        }

        @Override
        public int getRowCount() {
            return model.getRowCount();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    private void editRow(){
        //todo
    }

    private void initComponents(){
        setPreferredSize(new Dimension(1800, 600));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        setLayout(new BorderLayout());

        dataModel = new TModel();
        table = new JTable(dataModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    editRow();
                }
            }
        });
        Font tableFont = table.getFont();
        FontMetrics fm = table.getFontMetrics(tableFont);

        int w1 = fm.stringWidth(model.getColumnName(0)) + fm.getMaxAdvance();
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setPreferredWidth(w1);
        col.setMinWidth(w1);

        int w2 = fm.stringWidth(model.getColumnName(2)) + fm.getMaxAdvance();
        col = table.getColumnModel().getColumn(2);
        col.setPreferredWidth(w2);
        col.setMinWidth(w2);

        int w3 = getPreferredSize().width - w1 - w2;
        col = table.getColumnModel().getColumn(1);
        col.setPreferredWidth(w3);

        JScrollPane scrollPane = new JScrollPane(table);
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new FlowLayout());

//        lowerPanel.add(btnAdd);
//        lowerPanel.add(btnEdit);
//        lowerPanel.add(btnDelete);

        add(scrollPane, BorderLayout.CENTER);
        add(lowerPanel, BorderLayout.SOUTH);
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
                    //panel.loadData();
                }
            });
            frame.pack();
            frame.setMinimumSize(new Dimension(panel.getWidth(), panel.getHeight()));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
