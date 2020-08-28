import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDateChooserCellEditor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;

public class MainForm {
    private JPanel mainPanel, topPanel, orderPanel, orderInfoPanel;
    private JButton addBtn, changeBtn, deleteBtn, updateBtn, usersBtn, exitBtn, imageBtn;
    private JTextArea orderInfoTextArea, commentTextArea;
    private javax.swing.JTable orderTable;
    private JLabel executorLbl;
    private JScrollPane scroll;
    private JFrame extFrame;
    private Dimension dimension, screensize;
    private Blob blob;
    private OrderTableModel model;
    JComboBox usersCombo, statusCombo, executorCombo;

    public MainForm(){
        imageBtn.setVisible(false);
        orderInfoTextArea.setEditable(false);
        commentTextArea.setEditable(false);
        scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());

        //при нажатии кнопки добавить
        addBtn.addActionListener(e -> {
            extFrame = new JFrame("Добавление заявки");
            dimension = new Dimension(600, 350);
            screensize = Toolkit.getDefaultToolkit().getScreenSize();
            extFrame.setSize(dimension);
            extFrame.setResizable(false);
            extFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            Applicant.frame.setEnabled(false);
            Applicant.addForm = new AddForm();
            extFrame.setContentPane(Applicant.addForm.getMainPanel());
            extFrame.setUndecorated(true);
            extFrame.setBounds((screensize.width - 500)/2, (screensize.height - 300)/2, 500, 300);
            extFrame.setVisible(true);
        });

        //при просмотре изображения
        imageBtn.addActionListener(e -> {
            try{
                extFrame = new JFrame();
                ImageForm imgF = new ImageForm();
                byte[] content = blob.getBytes(1L, (int)blob.length());
                ImageIcon icon = new ImageIcon(content);
                imgF.getImageLbl().setIcon(icon);
                extFrame.setTitle("Просмотр изображения");
                extFrame.setContentPane(imgF.getMainPanel());
                extFrame.pack();
                extFrame.setVisible(true);
                extFrame.setResizable(true);
            } catch (java.sql.SQLException e1){
                JOptionPane.showMessageDialog(null, e1.getMessage());
            }

        });

        //при нажатии кнопки изменить
        changeBtn.addActionListener(e -> {
            if (Applicant.admin){
                try{
                    extFrame = new JFrame();
                    AdminChangeForm form = new AdminChangeForm(Integer.parseInt(model.getValueAt(orderTable.getSelectedRow(), 0).toString()));
                    form.getExecutorComboBox().setSelectedItem(model.getValueAt(orderTable.getSelectedRow(), 5));
                    form.getStatusComboBox().setSelectedItem(model.getValueAt(orderTable.getSelectedRow(), 4));
                    Statement statement = Applicant.connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select * from orders where order_id = " + model.getValueAt(orderTable.getSelectedRow(), 0));
                    while (resultSet.next())
                        if (resultSet.getString("comment") != null)
                            form.getTextArea1().setText(resultSet.getString("comment"));
                    orderTable.setEnabled(false);
                    extFrame.setTitle("Изменить заявку");
                    extFrame.setContentPane(form.getMainPanel());
                    extFrame.pack();
                    extFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    extFrame.setVisible(true);
                    statement.close();
                    resultSet.close();
                } catch(java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            } else{
                extFrame = new JFrame();
                try {
                    UserChangeForm form = new UserChangeForm(Integer.parseInt(model.getValueAt(orderTable.getSelectedRow(), 0).toString()));
                    form.getOrderTypeComboBox().setSelectedItem(model.getValueAt(orderTable.getSelectedRow(), 3));
                    form.getAxaptaCheckBox().setSelected(new Boolean(model.getValueAt(orderTable.getSelectedRow(), 6).toString()));
                    extFrame.setTitle("Изменить заявку");
                    extFrame.setContentPane(form.getMainPanel());
                    extFrame.pack();
                    orderTable.setEnabled(false);
                    extFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    extFrame.setVisible(true);
                } catch (java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });

        //при нажатии кнопки удалить
        deleteBtn.addActionListener(e -> {
            try {
                Statement statement = Applicant.connection.createStatement();
                statement.executeUpdate("delete from orders where order_id = " + model.getValueAt(orderTable.getSelectedRow(), 0));
                model.deleteRow(orderTable.getSelectedRow());
            } catch (java.sql.SQLException e1){
                JOptionPane.showMessageDialog(null, e1.getMessage());
            }
        });

        //при нажатии кнопки обновить
        updateBtn.addActionListener(e -> {
            model.repaint();
            if (Applicant.admin)
                fillAdminTable();
            else
                fillUserTable();
            scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
        });

        //при нажатии кнопки пользователи
        usersBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extFrame = new JFrame();
                extFrame.setTitle("Пользователи");
                extFrame.setContentPane(new UsersForm().getMainPanel());
                extFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                extFrame.pack();
                extFrame.setVisible(true);
            }
        });

        //при нажатии кнопки выхода
        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Applicant.frame.pack();
                Applicant.id = -1;
                Applicant.curUser = "";
                Applicant.admin = false;
                Applicant.d.setSize(700, 250);
                Applicant.frame.setSize(Applicant.d);
                Applicant.frame.setResizable(false);
                Applicant.mainForm = null;
                Applicant.frame.setVisible(true);
                Applicant.frame.setContentPane(Applicant.authorizationForm.getMainPanel());
            }
        });
    }

    public void close(){ extFrame.dispose(); }

    //кнопка пользователи видна только для админа
    public void checkAdmin(){
        if (Applicant.admin)
            usersBtn.setVisible(true);
        else
            usersBtn.setVisible(false);
    }

    //заполнение таблицы для админа
    private void fillAdminTable(){
        String pattern = "dd/MM/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        try {
            Statement st = Applicant.connection.createStatement();
            ResultSet resultSet = st.executeQuery("select * from orders");
            while (resultSet.next()){
                String user = "", executor = "";
                Statement statement = Applicant.connection.createStatement();
                ResultSet rs = statement.executeQuery("select * from users where id = " + resultSet.getInt(2));
                if (rs.next())
                    user = rs.getString(2);
                rs = statement.executeQuery("select * from users where id = " + resultSet.getInt("executor_id"));
                if (rs.next())
                    executor = rs.getString("name");
                rs.close();
                model.addRow(new Object[] {
                        Integer.toString(resultSet.getInt(1)),
                        df.format(resultSet.getDate(8)),
                        user,
                        resultSet.getString(6),
                        resultSet.getString(5),
                        executor,
                        resultSet.getBoolean(7)
                });
                statement.close();
            }
            st.close();
            resultSet.close();
        } catch (java.sql.SQLException e){
            e.printStackTrace();
        }
    }

    //заполнение таблицы для не админа(обычного пользователя)
    private void fillUserTable(){
        String pattern = "dd/MM/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        try{
            Statement st = Applicant.connection.createStatement();
            ResultSet resultSet = st.executeQuery("select * from orders where user_id = " + Applicant.id);
            while (resultSet.next()){
                String executor = "";
                Statement statement = Applicant.connection.createStatement();
                ResultSet rs = statement.executeQuery("select * from users where id = " + resultSet.getInt("executor_id"));
                if (rs.next())
                    executor = rs.getString("name");
                statement.close();
                rs.close();
                model.addRow(new Object[] {
                        Integer.toString(resultSet.getInt(1)),
                        df.format(resultSet.getDate(8)),
                        Applicant.curUser,
                        resultSet.getString(6),
                        resultSet.getString(5),
                        executor,
                        resultSet.getBoolean(7)
                });
            }
            st.close();
            resultSet.close();
        } catch (java.sql.SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void updateTable(String user, String status, String executor, Date date){
        model.repaint();
        String pattern = "dd/MM/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        String query = "select * from orders where";
        boolean add = false, u = false, s = false, e = false, d = false;
        int userId = -1;
        int executorId = -1;
        java.sql.Date date1 = null;
        if (date != null) {
            date1 = new java.sql.Date(date.getTime());
        }

        try{
            if (!user.equals(" ")){
                u = true;
                Statement statement = Applicant.connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from users where name = \"" + user + "\"");
                while (resultSet.next()) {
                    userId = resultSet.getInt("id");
                    if (!add) {
                        query += (" user_id = " + userId);
                        add = true;
                    } else{
                        query += (" and user_id = " + userId);
                    }
                }
                statement.close();
                resultSet.close();
            }
            if (!Applicant.admin) {
                u = true;
                Statement statement = Applicant.connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from users where name = \"" + Applicant.curUser + "\"");
                while (resultSet.next()) {
                    userId = resultSet.getInt("id");
                    if (!add) {
                        query += (" user_id = " + userId);
                        add = true;
                    } else{
                        query += (" and user_id = " + userId);
                    }
                }
                statement.close();
                resultSet.close();
            }
            if (!executor.equals(" ")){
                e = true;
                Statement statement = Applicant.connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from users where name = \"" + executor + "\"");
                while (resultSet.next()) {
                    executorId = resultSet.getInt("id");
                    if (!add) {
                        query += (" executor_id = " + executorId);
                        add = true;
                    } else{
                        query += (" and executor_id = " + executorId);
                    }
                }
                statement.close();
                resultSet.close();
            }
            if (!status.equals(" ")){
                s = true;
                if (!add) {
                    query += (" order_status = \"" + status + "\"");
                    add = true;
                } else{
                    query += (" and order_status = \"" + status + "\"");
                }
            }
            if (date != null){
                d = true;
                if (!add) {
                    query += (" order_date = \"" + date1 + "\"");
                    add = true;
                } else{
                    query += (" and order_date = \"" + date1 + "\"");
                }
            }
            if (u || s || e || d){
                Statement statement = Applicant.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()){
                    String executorName = "";
                    String userName = "";
                    Statement st = Applicant.connection.createStatement();
                    ResultSet rs = st.executeQuery("select * from users where id = " + resultSet.getInt("executor_id"));
                    if (rs.next())
                        executorName = rs.getString("name");
                    if (resultSet.getString("user_id") != null){
                        rs = st.executeQuery("select * from users where id = " + resultSet.getInt("user_id"));
                        if (rs.next())
                            userName = rs.getString("name");
                    }
                    st.close();
                    rs.close();
                    model.addRow(new Object[] {
                            Integer.toString(resultSet.getInt(1)),
                            df.format(resultSet.getDate(8)),
                            userName,
                            resultSet.getString(6),
                            resultSet.getString(5),
                            executorName,
                            resultSet.getBoolean(7)
                    });
                }
                statement.close();
                resultSet.close();
            } else if (!u && !s && !e && !d){
                if (Applicant.admin)
                    fillAdminTable();
                else
                    fillUserTable();
            }

        } catch (java.sql.SQLException e1){
            e1.printStackTrace();
        }
    }

    //custom create objects
    private void createUIComponents(){
        //таблица
        model = new OrderTableModel();
        orderTable = new JTable(model);
        orderTable.setDragEnabled(false);
        orderTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setTableHeader(new EditableHeader(orderTable.getColumnModel()));
        ListSelectionModel lsModel = orderTable.getSelectionModel();
        if (Applicant.admin)
            fillAdminTable();
        else
            fillUserTable();

        executorCombo = new JComboBox();
        usersCombo = new JComboBox();
        statusCombo = new JComboBox(Applicant.statusTypes);

        try{
            usersCombo.addItem(" ");
            Statement statement = Applicant.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from users");
            while (resultSet.next())
                usersCombo.addItem(resultSet.getString("name"));
            statement.close();
            resultSet.close();
        } catch (java.sql.SQLException e){
            e.printStackTrace();
        }

        try{
            executorCombo.addItem(" ");
            Statement statement = Applicant.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from users where admin = 1");
            while (resultSet.next())
                executorCombo.addItem(resultSet.getString("name"));
            statement.close();
            resultSet.close();
        } catch (java.sql.SQLException e){
            e.printStackTrace();
        }

        DefaultCellEditor userEditor = new DefaultCellEditor(usersCombo), statusEditor = new DefaultCellEditor(statusCombo), executorEditor = new DefaultCellEditor(executorCombo);
        //1-ый столбец
        TableColumn idTc = orderTable.getColumnModel().getColumn(0);
        idTc.setCellRenderer(new MyTableCellRenderer());

        //2-ой столбец
        JDateChooser dateChooser = new JDateChooser();
        EditableHeaderTableColumn dateTc = (EditableHeaderTableColumn)orderTable.getColumnModel().getColumn(1);
        dateTc.setCellRenderer(new MyTableCellRenderer());
        dateTc.setHeaderEditor(new JMyDateChooserCellEditor(dateChooser));
        dateTc.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateTable(usersCombo.getSelectedItem().toString(), statusCombo.getSelectedItem().toString(), executorCombo.getSelectedItem().toString(), dateChooser.getDate());
                scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
            }
        });

        //3-ий столбец
        ComboBoxRenderer userRenderer = new ComboBoxRenderer(usersCombo);
        if (Applicant.admin){
            EditableHeaderTableColumn userTc = (EditableHeaderTableColumn)orderTable.getColumnModel().getColumn(2);
            userTc.setHeaderRenderer(userRenderer);
            userTc.setHeaderEditor(userEditor);
            userTc.setCellRenderer(new MyTableCellRenderer());
            userTc.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateTable(usersCombo.getSelectedItem().toString(), statusCombo.getSelectedItem().toString(), executorCombo.getSelectedItem().toString(), dateChooser.getDate());
                    scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
                }
            });
        } else{
            TableColumn userTc = orderTable.getColumnModel().getColumn(2);
            userTc.setCellRenderer(new MyTableCellRenderer());
        }

        //4-ый столбец
        TableColumn planTc = orderTable.getColumnModel().getColumn(3);
        planTc.setCellRenderer(new MyTableCellRenderer());

        //5-ый столбец
        ComboBoxRenderer statusRenderer = new ComboBoxRenderer(statusCombo);
        EditableHeaderTableColumn statusTc = (EditableHeaderTableColumn)orderTable.getColumnModel().getColumn(4);
        statusTc.setHeaderRenderer(statusRenderer);
        statusTc.setHeaderEditor(statusEditor);
        statusTc.setCellRenderer(new MyTableCellRenderer());
        statusTc.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (Applicant.admin) {
                    updateTable(usersCombo.getSelectedItem().toString(), statusCombo.getSelectedItem().toString(), executorCombo.getSelectedItem().toString(), dateChooser.getDate());
                    scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
                } else {
                    updateTable(Applicant.curUser, statusCombo.getSelectedItem().toString(), executorCombo.getSelectedItem().toString(), dateChooser.getDate());
                    scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
                }
            }
        });

        //6-ый столбец
        ComboBoxRenderer executorRenderer = new ComboBoxRenderer(executorCombo);
        EditableHeaderTableColumn executorTc = (EditableHeaderTableColumn)orderTable.getColumnModel().getColumn(5);
        executorTc.setHeaderRenderer(executorRenderer);
        executorTc.setHeaderEditor(executorEditor);
        executorTc.setCellRenderer(new MyTableCellRenderer());
        executorTc.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (Applicant.admin) {
                    updateTable(usersCombo.getSelectedItem().toString(), statusCombo.getSelectedItem().toString(), executorCombo.getSelectedItem().toString(), dateChooser.getDate());
                    scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
                } else {
                    updateTable(Applicant.curUser, statusCombo.getSelectedItem().toString(), executorCombo.getSelectedItem().toString(), dateChooser.getDate());
                    scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
                }
            }
        });

        //7-ый столбец
        TableColumn axaptaTc = orderTable.getColumnModel().getColumn(6);
        axaptaTc.setCellRenderer(new CheckBoxRenderer());

        //при выборе строки в таблице
        lsModel.addListSelectionListener(e -> {
            if (!lsModel.isSelectionEmpty()){
                try {
                    int a = Integer.parseInt(model.getValueAt(orderTable.getSelectedRow(), 0).toString());
                    Statement statement = Applicant.connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select * from orders where order_id = " + a);
                    while (resultSet.next()){
                        orderInfoTextArea.setText(resultSet.getString("order_text"));
                        if (resultSet.getString("comment") != null)
                            commentTextArea.setText(resultSet.getString("comment"));
                        else
                            commentTextArea.setText("");
                        if (resultSet.getString("executor_id") != null) {
                            Statement st = Applicant.connection.createStatement();
                            ResultSet rs = st.executeQuery("select * from users where id = " + resultSet.getInt("executor_id"));
                            while (rs.next())
                                executorLbl.setText(rs.getString("name"));
                            st.close();
                            rs.close();
                        }
                        if (resultSet.getBlob("order_file") != null) {
                            blob = resultSet.getBlob("order_file");
                            imageBtn.setVisible(true);
                        } else{
                            imageBtn.setVisible(false);
                        }
                    }
                    statement.close();
                    resultSet.close();
                } catch (java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });
    }

    class UsersForm{
        private JPanel mainPanel;
        private JButton addBtn;
        private JButton deleteBtn;
        private JScrollPane scroll;
        private JTable userTable;
        private UserTableModel userTableModel;

        public UsersForm() {

            //при нажатии кнопки удалить
            deleteBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        int user_id;
                        Statement statement = Applicant.connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("select * from users where name = \"" + userTable.getValueAt(userTable.getSelectedRow(), 0) + "\"");
                        if (resultSet.next()) {
                            user_id = resultSet.getInt("id");
                            statement.executeUpdate("delete from orders where user_id = " + user_id);
                            statement.executeUpdate("delete from users where id = " + user_id);
                            userTableModel.deleteRow(userTable.getSelectedRow());
                        }
                        statement.close();
                        resultSet.close();
                    } catch (java.sql.SQLException e1){
                        e1.printStackTrace();
                    }
                }
            });

            //при нажатии кнопи добавления пользователя
            addBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFrame f = new JFrame("Добавление нового пользователя");
                    f.setContentPane(new AddUserForm(f, userTableModel).getMainPanel());
                    f.pack();
                    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    f.setVisible(true);
                }
            });
        }

        //custom-create objects
        private void createUIComponents() {
            userTableModel = new UserTableModel();
            userTable = new JTable(userTableModel);
            userTable.setDragEnabled(false);
            userTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            userTable.setTableHeader(new EditableHeader(userTable.getColumnModel()));
            JComboBox departmentCombo = new JComboBox(Applicant.departments);
            DefaultCellEditor userEditor = new DefaultCellEditor(usersCombo), departmentEditor  = new DefaultCellEditor(departmentCombo);

//            //1-ая колонка
            ComboBoxRenderer userRenderer = new ComboBoxRenderer(usersCombo);
            EditableHeaderTableColumn userTc = (EditableHeaderTableColumn)userTable.getColumnModel().getColumn(0);
            userTc.setHeaderRenderer(userRenderer);
            userTc.setHeaderEditor(userEditor);
            userTc.setCellRenderer(new DefaultTableCellRenderer());
            userTc.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateUserTable(usersCombo.getSelectedItem().toString(), departmentCombo.getSelectedItem().toString());
                }
            });
            //2-ая колонка
            ComboBoxRenderer departmentRenderer = new ComboBoxRenderer(departmentCombo);
            EditableHeaderTableColumn departmentTc = (EditableHeaderTableColumn)userTable.getColumnModel().getColumn(1);
            departmentTc.setCellRenderer(new DefaultTableCellRenderer());
            departmentTc.setHeaderRenderer(departmentRenderer);
            departmentTc.setHeaderEditor(departmentEditor);
            departmentTc.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateUserTable(usersCombo.getSelectedItem().toString(), departmentCombo.getSelectedItem().toString());
                }
            });

        }

        private void updateUserTable(String user, String department){
            userTableModel.repaint();
            String query = "select * from users where";
            boolean u = false, d = false, add = false;
            if (!user.equals(" ")){
                u = true;
                if (!add){
                    add = true;
                    query += (" name = \"" + user + "\"");
                } else{
                    query += (" and name = \"" + user + "\"");
                }
            }
            if (!department.equals(" ")){
                d = true;
                if (!add){
                    add = true;
                    query += (" department = \"" + department + "\"");
                } else{
                    query += (" and department = \"" + department + "\"");
                }
            }
            if (u || d) {
                try {
                    Statement statement = Applicant.connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);
                    while (resultSet.next()){
                        userTableModel.addRow(new Object[] {
                                resultSet.getString("name"),
                                resultSet.getString("department"),
                                resultSet.getString("password")
                        });
                    }
                    statement.close();
                    resultSet.close();
                } catch (java.sql.SQLException e){
                    e.printStackTrace();
                }
            } else if (!u && !d){
                fillTable();
            }
        }

        private void fillTable(){
            try {
                Statement statement = Applicant.connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from users");
                while (resultSet.next()){
                    userTableModel.addRow(new Object[]{resultSet.getString("name"), resultSet.getString("department"), resultSet.getString("password")});
                }
            } catch (java.sql.SQLException e1){
                e1.printStackTrace();
            }

        }

        //getters
        public JPanel getMainPanel() { return mainPanel; }
    }

    //getters
    public JTable getOrderTable() { return orderTable; }
    public OrderTableModel getModel() { return model; }
    public JTextArea getOrderInfoTextArea() { return orderInfoTextArea; }
    public JPanel getMainPanel() { return this.mainPanel; }
    public JTextArea getCommentTextArea() { return commentTextArea; }
}

class JMyDateChooserCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JDateChooser editorComponent;
    private final EventListenerList listeners;

    public JMyDateChooserCellEditor(JDateChooser chooser){
        this.editorComponent = chooser;
        listeners = new EventListenerList();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.editorComponent.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.editorComponent.removePropertyChangeListener(listener);
    }

    @Override
    public Component getTableCellEditorComponent(JTable jTable, Object o, boolean b, int i, int i1) {
        Date date = null;
        this.editorComponent.setDateFormatString("dd//MM//yyyy");
        if (o instanceof Date) {
            date = (Date) o;
        }
        this.editorComponent.setDate(date);
        return this.editorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        DateFormat df = new SimpleDateFormat("dd//MM//yyyy");
        return df.format(editorComponent.getDate());
    }
}


class MyTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(null);
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        //setText(String.valueOf(value));
        if (isSelected){
            setBackground(Color.BLUE);
        } else if (table.getValueAt(row, 4).equals("Выполнено")){
            setBackground(new Color(90, 255, 170));
            this.setForeground(Color.BLACK);
        } else {
            setBackground(new Color(255, 130, 132));
            this.setForeground(Color.BLACK);
        }
        return this;
    }
}

class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
    CheckBoxRenderer() {
        setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(null);
        if (isSelected){
            setBackground(Color.BLUE);
        } else if (table.getValueAt(row, 4).equals("Выполнено")){
            setBackground(new Color(90, 255, 170));
            this.setForeground(Color.BLACK);
        } else{
            setBackground(new Color(255, 130, 132));
            this.setForeground(Color.BLACK);
        }
        setSelected((Boolean) value);
        return this;
    }
}

class ComboBoxRenderer extends JComboBox implements TableCellRenderer {

    public ComboBoxRenderer(JComboBox box){
        for (int i = 0; i < box.getItemCount(); i++)
            addItem(box.getItemAt(i));
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setSelectedItem(value);
        return this;
    }
}

class EditableHeaderUI extends BasicTableHeaderUI {

    protected MouseInputListener createMouseInputListener() {
        return new MouseInputHandler((EditableHeader) header);
    }

    public class MouseInputHandler extends BasicTableHeaderUI.MouseInputHandler {
        private Component dispatchComponent;

        protected EditableHeader header;

        public MouseInputHandler(EditableHeader header) {
            this.header = header;
        }

        private void setDispatchComponent(MouseEvent e) {
            Component editorComponent = header.getEditorComponent();
            Point p = e.getPoint();
            Point p2 = SwingUtilities.convertPoint(header, p, editorComponent);
            dispatchComponent = SwingUtilities.getDeepestComponentAt(
                    editorComponent, p2.x, p2.y);
        }

        private boolean repostEvent(MouseEvent e) {
            if (dispatchComponent == null) {
                return false;
            }
            MouseEvent e2 = SwingUtilities.convertMouseEvent(header, e,
                    dispatchComponent);
            dispatchComponent.dispatchEvent(e2);
            return true;
        }

        public void mousePressed(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            super.mousePressed(e);

            if (header.getResizingColumn() == null) {
                Point p = e.getPoint();
                TableColumnModel columnModel = header.getColumnModel();
                int index = columnModel.getColumnIndexAtX(p.x);
                if (index != -1) {
                    if (header.editCellAt(index, e)) {
                        setDispatchComponent(e);
                        repostEvent(e);
                    }
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            repostEvent(e);
            dispatchComponent = null;
        }

    }

}

class EditableHeader extends JTableHeader implements CellEditorListener {
    public final int HEADER_ROW = -10;

    transient protected int editingColumn;

    transient protected TableCellEditor cellEditor;

    transient protected Component editorComp;

    public EditableHeader(TableColumnModel columnModel) {
        super(columnModel);
        setReorderingAllowed(false);
        cellEditor = null;
        recreateTableColumn(columnModel);
    }

    public void updateUI() {
        setUI(new EditableHeaderUI());
        resizeAndRepaint();
        invalidate();
    }

    protected void recreateTableColumn(TableColumnModel columnModel) {
        int n = columnModel.getColumnCount();
        EditableHeaderTableColumn[] newCols = new EditableHeaderTableColumn[n];
        TableColumn[] oldCols = new TableColumn[n];
        for (int i = 0; i < n; i++) {
            oldCols[i] = columnModel.getColumn(i);
            newCols[i] = new EditableHeaderTableColumn();
            newCols[i].copyValues(oldCols[i]);
        }
        for (int i = 0; i < n; i++) {
            columnModel.removeColumn(oldCols[i]);
        }
        for (int i = 0; i < n; i++) {
            columnModel.addColumn(newCols[i]);
        }
    }

    public boolean editCellAt(int index) {
        return editCellAt(index);
    }

    public boolean editCellAt(int index, EventObject e) {
        if (cellEditor != null && !cellEditor.stopCellEditing())
            return false;
        if (!isCellEditable(index))
            return false;
        TableCellEditor editor = getCellEditor(index);

        if (editor != null && editor.isCellEditable(e)) {
            editorComp = prepareEditor(editor, index);
            editorComp.setBounds(getHeaderRect(index));
            add(editorComp);
            editorComp.validate();
            setCellEditor(editor);
            setEditingColumn(index);
            editor.addCellEditorListener(this);
            return true;
        }
        return false;
    }

    public boolean isCellEditable(int index) {
        if (getReorderingAllowed())
            return false;
        int columnIndex = columnModel.getColumn(index).getModelIndex();
        EditableHeaderTableColumn col = (EditableHeaderTableColumn) columnModel
                .getColumn(columnIndex);
        return col.isHeaderEditable();
    }

    public TableCellEditor getCellEditor(int index) {
        int columnIndex = columnModel.getColumn(index).getModelIndex();
        EditableHeaderTableColumn col = (EditableHeaderTableColumn) columnModel
                .getColumn(columnIndex);
        return col.getHeaderEditor();
    }

    public void setCellEditor(TableCellEditor newEditor) {
        TableCellEditor oldEditor = cellEditor;
        cellEditor = newEditor;

        // firePropertyChange

        if (oldEditor != null && oldEditor instanceof TableCellEditor) {
            ((TableCellEditor) oldEditor)
                    .removeCellEditorListener((CellEditorListener) this);
        }
        if (newEditor != null && newEditor instanceof TableCellEditor) {
            ((TableCellEditor) newEditor)
                    .addCellEditorListener((CellEditorListener) this);
        }
    }

    public Component prepareEditor(TableCellEditor editor, int index) {
        Object value = columnModel.getColumn(index).getHeaderValue();
        boolean isSelected = true;
        int row = HEADER_ROW;
        JTable table = getTable();
        Component comp = editor.getTableCellEditorComponent(table, value,
                isSelected, row, index);
        if (comp instanceof JComponent) {
            ((JComponent) comp).setNextFocusableComponent(this);
        }
        return comp;
    }

    public TableCellEditor getCellEditor() {
        return cellEditor;
    }

    public Component getEditorComponent() {
        return editorComp;
    }

    public void setEditingColumn(int aColumn) {
        editingColumn = aColumn;
    }

    public int getEditingColumn() {
        return editingColumn;
    }

    public void removeEditor() {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            editor.removeCellEditorListener(this);

            requestFocus();
            remove(editorComp);

            int index = getEditingColumn();
            Rectangle cellRect = getHeaderRect(index);

            setCellEditor(null);
            setEditingColumn(-1);
            editorComp = null;

            repaint(cellRect);
        }
    }

    public boolean isEditing() {
        return (cellEditor == null) ? false : true;
    }

    //
    // CellEditorListener
    //
    public void editingStopped(ChangeEvent e) {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            Object value = editor.getCellEditorValue();
            int index = getEditingColumn();
            columnModel.getColumn(index).setHeaderValue(value);
            removeEditor();
        }
    }

    public void editingCanceled(ChangeEvent e) {
        removeEditor();
    }

    //
    // public void setReorderingAllowed(boolean b) {
    //   reorderingAllowed = false;
    // }

}

class EditableHeaderTableColumn extends TableColumn {

    protected TableCellEditor headerEditor;

    protected boolean isHeaderEditable;

    public EditableHeaderTableColumn() {
        setHeaderEditor(createDefaultHeaderEditor());
        isHeaderEditable = true;
    }

    public void setHeaderEditor(TableCellEditor headerEditor) {
        this.headerEditor = headerEditor;
    }

    public TableCellEditor getHeaderEditor() {
        return headerEditor;
    }

    public void setHeaderEditable(boolean isEditable) {
        isHeaderEditable = isEditable;
    }

    public boolean isHeaderEditable() {
        return isHeaderEditable;
    }

    public void copyValues(TableColumn base) {
        modelIndex = base.getModelIndex();
        identifier = base.getIdentifier();
        width = base.getWidth();
        minWidth = base.getMinWidth();
        setPreferredWidth(base.getPreferredWidth());
        maxWidth = base.getMaxWidth();
        headerRenderer = base.getHeaderRenderer();
        headerValue = base.getHeaderValue();
        cellRenderer = base.getCellRenderer();
        cellEditor = base.getCellEditor();
        isResizable = base.getResizable();
    }

    protected TableCellEditor createDefaultHeaderEditor() {
        return new DefaultCellEditor(new JTextField());
    }
}