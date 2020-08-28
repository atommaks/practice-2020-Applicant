import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class AddUserForm {
    private JPanel mainPanel;
    private JTextField userField;
    private JTextField passwordField;
    private JComboBox departmentCombo;
    private JCheckBox adminCheckBox;
    private JButton addBtn;
    private JFrame frame;

    public AddUserForm(JFrame frame, UserTableModel model){
        this.frame = frame;

        //при нажатии кнопки добавить
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    PreparedStatement statement = Applicant.connection.prepareStatement("insert into users (name, department, password, admin) values (?, ?, ?, ?)");
                    statement.setString(1, userField.getText());
                    statement.setString(2, departmentCombo.getSelectedItem().toString());
                    statement.setString(3, passwordField.getText());
                    statement.setBoolean(4, adminCheckBox.isSelected());
                    statement.executeUpdate();
                    statement.close();
                    model.addRow(new Object[] {
                            userField.getText(),
                            departmentCombo.getSelectedItem().toString(),
                            passwordField.getText()
                    });
                    frame.dispose();
                } catch (java.sql.SQLException e1){
                    e1.printStackTrace();
                }
            }
        });
    }

    private void createUIComponents() {
        departmentCombo = new JComboBox(Applicant.departments);
    }

    //getters
    public JPanel getMainPanel() {
        return mainPanel;
    }

}
