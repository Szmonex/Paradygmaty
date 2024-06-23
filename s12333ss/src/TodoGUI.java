import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class TodoGUI extends JFrame {
    private TodoApp todoApp;
    private JPanel todoPanel;
    private JTextField newTodoField;
    private JEditorPane editorPane;

    public TodoGUI(TodoApp todoApp) {
        this.todoApp = todoApp;
        setTitle("To-Do List");
        setSize(370, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel do edytowania tekstu HTML
        String customTextHTML = "<html>"
                + "<body contenteditable='true'>" + todoApp.getCustomText() + "</body>"
                + "</html>";

        editorPane = new JEditorPane("text/html", customTextHTML);
        editorPane.setEditable(true);
        add(editorPane, BorderLayout.NORTH);

        todoPanel = new JPanel();
        todoPanel.setLayout(new BoxLayout(todoPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(todoPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel addPanel = new JPanel();
        addPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        newTodoField = new JTextField(20);
        JButton addButton = new JButton("Add");
        JButton deleteAllButton = new JButton("Delete All");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String description = newTodoField.getText();
                if (!description.isEmpty()) {
                    todoApp.addTodoToDatabase(description);
                    newTodoField.setText("");
                    loadTodos();
                }
            }
        });

        deleteAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAllTodos();
            }
        });

        addPanel.add(newTodoField);
        addPanel.add(addButton);
        addPanel.add(deleteAllButton);
        add(addPanel, BorderLayout.SOUTH);

        loadTodos();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                todoApp.setCustomText(editorPane.getText());
            }
        });

        setVisible(true);
    }

    private void loadTodos() {
        new SwingWorker<List<TodoItem>, Void>() {
            @Override
            protected List<TodoItem> doInBackground() throws Exception {
                return todoApp.fetchTodosFromDatabase();
            }

            @Override
            protected void done() {
                try {
                    List<TodoItem> todoList = get();
                    todoPanel.removeAll();

                    for (TodoItem item : todoList) {
                        JPanel itemPanel = createItemPanel(item);
                        todoPanel.add(itemPanel);

                    }

                    todoPanel.revalidate();
                    todoPanel.repaint();

                    System.out.println("Loaded " + todoList.size() + " items into the GUI.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private JPanel createItemPanel(TodoItem item) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(item.getDescription());
        JButton deleteButton = new JButton("Delete");
        JButton completeButton = new JButton("Complete");
        JTextField boxField = new JTextField(item.getBox(), 20);

        if (item.isCompleted()) {
            completeButton.setBackground(Color.GREEN);
        } else {
            completeButton.setBackground(Color.RED);
        }

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                todoApp.deleteTodoFromDatabase(item.getId());
                loadTodos();
            }
        });

        completeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isCompleted = item.isCompleted();
                item.setCompleted(!isCompleted);
                todoApp.markTodoAsCompletedInDatabase(item.getId(), !isCompleted);
                completeButton.setBackground(item.isCompleted() ? Color.GREEN : Color.RED);
            }
        });

        boxField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                todoApp.updateTodoBoxInDatabase(item.getId(), boxField.getText());
            }
        });

        itemPanel.add(descriptionLabel);
        itemPanel.add(deleteButton);
        itemPanel.add(completeButton);
        itemPanel.add(boxField);

        JPanel borderedPanel = new JPanel(new BorderLayout());
        borderedPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        borderedPanel.add(itemPanel, BorderLayout.CENTER);
        borderedPanel.setMaximumSize(new Dimension(350, 70));
        borderedPanel.setPreferredSize(new Dimension(350, 60));
        borderedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return borderedPanel;
    }

    private void deleteAllTodos() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<TodoItem> todoList = todoApp.fetchTodosFromDatabase();
                int numberOfThreads = todoList.size();
                Thread[] threads = new Thread[numberOfThreads];
                for (int i = 0; i < numberOfThreads; i++) {
                    final TodoItem item = todoList.get(i);
                    threads[i] = new Thread(() -> todoApp.deleteTodoFromDatabase(item.getId()));
                    threads[i].start();
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                loadTodos();
                System.out.println("Deleted all items.");
            }
        }.execute();
    }
}
