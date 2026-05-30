package ui;

import service.TodoService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FloatingBallWindow extends JWindow {

    private final static int RADIUS = 60;
    private final static Font LEFT_IMPORT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 30);
    private final Timer hideTimer;
    private JFrame detailFrame;

    public FloatingBallWindow() {
        this.build();
        this.hideTimer = new Timer(2000, e -> {
            moveToHide();
        });
        this.hideTimer.setRepeats(false);
    }

    private void build() {
        this.setSize(RADIUS, RADIUS);
        this.setAlwaysOnTop(true);
        this.setBackground(new Color(0, 0, 0, 0));

        BallPanel ballPanel = new BallPanel();
        this.ballPanelEvent(ballPanel);
        this.setContentPane(ballPanel);

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

    private void ballPanelEvent(BallPanel ballPanel) {
        ballPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hideTimer.start();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hideTimer.stop();
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
        mainWindow.setDeadlineLevel(ballPanel.getCurrentLevel());
        mainWindow.refreshTodos();
        mainWindow.setVisible(true);
    }

    private static class BallPanel extends JPanel {

        private final TodoService todoService;
        private int currentLevel;

        private BallPanel() {
            this.todoService = new TodoService();
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
            g2.fillOval(0, 0, getWidth(), getHeight());

            this.drawLeft(g2, String.valueOf(levelCount[1]));
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
