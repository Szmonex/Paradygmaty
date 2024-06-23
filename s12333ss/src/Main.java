import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("Biezacy katalog: " + System.getProperty("user.dir"));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TodoApp todoApp = new TodoApp();
                new TodoGUI(todoApp);
            }
        });
    }
}
