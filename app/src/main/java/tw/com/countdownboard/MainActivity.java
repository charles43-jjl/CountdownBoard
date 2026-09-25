package tw.com.countdownboard;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MainActivity extends Activity {

    private final int[] colors = new int[] {
            Color.parseColor("#4DD0E1"),
            Color.parseColor("#5EA8FF"),
            Color.parseColor("#FFB74D"),
            Color.parseColor("#B388FF"),
            Color.parseColor("#66BB6A"),
            Color.parseColor("#F06292"),
            Color.parseColor("#EF5350"),
            Color.parseColor("#26C6DA")
    };

    private EditText nameInput;
    private Button dateButton;
    private LinearLayout palette;
    private LinearLayout eventList;
    private Spinner repeatSpinner;
    private LocalDate selectedDate;
    private int selectedColor;
    private final DateTimeFormatter displayDate = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedDate = LocalDate.now().plusDays(7);
        selectedColor = colors[0];

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0B0F14"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView title = text("活動倒數", 28, Color.WHITE, true);
        root.addView(title);

        TextView subtitle = text("一次看多個活動，桌面小工具可上下滑動。", 14,
                Color.parseColor("#9AA7B5"), false);
        LinearLayout.LayoutParams subtitleParams = wrap();
        subtitleParams.topMargin = dp(6);
        root.addView(subtitle, subtitleParams);

        nameInput = new EditText(this);
        nameInput.setHint("活動名稱");
        nameInput.setTextColor(Color.WHITE);
        nameInput.setHintTextColor(Color.parseColor("#758291"));
        nameInput.setSingleLine(true);
        nameInput.setPadding(dp(14), 0, dp(14), 0);
        nameInput.setBackground(rounded(Color.parseColor("#171E27"), 14));
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        inputParams.topMargin = dp(24);
        root.addView(nameInput, inputParams);

        dateButton = new Button(this);
        updateDateButton();
        dateButton.setTextColor(Color.WHITE);
        dateButton.setTextSize(16);
        dateButton.setAllCaps(false);
        dateButton.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        dateButton.setPadding(dp(14), 0, dp(14), 0);
        dateButton.setBackground(rounded(Color.parseColor("#171E27"), 14));
        dateButton.setOnClickListener(v -> openDatePicker());
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        dateParams.topMargin = dp(12);
        root.addView(dateButton, dateParams);

        TextView repeatLabel = text("重複", 14, Color.parseColor("#C6CFD9"), true);
        LinearLayout.LayoutParams repeatLabelParams = wrap();
        repeatLabelParams.topMargin = dp(18);
        root.addView(repeatLabel, repeatLabelParams);

        repeatSpinner = new Spinner(this);
        String[] repeatOptions = {"不重複", "每月", "每 3 個月", "每 6 個月", "每年"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, repeatOptions);
        repeatSpinner.setAdapter(repeatAdapter);
        LinearLayout.LayoutParams repeatParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        repeatParams.topMargin = dp(6);
        root.addView(repeatSpinner, repeatParams);

        TextView colorLabel = text("顏色", 14, Color.parseColor("#C6CFD9"), true);
        LinearLayout.LayoutParams labelParams = wrap();
        labelParams.topMargin = dp(18);
        root.addView(colorLabel, labelParams);

        HorizontalScrollView colorScroll = new HorizontalScrollView(this);
        colorScroll.setHorizontalScrollBarEnabled(false);
        palette = new LinearLayout(this);
        palette.setOrientation(LinearLayout.HORIZONTAL);
        palette.setPadding(0, dp(10), 0, dp(6));
        colorScroll.addView(palette);
        root.addView(colorScroll);
        renderPalette();

        Button addButton = new Button(this);
        addButton.setText("＋ 新增活動");
        addButton.setTextColor(Color.WHITE);
        addButton.setTextSize(16);
        addButton.setAllCaps(false);
        addButton.setBackground(rounded(Color.parseColor("#2F80ED"), 16));
        addButton.setOnClickListener(v -> addEvent());
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        addParams.topMargin = dp(8);
        root.addView(addButton, addParams);

        TextView savedLabel = text("已加入的活動", 18, Color.WHITE, true);
        LinearLayout.LayoutParams savedParams = wrap();
        savedParams.topMargin = dp(28);
        root.addView(savedLabel, savedParams);

        eventList = new LinearLayout(this);
        eventList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        listParams.topMargin = dp(8);
        root.addView(eventList, listParams);

        setContentView(scroll);
        renderEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventList != null) {
            renderEvents();
        }
    }

    private void openDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedDate = LocalDate.of(year, month + 1, day);
                    updateDateButton();
                },
                selectedDate.getYear(),
                selectedDate.getMonthValue() - 1,
                selectedDate.getDayOfMonth()
        );
        dialog.show();
    }

    private void updateDateButton() {
        dateButton.setText("日期　" + selectedDate.format(displayDate));
    }

    private void renderPalette() {
        palette.removeAllViews();
        for (int color : colors) {
            TextView swatch = new TextView(this);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            if (color == selectedColor) {
                bg.setStroke(dp(3), Color.WHITE);
                swatch.setText("✓");
                swatch.setTextColor(Color.WHITE);
                swatch.setTextSize(18);
                swatch.setGravity(Gravity.CENTER);
            }
            swatch.setBackground(bg);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(44), dp(44));
            p.rightMargin = dp(10);
            palette.addView(swatch, p);
            swatch.setOnClickListener(v -> {
                selectedColor = color;
                renderPalette();
            });
        }
    }

    private void addEvent() {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "請輸入活動名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        int[] repeatMonths = {0, 1, 3, 6, 12};
        int repeat = repeatMonths[repeatSpinner.getSelectedItemPosition()];
        EventStore.add(this, name, selectedDate, selectedColor, repeat);
        nameInput.setText("");
        selectedDate = LocalDate.now().plusDays(7);
        updateDateButton();
        renderEvents();
        CountdownWidgetProvider.refreshAll(this);
        Toast.makeText(this, "已新增活動", Toast.LENGTH_SHORT).show();
    }

    private void renderEvents() {
        eventList.removeAllViews();
        List<EventStore.Event> events = EventStore.getAll(this);

        if (events.isEmpty()) {
            TextView empty = text("還沒有活動", 15, Color.parseColor("#758291"), false);
            empty.setPadding(0, dp(16), 0, dp(16));
            eventList.addView(empty);
            return;
        }

        for (EventStore.Event event : events) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(12), dp(10), dp(12));
            row.setBackground(rounded(Color.parseColor("#171E27"), 14));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

            TextView name = text(event.name, 16, event.color, true);
            info.addView(name);

            long days = ChronoUnit.DAYS.between(LocalDate.now(), event.date);
            String dateText = event.date.format(displayDate);
            if (days >= 0) {
                dateText += days == 0 ? "　今天" : "　剩 " + days + " 天";
            } else {
                dateText += "　已過期";
            }
            TextView date = text(dateText, 13, Color.parseColor("#9AA7B5"), false);
            LinearLayout.LayoutParams dateP = wrap();
            dateP.topMargin = dp(3);
            info.addView(date, dateP);

            row.addView(info, infoParams);

            Button delete = new Button(this);
            delete.setText("刪除");
            delete.setTextColor(Color.parseColor("#FF8A80"));
            delete.setTextSize(13);
            delete.setAllCaps(false);
            delete.setBackgroundColor(Color.TRANSPARENT);
            delete.setOnClickListener(v -> {
                EventStore.delete(this, event.id);
                renderEvents();
                CountdownWidgetProvider.refreshAll(this);
            });
            row.addView(delete, new LinearLayout.LayoutParams(dp(70), dp(48)));

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(8);
            eventList.addView(row, rowParams);
        }
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return view;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
