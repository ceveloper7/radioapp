package org.radio.ui;

import org.radio.db.CConnection;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
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
        private LocalDate studyDate;
        private int zoneId;
        private int serieId;
        private double ctdi;
        private double dlp;
        private double dosisEffect;
        private String observations;
    }

    private interface StudyModelListener{
        public void dataInsert(StudyData data, int row);
        public void dataUpdate(StudyData data, int row);
        public void dataDelete(StudyData data, int row);
    }

    private class StudyModel{
        private String[] columnNames = {
                "  DNI  ",
                "APELLIDOS Y  NOMBRES",
                "  FECHA  ",
                "ESTUDIO",
                "SERIE",
                "CTDI",
                "DLP",
                "D-EFECT."
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
                        WHERE s.status=1
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
                    study.studyDate = rs.getDate("study_date").toLocalDate();
                    study.zoneId = rs.getInt("zone_id");
                    study.serieId = rs.getInt("serie_id");
                    study.ctdi = rs.getDouble("dosis_ctdi");
                    study.dlp = rs.getDouble("dosis_dlp");
                    study.dosisEffect = rs.getDouble("dosis_effective");
                    String observations = rs.getString("observations");
                    if(observations == null){
                        study.observations = "Sin descripcion";
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

        private boolean insertStudy(int dni, java.sql.Date studyDate, int studyZoneId, int studySerieId, double ctdi, double dlp,
                                    double dosisEffective, String observations, String patientName){
            int studyId = -1;
            String sqlInsert = """
                    INSERT INTO study(study_date, observations, dosis_ctdi, dosis_dlp, dosis_effective, patient_id, zone_id, serie_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try(Connection conn = CConnection.connection();
                PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)){

                pstmt.setDate(1, studyDate);
                pstmt.setString(2, observations);
                pstmt.setDouble(3, ctdi);
                pstmt.setDouble(4, dlp);
                pstmt.setDouble(5, dosisEffective);
                pstmt.setInt(6, dni);
                pstmt.setInt(7, studyZoneId);
                pstmt.setInt(8, studySerieId);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                rs.next();
                studyId = rs.getInt(1);
                rs.close();
            }
            catch (SQLException ex){
                System.out.println(ex.getClass().getName() + " generated: " + ex.getMessage());
                return false;
            }

            StudyData study = new StudyData();
            study.studyId = studyId;
            study.patientDni = dni;
            study.patientName = patientName;
            study.studyDate = studyDate.toLocalDate();
            study.zoneId = studyZoneId;
            study.serieId = studySerieId;
            study.ctdi = ctdi;
            study.dlp = dlp;
            study.dosisEffect = dosisEffective;
            study.observations = observations;
            int row = model.add(study);

            for(StudyModelListener listener : listeners){
                listener.dataInsert(study, row);
            }
            return true;
        }

        private boolean updateStudy(int studyId, int dni, LocalDate studyDate, int studyZoneId, int StudySerieId,
                                    double ctdi, double dlp, double dosisEffective, String observations, String patientName){
            String sqlUpdate = """
                    UPDATE study set study_date=?, observations=?, dosis_ctdi=?, dosis_dlp=?, 
                                 zone_id=?, serie_id=?
                    WHERE id=?
                    """;
            int row = findRow(studyId);
            if(row == -1){
                return false;
            }
            StudyData d = getRow(studyId);

            try(Connection conn = CConnection.connection();
                PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)){
                pstmt.setDate(1, new Date(studyDate.getYear(), studyDate.getMonthValue(), studyDate.getDayOfMonth()));
                if(observations != null){
                    pstmt.setString(2, observations);
                }else{
                    pstmt.setString(2,"");
                }

                pstmt.setDouble(3, ctdi);
                pstmt.setDouble(4, dlp);
                pstmt.setInt(5, studyZoneId);
                pstmt.setInt(6, StudySerieId);
                pstmt.setInt(7, studyId);
                pstmt.executeUpdate();
            }
            catch(SQLException ex){
                ex.printStackTrace();
                //System.out.println(ex.getClass().getName() + " generated: " + ex.getMessage());
                return false;
            }

            d.studyDate = studyDate;
            d.observations = observations;
            d.ctdi = ctdi;
            d.dlp = dlp;
            d.zoneId = studyZoneId;
            d.serieId = StudySerieId;

            for(StudyModelListener listener : listeners){
                listener.dataUpdate(d, row);
            }
            return true;
        }

        private boolean deleteStudy(int studyId){
            int row = findRow(studyId);
            if(row == -1){
                return false;
            }

            StudyData d = getRow(row);
            try(Connection conn = CConnection.connection();
                PreparedStatement pstmt = conn.prepareStatement("UPDATE study set status=0 WHERE id=?")){
                pstmt.setInt(1, studyId);
                pstmt.executeUpdate();
            }
            catch(SQLException ex){
                System.out.println(ex.getClass().getName() + " generated: " + ex.getMessage());
                return false;
            }

            data.remove(d);
            for(StudyModelListener listener : listeners){
                listener.dataDelete(d, row);
            }
            return true;
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
                    return d.patientDni;
                case 1:
                    return d.patientName;
                case 2:
                    return d.studyDate;
                case 3:
                    return model.getStudyZoneLabel(d.zoneId);
                case 4:
                    return model.getStudySerieLabel(d.serieId);
                case 5:
                    return d.ctdi;
                case 6:
                    return d.dlp;
                case 7:
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
        int row = table.getSelectedRow();
        if(row >= 0){
            StudyData d = model.getRow(row);

            StudyInfo form = new StudyInfo(SwingUtilities.getWindowAncestor(this), model.studyNameMap, model.serieNameMap);
            form.setData(d.patientDni, d.patientName, d.studyDate, d.zoneId, d.serieId, d.ctdi, d.dlp, d.dosisEffect, d.observations);
            form.setTitle("Estudio: " + d.patientName + " - " + d.studyDate);
            form.setVisible(true);
            if(form.okWasSelected()){
                //Date dt = new Date(d.studyDate.getYear(), d.studyDate.getMonth().getValue(), d.studyDate.getDayOfMonth());
                model.updateStudy(d.studyId, d.patientDni, form.getStudyDate(), form.getId_studyZone(), form.getId_serie(),
                        form.getCtdi(), form.getDlp(), form.getEffect(), form.getObs(), d.patientName);
            }
        }
    }

    private void initComponents(){
        setPreferredSize(new Dimension(800, 600));
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

        int w3 = fm.stringWidth(model.getColumnName(3)) + fm.getMaxAdvance();
        col = table.getColumnModel().getColumn(3);
        col.setPreferredWidth(w3);
        col.setMinWidth(w3);

        int w4 = fm.stringWidth(model.getColumnName(4)) + fm.getMaxAdvance();
        col = table.getColumnModel().getColumn(4);
        col.setPreferredWidth(w4);
        col.setMinWidth(w4);

        int w5 = fm.stringWidth(model.getColumnName(5)) + fm.getMaxAdvance();
        col = table.getColumnModel().getColumn(5);
        col.setPreferredWidth(w5);
        col.setMinWidth(w5);

        int w6 = fm.stringWidth(model.getColumnName(6)) + fm.getMaxAdvance();
        col = table.getColumnModel().getColumn(6);
        col.setPreferredWidth(w6);
        col.setMinWidth(w6);

        int w7 = fm.stringWidth(model.getColumnName(7)) + fm.getMaxAdvance();
        col = table.getColumnModel().getColumn(7);
        col.setPreferredWidth(w7);
        col.setMinWidth(w7);

        int w8 = fm.stringWidth(model.getColumnName(1))+ fm.getMaxAdvance();
        col = table.getColumnModel().getColumn(1);
        col.setPreferredWidth(w8);
        col.setMinWidth(w8);

        JScrollPane scrollPane = new JScrollPane(table);
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new FlowLayout());

        btnAdd = new JButton("Nuevo estudio");
        btnEdit = new JButton("Modificar estudio");
        btnDelete = new JButton("Eliminar estudio");

        btnAdd.addActionListener((e) -> {
            StudyForm form = new StudyForm(SwingUtilities.getWindowAncestor(this), model.studyNameMap, model.serieNameMap);
            form.setVisible(true);
        });

        btnEdit.addActionListener((e -> {
            editRow();
        }));

        lowerPanel.add(btnAdd);
        lowerPanel.add(btnEdit);
        lowerPanel.add(btnDelete);

        add(scrollPane, BorderLayout.CENTER);
        add(lowerPanel, BorderLayout.SOUTH);

        disabledButtons();

        model.addListener(new StudyModelListener() {
            @Override
            public void dataInsert(StudyData data, int row) {
                dataModel.fireTableRowsInserted(row, row);
            }

            @Override
            public void dataUpdate(StudyData data, int row) {
                dataModel.fireTableRowsUpdated(row, row);
            }

            @Override
            public void dataDelete(StudyData data, int row) {
                dataModel.fireTableRowsDeleted(row, row);
            }
        });
    }

    private void disabledButtons(){
        btnAdd.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    private void enabledButtons(){
        btnAdd.setEnabled(true);
        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    private void loadData(){
        Thread t = new Thread(()->{
            try{
                model.loadData();
                SwingUtilities.invokeLater(()->{
                    dataModel.fireTableDataChanged();
                    enabledButtons();
                });
            }catch (SQLException ex){
                SwingUtilities.invokeLater(()->{
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Error. No se puede" +
                            " obtener la informacion de la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
                });
                System.out.println(ex.getClass().getName() + " generated: " + ex.getMessage());
            }
        });
        t.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RadioDosis panel = new RadioDosis();
            JFrame frame = new JFrame("Listado de Estudios");
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
