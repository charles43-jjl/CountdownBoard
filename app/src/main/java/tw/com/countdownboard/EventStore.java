package tw.com.countdownboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class EventStore {
    private static final String PREFS = "countdown_events";
    private static final String KEY = "events";

    private EventStore() {}

    public static final class Event {
        public final String id;
        public final String name;
        public final LocalDate date;
        public final int color;
        public final int repeatMonths;

        public Event(String id, String name, LocalDate date, int color) {
            this(id, name, date, color, 0);
        }

        public Event(String id, String name, LocalDate date, int color, int repeatMonths) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.color = color;
            this.repeatMonths = repeatMonths;
        }

        public boolean isRecurring() {
            return repeatMonths > 0;
        }

        public LocalDate nextDate(LocalDate today) {
            if (!isRecurring() || !date.isBefore(today)) return date;
            LocalDate next = date;
            while (next.isBefore(today)) {
                next = next.plusMonths(repeatMonths);
            }
            return next;
        }
    }

    public static synchronized List<Event> getAll(Context context) {
        ArrayList<Event> result = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                result.add(new Event(
                        obj.optString("id", UUID.randomUUID().toString()),
                        obj.optString("name", "未命名活動"),
                        LocalDate.parse(obj.getString("date")),
                        obj.optInt("color", 0xFF4DD0E1),
                        obj.optInt("repeatMonths", 0)
                ));
            }
        } catch (Exception ignored) {
        }
        LocalDate today = LocalDate.now();
        Collections.sort(result, Comparator.comparing(e -> e.nextDate(today)));
        return result;
    }

    private static synchronized void save(Context context, List<Event> events) {
        JSONArray array = new JSONArray();
        for (Event event : events) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", event.id);
                obj.put("name", event.name);
                obj.put("date", event.date.toString());
                obj.put("color", event.color);
                obj.put("repeatMonths", event.repeatMonths);
                array.put(obj);
            } catch (Exception ignored) {
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, array.toString())
                .apply();
    }

    public static synchronized void add(Context context, String name, LocalDate date, int color) {
        add(context, name, date, color, 0);
    }

    public static synchronized void add(Context context, String name, LocalDate date, int color, int repeatMonths) {
        List<Event> events = getAll(context);
        events.add(new Event(UUID.randomUUID().toString(), name, date, color, repeatMonths));
        save(context, events);
    }

    public static synchronized void delete(Context context, String id) {
        List<Event> events = getAll(context);
        events.removeIf(event -> event.id.equals(id));
        save(context, events);
    }

    public static List<Event> upcoming(Context context) {
        LocalDate today = LocalDate.now();
        ArrayList<Event> result = new ArrayList<>();
        for (Event event : getAll(context)) {
            if (event.isRecurring() || !event.date.isBefore(today)) {
                result.add(event);
            }
        }
        result.sort(Comparator.comparing(e -> e.nextDate(today)));
        return result;
    }
}
