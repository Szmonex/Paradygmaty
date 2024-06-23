import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import javax.swing.*;

@Aspect
public class LogAspect {
    @Before("execution(* TodoGUI.loadTodos(..))")
    public void requireLogin() {
        String password = TodoApp.getPassword();
        String input = JOptionPane.showInputDialog("Haslo:");
        if (!password.equals(input)) {
            JOptionPane.showMessageDialog(null, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
