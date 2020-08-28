import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminChangeForm {
    private JPanel mainPanel;
    private JComboBox executorComboBox, statusComboBox;
    private JTextArea textArea1;
    private JButton changeBtn, cancelBtn;
    private Statement statement;

    public AdminChangeForm(int order_id) throws java.sql.SQLException{
        statement = Applicant.connection.createStatement();

        //при выборе исполнителя
        executorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Statement st = Applicant.connection.createStatement();
                    ResultSet rs = st.executeQuery("select * from users where name = \"" + executorComboBox.getSelectedItem() + "\"");
                    while (rs.next())
                        statement.addBatch("update orders set executor_id = " + rs.getInt("id") + " where order_id = " + order_id);
                    st.close();
                    rs.close();
                } catch (java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });

        //при выборе статуса
        statusComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    statement.addBatch("update orders set order_status = \"" + statusComboBox.getSelectedItem() + "\" where order_id = " + order_id);
                } catch (java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });

        //при нажатии кнопки изменить
        changeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    statement.addBatch("update orders set comment = \"" + textArea1.getText() + "\" where order_id = " + order_id);
                    statement.executeBatch();
                    statement.close();
                    Applicant.mainForm.getOrderTable().setEnabled(true);
                    Applicant.mainForm.getOrderTable().setValueAt(statusComboBox.getSelectedItem(), Applicant.mainForm.getOrderTable().getSelectedRow(), 4);
                    Applicant.mainForm.getOrderTable().setValueAt(executorComboBox.getSelectedItem(), Applicant.mainForm.getOrderTable().getSelectedRow(), 5);
                    Applicant.mainForm.getCommentTextArea().setText(textArea1.getText());
                    Applicant.mainForm.close();
                } catch (java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });

        //при нажатии кнопки отмена
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (statement.executeBatch().length != 0)
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
    private void createUIComponents() throws java.sql.SQLException{
        executorComboBox = new JComboBox();
        statusComboBox = new JComboBox(Applicant.statusTypes);
        Statement st = Applicant.connection.createStatement();
        ResultSet resultSet = st.executeQuery("select * from users where admin = 1");
        while (resultSet.next()){
            Statement statement = Applicant.connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from users where id = " + resultSet.getInt("id"));
            while (rs.next())
                executorComboBox.addItem(rs.getString("name"));
            statement.close();
            rs.close();
        }
        st.close();
        resultSet.close();
    }

    //getters
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JComboBox getExecutorComboBox() {
        return executorComboBox;
    }

    public JComboBox getStatusComboBox() {
        return statusComboBox;
    }

    public JTextArea getTextArea1() {
        return textArea1;
    }
}