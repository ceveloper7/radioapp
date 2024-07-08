package org.radio.ui;

import org.radio.db.CConnection;
import org.radio.model.Patient;
import org.radio.model.Study;

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
import java.util.ArrayList;
import java.util.List;

public class CatalogStudy extends JPanel {
    JTable table;
    TModel dataModel;
    JButton btnAdd;
    JButton btnEdit;
    JButton btnDelete;

    private CatalogModel model;

    public CatalogStudy() {
        super();
        model = new CatalogModel();
        initComponents();
    }


    private interface CatalogModelListener {
        // pasamos el objeto CompanyData y la fila segun su operacion
        public void dataInserted(Study data, int row);
        public void dataUpdated(Study data, int row);
        public void dataDeleted(Study data, int row);
    }

    private class CatalogModel{
        private String columnNames[] = {"FECHA ESTUDIO", "APELLIDOS Y NOMBRES", "    D.N.I PACIENTE"};
        private List<Study> data;
        private List<CatalogModelListener> listeners;

        private CatalogModel(){
            data = new ArrayList<>();
            listeners = new ArrayList<>();
        }

        private void loadData() throws SQLException {
            String sql = """
                    SELECT 
                            s.id,
                            p.dni,
                            p.last_name,
                            p.first_name,
                            s,study_date,
                    FROM
                            patient p
                    RIGHT JOIN
                            study s ON s.patient_id = p.id
                    WHERE s.status = 1
                    ORDER BY s.study_date DESC
                    """;
            try(
                    Connection conn = CConnection.connection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    ){
                while (rs.next()){
                    Patient patient = new Patient(rs.getInt("dni"), rs.getString("first_name"), rs.getString("last_name"), "");
                    Study studyList = new Study(
                            rs.getInt("id"),
                            rs.getDate("study_date").toLocalDate(),
                            patient
                    );
                    data.add(studyList);
                }
                System.out.println(data.size() + " rows read.");
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

        private Study getRow(int index) {
            return data.get(index);
        }

        private int add(Study study){
            data.add(study);
            return data.size()-1;
        }

        private int findRow(int id_study){
            int row = 0;
            for(Study s : data){
                if(s.studyId() == id_study){
                    return row;
                }
                row++;
            }
            return -1;
        }

        private boolean insertData(){return false;}
        private boolean updateData(){return false;}
        private boolean deleteData(){return false;}

        private void addListener(CatalogModelListener listener) {
            listeners.add(listener);
        }
    }

    private class TModel extends AbstractTableModel {

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Study d = model.getRow(rowIndex);
            switch (columnIndex){
                case 0:
                    return d.date();
                case 1:
                    return d.patient().lastName() + " " + d.patient().firstName();
                case 2:
                    return d.patient().dni();
                default:
                    return "?";
            }
        }

        @Override
        public String getColumnName(int column) {
            return model.getColumnName(column);
        }

        @Override
        public int getRowCount() {
            return model.getRowCount();
        }

        @Override
        public int getColumnCount() {
            return model.getColumnCount();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    private void editRow(){}

    private void initComponents(){
        setPreferredSize(new Dimension(600, 300));

        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setLayout(new BorderLayout());

        dataModel = new TModel();
        table = new JTable(dataModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
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

        btnAdd = new JButton("Nuevo");
        btnAdd.addActionListener((e) -> {
//            CompanyForm form = new CompanyForm(SwingUtilities.getWindowAncestor(this), model.companyStatusMap);
//            form.setVisible(true);
//            if (form.okWasSelected())
//                model.insertData(form.getSymbol(), form.getName(), form.getId_companyStatus());
        });
        btnEdit = new JButton("Editar");
        btnEdit.addActionListener((e) -> {
            editRow();
        });
        btnDelete = new JButton("Borrar");
        btnDelete.addActionListener((e) -> {
            if (JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this), "¿Deseas borrar este registro?", "Borrar registro", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            int row = table.getSelectedRow();
            if (row >= 0) {
                Study d = model.getRow(row);
                //model.deleteData(d.studyId());
            }
        });
        lowerPanel.add(btnAdd);
        lowerPanel.add(btnEdit);
        lowerPanel.add(btnDelete);

        add(scrollPane, BorderLayout.CENTER);
        add(lowerPanel, BorderLayout.SOUTH);

        disableButtons();

        //
        model.addListener(new CatalogModelListener() {
            @Override
            public void dataInserted(Study data, int row) {
                dataModel.fireTableRowsInserted(row, row);
            }
            @Override
            public void dataUpdated(Study data, int row) {
                dataModel.fireTableRowsUpdated(row, row);
            }
            @Override
            public void dataDeleted(Study data, int row) {
                dataModel.fireTableRowsDeleted(row, row);
            }
        });
    }

    private void disableButtons() {
        btnAdd.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    private void enableButtons() {
        btnAdd.setEnabled(true);
        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                model.loadData();
                SwingUtilities.invokeLater(() -> {
                    dataModel.fireTableDataChanged();
                    enableButtons();
                });
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                            "No se puede obtener la información de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                });
                System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
            }
        });
        t.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CatalogStudy panel = new CatalogStudy();

            JFrame frame = new JFrame("Catalogo de estudio");
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
