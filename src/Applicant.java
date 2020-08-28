import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;


public class Applicant {
    //sql
    private static final String url = "jdbc:mysql://localhost:3306/applicant?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String user = "root";
    private static final String password = "atom1105";
    public static Connection connection;
    //информация о текущем пользователе
    public static String curUser;
    public static int id ;
    public static boolean admin = false;
    //window
    public static JFrame frame;
    public static Dimension d = new Dimension();
    public static AuthorizationForm authorizationForm;
    public static MainForm mainForm;
    public static AddForm addForm;
    //VTT information
    public static String[] departments = new String[] {" ", "Отдел IT", "Бухгалтерия"};
    public static String[] orderTypes = new String[] {" ", "Новый план", "Что-то еще"};
    public static String[] statusTypes = new String[] {" ", "Не выполнено", "Выполнено"};

    public static void main(String[] args){
        authorizationForm = new AuthorizationForm();
        frame = new JFrame("Авторизация");
        d.setSize(700, 250);
        frame.setContentPane(authorizationForm.getMainPanel());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(d);
        frame.setResizable(false);
        frame.setVisible(true);
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        //при закрытии окна
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    connection.close();
                } catch (java.sql.SQLException e1){
                    JOptionPane.showMessageDialog(null, e1.getMessage());
                }
            }
        });
    }
}