package ui;

import model.SpinnerKV;
import model.TodoItem;
import service.TodoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

public class MainWindow extends JFrame {

    private static MainWindow instance;
    private JComboBox<SpinnerKV<Integer>> levelComboBox;

    public static MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }

    private final TodoService todoService;
    private final BiConsumer<TodoItem, Integer> todoChangedConsumer;
    private AddTodoWindow addTodoWindow;
    private JTable todoTable;
    private int deadlineLevel = 0;

    private MainWindow() {
        this.todoService = new TodoService();
        this.todoChangedConsumer = (todoItem, type) -> {
            if (type == 1) {
                this.todoService.add(todoItem);
                // 新增了，自动切换到全部
                this.setDeadlineLevel(0);
            } else if (type == 2) {
                this.todoService.update(todoItem);
            } else {
                return;
            }
            this.refreshTodos();
        };

        this.create();
    }

    public void setDeadlineLevel(int level) {
        this.deadlineLevel = level;
        if (level >= 0 && level <= this.levelComboBox.getItemCount()) {
            this.levelComboBox.setSelectedIndex(level);
        }
    }

    public void create() {
        this.setLayout(new BorderLayout(5, 5));
        this.setLocationRelativeTo(null);
        this.setSize(500, 300);

        JPanel titlePanel = this.getTitlePanel();
        this.add(titlePanel, BorderLayout.NORTH);

        {
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("内容");
            model.addColumn("截止时间");
            this.todoTable = new JTable(model);
            this.todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane tablePanel = new JScrollPane(this.todoTable);
            this.refreshTodos();
            this.add(tablePanel, BorderLayout.CENTER);
        }
    }

    private JPanel getTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout());

        JComboBox<SpinnerKV<Integer>> comboBox = getSpinnerKVJComboBox();
        titlePanel.add(comboBox);

        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> {
            if (this.addTodoWindow == null) {
                this.addTodoWindow = new AddTodoWindow(this.todoChangedConsumer);
            }
            this.addTodoWindow.setVisible(true);
        });
        titlePanel.add(addBtn);

        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> {
            int idx = this.todoTable.getSelectedRow();
            if (idx < 0) {
                return;
            }
            if (this.addTodoWindow == null) {
                this.addTodoWindow = new AddTodoWindow(this.todoChangedConsumer);
            }
            this.addTodoWindow.setTodoItem(this.todoService.list().get(idx));
            this.addTodoWindow.setVisible(true);
        });
        titlePanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> {
            int idx = this.todoTable.getSelectedRow();
            if (idx < 0) {
                return;
            }
            this.todoService.removeIdx(idx);
            DefaultTableModel model = (DefaultTableModel) this.todoTable.getModel();
            model.removeRow(idx);
        });
        titlePanel.add(deleteBtn);
        return titlePanel;
    }

    @SuppressWarnings("unchecked")
    private JComboBox<SpinnerKV<Integer>> getSpinnerKVJComboBox() {
        this.levelComboBox = new JComboBox<>();
        this.levelComboBox.addItem(new SpinnerKV<>(0, "全部"));
        this.levelComboBox.addItem(new SpinnerKV<>(1, "已过期"));
        this.levelComboBox.addItem(new SpinnerKV<>(2, "1小时内到期"));
        this.levelComboBox.addItem(new SpinnerKV<>(3, "1天内到期"));
        this.levelComboBox.addItem(new SpinnerKV<>(4, "其他"));
        this.levelComboBox.addActionListener(e -> {
            SpinnerKV<Integer> item = (SpinnerKV<Integer>) this.levelComboBox.getSelectedItem();
            this.deadlineLevel = item == null ? 0 : item.value();
            this.refreshTodos();
        });
        return this.levelComboBox;
    }

    public void refreshTodos() {
        List<TodoItem> list = this.todoService.list(this.deadlineLevel);
        DefaultTableModel model = (DefaultTableModel) this.todoTable.getModel();
        model.setRowCount(0);
        for (TodoItem todoItem : list) {
            model.addRow(new Object[]{
                    todoItem.content,
                    todoItem.deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            });
        }
    }

    public static void main(String[] args) {
        MainWindow main = new MainWindow();
    }
}
