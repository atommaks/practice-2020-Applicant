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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddForm {
    private JPanel mainPanel;
    private JComboBox orderTypeComboBox;
    private JCheckBox axaptaCheckBox;
    private JTextArea orderInfoTextArea;
    private JButton cancelBtn, addBtn, fileBtn;
    private JLabel fileLabel;
    private File file;

    public AddForm(){
        //при нажатии кнопки отмена
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Applicant.frame.setEnabled(true);
                Applicant.mainForm.close();
            }
        });

        //при выборе файла
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
                    fileLabel.setText(file.getName());
                }
            }
        });

        //при нажатии кнопки добавить
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String orderType = orderTypeComboBox.getSelectedItem().toString(), orderInfo = orderInfoTextArea.getText();
                java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
                String pattern = "MM/dd/yyyy", user = "";
                DateFormat df = new SimpleDateFormat(pattern);
                int id = -1;
                if (!fileLabel.getText().equals("")){
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        PreparedStatement pre = Applicant.connection.prepareStatement("insert into orders (user_id, order_text, order_file, order_type, axapta, order_date) values(?, ?, ?, ?, ?, ?)");
                        pre.setInt(1, Applicant.id);
                        pre.setString(2, orderInfo);
                        pre.setBinaryStream(3, fis, (int) file.length());
                        pre.setString(4, orderType);
                        pre.setBoolean(5, axaptaCheckBox.isSelected());
                        pre.setDate(6, date);
                        pre.executeUpdate();
                        pre.close();
                        fileLabel.setText("");
                        Applicant.frame.setEnabled(true);
                        Statement statement = Applicant.connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("select LAST_INSERT_ID()");
                        if (resultSet.next())
                            id = resultSet.getInt(1);
                        Applicant.mainForm.getModel().addRow(new Object[]{
                                id,
                                df.format(date),
                                Applicant.curUser,
                                orderType,
                                "Не выполнено",
                                "",
                                axaptaCheckBox.isSelected()
                        });
                        statement.close();
                        resultSet.close();
                        Applicant.mainForm.close();
                    } catch (FileNotFoundException | SQLException e1) {
                        JOptionPane.showMessageDialog(null, e1.getMessage());
                    }
                } else try{
                    PreparedStatement pre = Applicant.connection.prepareStatement("insert into orders (user_id, order_text, order_type, axapta, order_date) values(?, ?, ?, ?, ?)");
                    pre.setInt(1, Applicant.id);
                    pre.setString(2, orderInfo);
                    pre.setString(3, orderType);
                    pre.setBoolean(4, axaptaCheckBox.isSelected());
                    pre.setDate(5, date);
                    pre.executeUpdate();
                    pre.close();
                    Statement statement = Applicant.connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select LAST_INSERT_ID()");
                    if (resultSet.next())
                        id = resultSet.getInt(1);
                    resultSet = statement.executeQuery("select * from orders where order_id = " + id);
                    Applicant.mainForm.getModel().addRow(new Object[]{
                            id,
                            df.format(date),
                            Applicant.curUser,
                            orderType,
                            "Не выполнено",
                            "",
                            axaptaCheckBox.isSelected()
                    });
                    statement.close();
                    resultSet.close();
                    Applicant.frame.setEnabled(true);
                    Applicant.mainForm.close();
                } catch (SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });
    }

    //custom create objects
    private void createUIComponents() {
        orderTypeComboBox = new JComboBox(Applicant.orderTypes);
    }

    //getters
    public JPanel getMainPanel() {
        return this.mainPanel;
    }
}