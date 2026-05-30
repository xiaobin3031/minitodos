package ui;

import model.TodoItem;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.BiConsumer;

public class AddTodoWindow extends JFrame {

    private final BiConsumer<TodoItem, Integer> onAdd;
    private JTextArea contentArea;
    private JSpinner spinner;
    private TodoItem todoItem;

    public AddTodoWindow(BiConsumer<TodoItem, Integer> onAdd) {
        this.build();
        this.onAdd = onAdd;
    }

    public void setTodoItem(TodoItem todoItem) {
        this.todoItem = todoItem;
        if (todoItem != null) {
            this.contentArea.setText(todoItem.content);
            this.spinner.setValue(Date.from(todoItem.deadline.toInstant(ZoneOffset.ofHours(8))));
        }
    }

    private void build() {
        this.setSize(500, 300);
        this.setLayout(new BorderLayout(5, 5));
        this.setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // 输入内容
        this.contentArea = new JTextArea();
        contentPanel.add(this.contentArea, BorderLayout.CENTER);

        // 截止日期
        SpinnerDateModel model = new SpinnerDateModel();
        this.spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(this.spinner, "yyyy-MM-dd HH:mm");
        this.spinner.setEditor(editor);
        contentPanel.add(this.spinner, BorderLayout.SOUTH);
        this.add(contentPanel, BorderLayout.CENTER);

        // 按钮
        JPanel footPanel = new JPanel();
        footPanel.setLayout(new FlowLayout());
        // 保存按钮
        JButton addBtn = buildOkBtn();
        footPanel.add(addBtn);
        // 取消按钮
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(a -> this.setVisible(false));
        footPanel.add(cancelBtn);
        this.add(footPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private JButton buildOkBtn() {
        JButton addBtn = new JButton("Ok");
        addBtn.addActionListener(a -> {
            String content = this.contentArea.getText();
            if (content == null || content.trim().isEmpty()) return;
            Date date = (Date) this.spinner.getValue();
            if (date == null) return;
            LocalDateTime deadline = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            TodoItem todoItem;
            int type;
            if (this.todoItem == null) {
                todoItem = new TodoItem(content, deadline);
                type = 1;
            } else {
                this.todoItem.content = content;
                this.todoItem.deadline = deadline;
                todoItem = this.todoItem;
                this.todoItem = null;
                type = 2;
            }
            this.setVisible(false);
            this.onAdd.accept(todoItem, type);
        });
        return addBtn;
    }

    public static void main(String[] args) {
        AddTodoWindow main = new AddTodoWindow((a, b) -> {
        });
    }
}
