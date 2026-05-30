package service;

import model.TodoItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StorageService {

    private static final Path todoFile;
    private static final String STRING_FORMAT = "%s,%s,%s,%s";

    private static final int id_idx = 0;
    private static final int content_idx = 1;
    private static final int deadline_idx = 2;
    private static final int deleted_idx = 3;

    static {
        Path dataDir = getDataDir();
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException("create data dir error: " + dataDir.getFileName());
        }

        todoFile = dataDir.resolve("todo.csv");
    }

    private static Path getDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Paths.get(System.getenv("APPDATA"), "TodoReminder");
        }
        return Paths.get(System.getProperty("user.home"), ".todo-reminder");
    }

    public void saveAll(List<TodoItem> list) {
        List<String> lines = new ArrayList<>();
        lines.add("id,content,deadline,deleted");
        for (TodoItem item : list) {
            lines.add(
                    STRING_FORMAT.formatted(
                            item.id, this.encodeContent(item.content),
                            item.deadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            Boolean.TRUE.equals(item.deleted) ? 1 : 0
                    )
            );
        }
        try {
            Files.write(todoFile, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("保存清单失败");
        }
    }

    public List<TodoItem> readAll() {
        try {
            if (!todoFile.toFile().exists()) {
                return null;
            }
            List<String> allLines = Files.readAllLines(todoFile);
            if (allLines.size() > 1) {
                int[] colIndexes = this.colIdx(allLines.get(0));
                List<TodoItem> items = new ArrayList<>();
                for (int i = 1; i < allLines.size(); i++) {
                    String[] values = allLines.get(i).split(",");
                    boolean deleted = "1".equals(values[colIndexes[deleted_idx]]);
                    if (deleted) continue;
                    TodoItem item = new TodoItem(
                            values[id_idx],
                            this.decodeContent(values[content_idx]),
                            LocalDateTime.parse(values[deadline_idx])
                    );
                    items.add(item);
                }
                return items;
            }
        } catch (IOException e) {
            System.err.println("读取清单失败: " + e.getMessage());
        }
        return null;
    }

    private String decodeContent(String content) {
        return content;
    }

    private String encodeContent(String content) {
        return content;
    }

    private int[] colIdx(String column) {
        String[] cols = column.split(",");
        int[] colIndexes = new int[cols.length];
        for (int i = 0; i < cols.length; i++) {
            String col = cols[i].trim();
            switch (col) {
                case "id" -> colIndexes[id_idx] = i;
                case "content" -> colIndexes[content_idx] = i;
                case "deadline" -> colIndexes[deadline_idx] = i;
                case "deleted" -> colIndexes[deleted_idx] = i;
                default -> throw new RuntimeException("unknown column: " + col);
            }
        }
        return colIndexes;
    }
}
