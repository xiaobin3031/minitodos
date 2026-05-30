package service;

import model.TodoItem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiPredicate;

public class TodoService {

    private static final StorageService storageService = new StorageService();
    private static final List<TodoItem> todos = new ArrayList<>();
    private static final int[] DEFAULT_LEVEL_COUNT = new int[]{1, 0};
    private static final Map<Integer, BiPredicate<TodoItem, LocalDateTime>> DEADLINE_PREDICATE = new HashMap<>();

    static {
        List<TodoItem> items = storageService.readAll();
        if (items != null) {
            todos.addAll(items);
        }

        DEADLINE_PREDICATE.put(1, (todoItem, now) -> todoItem.deadline.isBefore(now));
        DEADLINE_PREDICATE.put(2, (todoItem, now) -> {
            long duration = Duration.between(now, todoItem.deadline).toMinutes();
            return duration > 0 && duration < 60;
        });
        DEADLINE_PREDICATE.put(3, (todoItem, now) -> {
            long duration = Duration.between(now, todoItem.deadline).toMinutes();
            return duration >= 60 && duration / 60 < 24;
        });
        DEADLINE_PREDICATE.put(4, (todoItem, now) -> {
            long duration = Duration.between(now, todoItem.deadline).toMinutes();
            return duration / 60 >= 24;
        });
    }

    public void add(TodoItem item) {
        todos.add(item);
        sort();
        storageService.saveAll(todos);
    }

    public void remove(String id) {
        todos.removeIf(a -> a.id.equals(id));
        storageService.saveAll(todos);
    }

    public void removeIdx(int idx) {
        todos.remove(idx);
        storageService.saveAll(todos);
    }

    public void update(TodoItem item) {
        this.remove(item.id);
        this.add(item);
        storageService.saveAll(todos);
    }

    public List<TodoItem> list() {
        return todos;
    }

    public List<TodoItem> list(int level) {
        List<TodoItem> list = list();
        if (level > 0) {
            LocalDateTime now = LocalDateTime.now();
            list = list.stream().filter(todoItem -> DEADLINE_PREDICATE.get(level).test(todoItem, now)).toList();
        }
        return list;
    }

    /**
     * 按截止时间统计
     */
    public int[] countByDeadline() {
        int[] counts = new int[]{0, 0, 0, 0};
        List<TodoItem> list = this.list();
        if (!list.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (TodoItem todoItem : list) {
                if (DEADLINE_PREDICATE.get(1).test(todoItem, now)) {
                    // 超期了
                    counts[0]++;
                } else {
                    if (DEADLINE_PREDICATE.get(2).test(todoItem, now)) {
                        // 1小时内到期
                        counts[1]++;
                    } else if (DEADLINE_PREDICATE.get(3).test(todoItem, now)) {
                        // 1天内到期
                        counts[2]++;
                    } else {
                        // 其他
                        counts[3]++;
                    }
                }
            }
        }
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                return new int[]{i + 1, counts[i]};
            }
        }
        return DEFAULT_LEVEL_COUNT;
    }

    private void sort() {
        todos.sort(Comparator.comparing(a -> a.deadline));
    }
}
