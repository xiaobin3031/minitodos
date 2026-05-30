package service;

import model.TodoItem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderService {

    private final TodoService todoService = new TodoService();

    public void start() {
        Timer timer = new Timer(true);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                check();
            }
        }, 0, 60_000);
    }

    private void check() {
        if(this.todoService.list().isEmpty()) {
            return;
        }

        TodoItem item = this.todoService.list().get(0);
        long minutes = Duration.between(LocalDateTime.now(), item.deadline).toMinutes();
        //todo 这里补充到期逻辑
    }
}
