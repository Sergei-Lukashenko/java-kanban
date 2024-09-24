package ru.yandex.kanban.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.kanban.http.DurationAdapter;
import ru.yandex.kanban.http.LocalDateTimeAdapter;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public final class Managers {

    private static TaskManager DEFAULT_MANAGER;
    private static FileBackedTaskManager DEFAULT_FILE_MANAGER;
    private static HistoryManager DEFAULT_HISTORY;

    private Managers() {
        throw new RuntimeException("Utility class Managers cannot be implemented, call Managers.getDefault*() instead");
    }

    public static TaskManager getDefault() {
        if (DEFAULT_MANAGER == null) {
            DEFAULT_MANAGER = new InMemoryTaskManager();
        }
        return DEFAULT_MANAGER;
    }

    public static FileBackedTaskManager getDefaultFileMan(String fileName) {
        if (DEFAULT_FILE_MANAGER == null) {
            DEFAULT_FILE_MANAGER = new FileBackedTaskManager(new File(fileName));
        }
        return DEFAULT_FILE_MANAGER;
    }

    public static HistoryManager getDefaultHistory() {
        if (DEFAULT_HISTORY == null) {
            DEFAULT_HISTORY = new InMemoryHistoryManager();
        }
        return DEFAULT_HISTORY;
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        return gsonBuilder.create();
    }
}
