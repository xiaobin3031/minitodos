package ui;

import model.TodoItem;
import service.TodoService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class FloatingBallWindow extends JWindow {

    private final static int RADIUS = 60;
    private final static Font LEFT_IMPORT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 30);
    private final Timer hideTimer;
    private final TodoService todoService;
    private JPanel detailPanel;
    private JLabel detailLabel;

    public FloatingBallWindow() {
        this.todoService = new TodoService();
        this.hideTimer = new Timer(2000, e -> {
            moveToHide();
        });
        this.hideTimer.setRepeats(false);

        this.build();
    }

    private void build() {
        this.setLayout(new BorderLayout());
        this.setSize(RADIUS, RADIUS);
        this.setAlwaysOnTop(true);
        this.setBackground(new Color(0, 0, 0, 0));

        this.detailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.detailLabel = new JLabel();
        this.detailLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.detailPanel.add(this.detailLabel);

        BallPanel ballPanel = new BallPanel(this.todoService, this);
        this.ballPanelEvent(ballPanel);

        this.add(this.detailPanel, BorderLayout.WEST);
        this.add(ballPanel, BorderLayout.CENTER);

        this.moveToHide();
    }

    public void moveToHide() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screen.width - 10, 20);
    }

    public void moveToShow() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screen.width - 70, 20);
    }

    private boolean isFocusShow(BallPanel ballPanel) {
        int level = ballPanel.getCurrentLevel();
        return level > 0 && level <= 2;
    }

    /**
     * 强制去显示
     */
    private void focusToShow(BallPanel ballPanel) {
        int level = ballPanel.getCurrentLevel();
        if (this.isFocusShow(ballPanel)) {
            // 需要强制显示
            TodoItem todoItem = this.todoService.findOneByLevel(level);
            if (todoItem != null) {
                String content = todoItem.content;
                if (content.length() > 20) {
                    content = content.substring(0, 18) + "...";
                }
                String[] contents = new String[2];
                contents[0] = content.substring(0, Math.min(content.length(), 10));
                if (content.length() > 10) {
                    contents[1] = content.substring(10);
                }
                this.detailLabel.setText("<html>%s<br/>%s</html>".formatted(contents[0], Objects.requireNonNullElse(contents[1], "")));
                Dimension labelSize = this.detailLabel.getPreferredSize();
                Dimension panelSize = new Dimension(labelSize.width + 5, labelSize.height);
                this.detailPanel.setPreferredSize(panelSize);
                this.setSize(panelSize.width + RADIUS, RADIUS);
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                this.setLocation(screen.width - RADIUS - panelSize.width - 10, 20);
            }
        }
    }

    private void ballPanelEvent(BallPanel ballPanel) {
        ballPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (isFocusShow(ballPanel)) return;
                hideTimer.start();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hideTimer.stop();
                if (isFocusShow(ballPanel)) return;
                moveToShow();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showDetail(ballPanel);
            }
        });
    }

    private void showDetail(BallPanel ballPanel) {
        MainWindow mainWindow = MainWindow.getInstance();
        if (!mainWindow.isVisible()) {
            mainWindow.setDeadlineLevel(ballPanel.getCurrentLevel());
            mainWindow.refreshTodos();
            mainWindow.setVisible(true);
        }
    }

    private static class BallPanel extends JPanel {

        private final TodoService todoService;
        private final FloatingBallWindow floatingBallWindow;
        private int currentLevel;

        private BallPanel(TodoService todoService, FloatingBallWindow floatingBallWindow) {
            this.todoService = todoService;
            this.floatingBallWindow = floatingBallWindow;
            setOpaque(false);
        }

        public int getCurrentLevel() {
            return currentLevel;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            // 左边第一个不为0的数，或者第一个为0的
            int[] levelCount = this.todoService.countByDeadline();
            this.currentLevel = levelCount[0];
            Color matchColor = this.levelColor(levelCount[0]);
            g2.setColor(matchColor);
            g2.fillOval(0, 0, RADIUS, RADIUS);

            this.drawLeft(g2, String.valueOf(levelCount[1]));
            this.floatingBallWindow.focusToShow(this);
        }

        private void drawLeft(Graphics2D g2, String count) {
            Font oldFont = g2.getFont();
            g2.setFont(LEFT_IMPORT_FONT);
            FontMetrics fm = g2.getFontMetrics();
            int width = fm.stringWidth(count);
            int baseline = (RADIUS - fm.getHeight()) / 2 + fm.getAscent();
            int x = (RADIUS - width) / 2;
            g2.setColor(Color.WHITE);
            g2.drawString(count, x, baseline);

            g2.setFont(oldFont);
        }

        private Color levelColor(int level) {
            return switch (level) {
                case 1 -> Color.RED;
                case 2 -> Color.ORANGE;
                case 3 -> Color.GREEN;
                default -> Color.GRAY;
            };
        }
    }

    public static void main(String[] args) {
        FloatingBallWindow main = new FloatingBallWindow();
        main.setVisible(true);
    }
}
