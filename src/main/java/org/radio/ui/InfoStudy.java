package org.radio.ui;

import org.radio.db.CConnection;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

public class InfoStudy extends JPanel{
    // componentes relacionados a la vista
    JTable table;
    TModel dataModel;
    JButton btnAdd;
    JButton btnEdit;
    JButton btnDelete;

    private CatalogModel model;

    public InfoStudy(){
        super();
        model = new CatalogModel();
        initComponents();
    }

    private class StudyData {
        private int id_study;
        private LocalDate studyDate;
        private int patientDni;
        private String patientName;
        //private int id_companyStatus;
    }

    // interface que informa cambios en el modelo
    private interface CatalogModelListener {
        // pasamos el objeto CompanyData y la fila segun su operacion
        public void dataInserted(StudyData data, int row);
        public void dataUpdated(StudyData data, int row);
        public void dataDeleted(StudyData data, int row);
    }

    // clase que maneja los datos y separamos la funcionalidad
    private class CatalogModel {
        private String columnNames[] = {"FECHA ESTUDIO", "APELLIDOS Y NOMBRES", "    D.N.I PACIENTE"};
        //private Map<Integer, String> companyStatusMap;
        private List<StudyData> data;

        // informamos cambios en el modelo
        private List<CatalogModelListener> listeners;

        private CatalogModel() {
            data = new ArrayList<>();
            listeners = new LinkedList<>();
            //companyStatusMap = new HashMap<>();
        }

        // metodo que carga la informacion inical
        private void loadData() throws SQLException {
            Connection conn = CConnection.connection();
            Statement stmt = conn.createStatement();
            // query que obtiene los companyStatus
            //ResultSet rs = stmt.executeQuery("select id_companyStatus, code, description from companyStatus");
            // llenado del mapa companyStatusMap
//            while (rs.next()) {
//                int id_companyStatus = rs.getInt("id_companyStatus");
//                String code = rs.getString("code");
//                String description = rs.getString("description");
//                if (description != null)
//                    code += " " + description;
//                companyStatusMap.put(id_companyStatus, code);
//            }
//            rs.close();

            // leemos todas las companias
            ResultSet rs = stmt.executeQuery("select s.id, s.study_date, s.patient_dni, concat(p.last_name, ' ', p.first_name) as name from patient p right join study s on s.patient_dni = p.dni order by s.study_date desc");
            while (rs.next()) {
                StudyData company = new StudyData();
                company.id_study = rs.getInt("id");
                company.studyDate = rs.getDate("study_date").toLocalDate();
                company.patientDni = rs.getInt("patient_dni");
                company.patientName = rs.getString("name");
//                company.id_companyStatus = rs.getInt("id_companyStatus");
//                if (rs.wasNull())
//                    company.id_companyStatus = -1;
                data.add(company);
            }
            rs.close();

            System.out.println(data.size() + " rows read.");

            stmt.close();
            conn.close();
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

        // dado un numero de fila retorna un objeto CompanyData que corresponde a la fila
        private StudyData getRow(int index) {
            return data.get(index);
        }

        // agregamos un company a la coleccion y retornamos el numero de fila donde fue agregado
        private int add(StudyData company) {
            data.add(company);
            return data.size()-1;
        }

        // dado un id_company, conocemos que fila ocupa
        private int findRow(int id_company) {
            int row = 0;
            for (StudyData c : data) {
                if (c.id_study == id_company)
                    return row;
                row++;
            }
            return -1;
        }

        // dado un id_companyStatus obtenemos su descripcion o etiqueta
//        private String getCompanyStatusLabel(int id_companyStatus) {
//            String res = companyStatusMap.get(id_companyStatus);
//            if (res == null)
//                res = "?";
//            return res;
//        }

        private boolean insertData(String symbol, String name, int id_companyStatus) {
            int id_company = -1;

            try (Connection conn = CConnection.connection()) {
                PreparedStatement pstmt = conn.prepareStatement("insert into company (symbol, name, id_companyStatus) values (?, ? , ?)", Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, symbol);
                pstmt.setString(2, name);
                if (id_companyStatus != -1)
                    pstmt.setInt(3, id_companyStatus);
                else
                    pstmt.setNull(3, java.sql.Types.INTEGER);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                rs.next();
                id_company = rs.getInt(1);
                rs.close();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
                return false;
            }

            StudyData c = new StudyData();
            c.id_study = id_company;
//            c.studyDate = symbol;
//            c.patientDni = name;
//            c.id_companyStatus = id_companyStatus;
            int row = model.add(c);

            // llamamos a los listeners
            for (CatalogModelListener listener : listeners)
                // invocamos al meotod dataInserted y pasamos el registro y numero de fila
                listener.dataInserted(c, row);
            return true;
        }

        private boolean updateData(int id_company, String symbol, String name, int id_companyStatus) {
            int row = model.findRow(id_company);
            if (row == -1)
                return false;
            StudyData c = getRow(row);

            try (Connection conn = CConnection.connection()) {
                PreparedStatement pstmt = conn.prepareStatement("update company set symbol=?, name=?, id_companyStatus=? where id_company=?");
                pstmt.setString(1, symbol);
                pstmt.setString(2, name);
                if (id_companyStatus != -1)
                    pstmt.setInt(3, id_companyStatus);
                else
                    pstmt.setNull(3, java.sql.Types.INTEGER);
                pstmt.setInt(4, id_company);
                pstmt.executeUpdate();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
                return false;
            }

//            c.studyDate = symbol;
//            c.patientDni = name;
//            c.id_companyStatus = id_companyStatus;

            for (CatalogModelListener listener : listeners)
                listener.dataUpdated(c, row);

            return true;
        }

        private boolean deleteData(int id_company) {
            int row = findRow(id_company);
            if (row == -1)
                return false;
            StudyData c = getRow(row);

            try (Connection conn = CConnection.connection()) {
                PreparedStatement pstmt = conn.prepareStatement("delete from company where id_company=?");
                pstmt.setInt(1, id_company);
                pstmt.executeUpdate();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
                return false;
            }

            data.remove(row);
            for (CatalogModelListener listener : listeners)
                listener.dataDeleted(c, row);

            return true;
        }

        private void addListener(CatalogModelListener listener) {
            listeners.add(listener);
        }
    }

    private class TModel extends AbstractTableModel {
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StudyData d = model.getRow(rowIndex);
            switch(columnIndex) {
                case 0:
                    return d.studyDate;
                case 1:
                    return d.patientName;
                case 2:
                    return d.patientDni;
//                    return model.getCompanyStatusLabel(d.id_companyStatus);
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

    private void editRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            StudyData d = model.getRow(row);

//            CompanyForm form = new CompanyForm(SwingUtilities.getWindowAncestor(this), model.companyStatusMap);
//            form.setData(d.symbol, d.companyName, d.id_companyStatus);
//            form.setVisible(true);
//            if (form.okWasSelected()) {
//                model.updateData(d.id_company, form.getSymbol(), form.getName(), form.getId_companyStatus());
//            }
        }
    }

    private void initComponents() {
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
                StudyData d = model.getRow(row);
                model.deleteData(d.id_study);
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
            public void dataInserted(StudyData data, int row) {
                dataModel.fireTableRowsInserted(row, row);
            }
            @Override
            public void dataUpdated(StudyData data, int row) {
                dataModel.fireTableRowsUpdated(row, row);
            }
            @Override
            public void dataDeleted(StudyData data, int row) {
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
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            System.out.println(e.getClass().getName() + " genrated: " + e.getMessage());
//        }

        SwingUtilities.invokeLater(() -> {
            InfoStudy panel = new InfoStudy();

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
