package tw.com.countdownboard;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CountdownWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    private static class Factory implements RemoteViewsFactory {
        private final Context context;
        private List<EventStore.Event> events = new ArrayList<>();
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        Factory(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate() {
            load();
        }

        @Override
        public void onDataSetChanged() {
            load();
        }

        private void load() {
            events = EventStore.upcoming(context);
        }

        @Override
        public void onDestroy() {
            events = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= events.size()) {
                return null;
            }

            EventStore.Event event = events.get(position);
            LocalDate shownDate = event.nextDate(LocalDate.now());
            long days = ChronoUnit.DAYS.between(LocalDate.now(), shownDate);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_event);
            views.setTextViewText(R.id.event_name, event.name);
            views.setTextViewText(R.id.event_date, shownDate.format(formatter));
            views.setTextViewText(R.id.event_days, days == 0 ? "今天" : days + " 天");
            views.setTextColor(R.id.event_name, event.color);
            views.setTextColor(R.id.event_days, event.color);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("event_id", event.id);
            views.setOnClickFillInIntent(R.id.item_root, fillInIntent);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
