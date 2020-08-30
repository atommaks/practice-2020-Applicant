import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.Statement;

public class AuthorizationForm {
    private JPanel mainPanel;
    private JComboBox departmentComboBox, userComboBox;
    private JPasswordField passwordField;
    private JButton entrBtn;
    private JLabel resultLabel;

    public AuthorizationForm(){
        //нажатие на кнопку входa
        entrBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Statement statement = Applicant.connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE name = \"" + userComboBox.getSelectedItem() + "\"" + " AND password = \"" + passwordField.getText() + "\"");
                    if ( resultSet.next() && userComboBox.getSelectedItem().equals(resultSet.getString(2)) && passwordField.getText().equals(resultSet.getString(4))) {
                        if (resultSet.getBoolean(5))
                            Applicant.admin = true;
                        Applicant.id = resultSet.getInt("id");
                        Applicant.curUser = userComboBox.getSelectedItem().toString();
                        Applicant.d.setSize(1280, 720);
                        Applicant.frame.setResizable(true);
                        Applicant.frame.setSize(Applicant.d);
                        Applicant.mainForm = new MainForm();
                        Applicant.frame.setContentPane(Applicant.mainForm.getMainPanel());
                        Applicant.mainForm.checkAdmin();
                        Applicant.frame.setTitle("Программа учета заявок в IT");
                    } else {
                        resultLabel.setText("Неверно введены данные");
                    }
                    statement.close();
                    resultSet.close();
                }catch (java.sql.SQLException | java.lang.NullPointerException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });

        //добавление элементов в список
        departmentComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    userComboBox.addItem(" ");
                    Statement statement = Applicant.connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE department = \"" + departmentComboBox.getSelectedItem() + "\"");
                    while (resultSet.next())
                        userComboBox.addItem(resultSet.getString(2));
                    statement.close();
                    resultSet.close();
                } catch(java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });

        //удаление элементов из списка
        departmentComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.DESELECTED)
                    userComboBox.removeAllItems();
            }
        });
    }

    //custom create objects
    private void createUIComponents() {
        departmentComboBox = new JComboBox(Applicant.departments);
        userComboBox = new JComboBox();
    }

    //getters
    public JPanel getMainPanel() {
        return this.mainPanel;
    }
}