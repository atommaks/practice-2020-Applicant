import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserChangeForm {
    private JPanel mainPanel;
    private JComboBox orderTypeComboBox;
    private JCheckBox axaptaCheckBox;
    private JTextArea orderTextArea;
    private JButton fileBtn, changeBtn, cancelBtn;
    private JLabel fileLbl;
    private File file;
    private Statement statement;

    public UserChangeForm(int order_id) throws java.sql.SQLException{
        statement = Applicant.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from orders where order_id = " + order_id);
        while (resultSet.next())
            orderTextArea.setText(resultSet.getString("order_text"));
        resultSet.close();

        //добавление в sql update список при выборе типа заявки
        orderTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    statement.addBatch("update orders set order_type = \"" + orderTypeComboBox.getSelectedItem() + "\" where order_id = " + order_id);
                } catch (java.sql.SQLException e1){
                    e1.printStackTrace();
                }
            }
        });

        //добавление в sql update список при выборе axapta
        axaptaCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    int b = 0;
                    if (axaptaCheckBox.isSelected())
                        b = 1;
                    statement.addBatch("update orders set axapta = " + b + " where order_id = " + order_id);
                } catch (java.sql.SQLException e1){
                    e1.printStackTrace();
                }
            }
        });

        //выбор файла
        fileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "bmp", "jpeg");
                fileChooser.setFileFilter(filter);
                int ret = fileChooser.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    fileLbl.setText(file.getName());
                }
            }
        });

        //нажатие на кнопку изменить
        changeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    statement.addBatch("update orders set order_text = \"" + orderTextArea.getText() + "\" where order_id = " + order_id);
                    statement.executeBatch();
                    statement.close();
                    Applicant.mainForm.getModel().setValueAt(orderTypeComboBox.getSelectedItem(), Applicant.mainForm.getOrderTable().getSelectedRow(), 3);
                    Applicant.mainForm.getModel().setValueAt(axaptaCheckBox.isSelected(), Applicant.mainForm.getOrderTable().getSelectedRow(), 6);
                    Applicant.mainForm.getOrderInfoTextArea().setText(orderTextArea.getText());
                    Applicant.mainForm.getOrderTable().setEnabled(true);
                    if (!fileLbl.getText().equals("")) {
                        FileInputStream fis = new FileInputStream(file);
                        PreparedStatement ps = Applicant.connection.prepareStatement("update orders set order_file = ? where order_id = " + order_id);
                        ps.setBinaryStream(1, fis, (int) file.length());
                        ps.executeUpdate();
                        ps.close();
                    }
                    Applicant.mainForm.close();
                } catch (SQLException | FileNotFoundException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });

        //нажатие на кнопку отмена
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    statement.clearBatch();
                    statement.close();
                    Applicant.mainForm.getOrderTable().setEnabled(true);
                    Applicant.mainForm.close();
                } catch (java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });
    }

    //custom create objects
    private void createUIComponents() { orderTypeComboBox = new JComboBox(Applicant.orderTypes); }

    //getters
    public JPanel getMainPanel() { return mainPanel; }
    public JComboBox getOrderTypeComboBox() { return orderTypeComboBox; }
    public JCheckBox getAxaptaCheckBox() { return axaptaCheckBox; }
}