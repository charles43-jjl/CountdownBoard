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
        public final int repeatType; // 0 none, 1 every N days, 2 weekly, 3 monthly, 4 yearly
        public final int repeatInterval;

        public Event(String id, String name, LocalDate date, int color, int repeatType, int repeatInterval) {
            this.id=id; this.name=name; this.date=date; this.color=color;
            this.repeatType=repeatType; this.repeatInterval=repeatInterval;
        }
        public boolean isRecurring(){ return repeatType > 0; }
        public String repeatLabel(){
            if(repeatType==1) return "每 "+repeatInterval+" 天";
            if(repeatType==2) return "每週";
            if(repeatType==3) return "每月 "+date.getDayOfMonth()+" 日";
            if(repeatType==4) return "每年";
            return "";
        }
        public LocalDate nextDate(LocalDate today){
            if(!isRecurring() || !date.isBefore(today)) return date;
            LocalDate next=date;
            if(repeatType==1){
                int n=Math.max(1,repeatInterval);
                while(next.isBefore(today)) next=next.plusDays(n);
            } else if(repeatType==2){
                while(next.isBefore(today)) next=next.plusWeeks(1);
            } else if(repeatType==3){
                while(next.isBefore(today)) next=next.plusMonths(1);
            } else if(repeatType==4){
                while(next.isBefore(today)) next=next.plusYears(1);
            }
            return next;
        }
    }

    public static synchronized List<Event> getAll(Context context){
        ArrayList<Event> result=new ArrayList<>();
        String raw=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(KEY,"[]");
        try{
            JSONArray array=new JSONArray(raw);
            for(int i=0;i<array.length();i++){
                JSONObject o=array.getJSONObject(i);
                int type=o.optInt("repeatType",-1);
                int interval=o.optInt("repeatInterval",0);
                if(type<0){ // migrate the previous repeatMonths format
                    int months=o.optInt("repeatMonths",0);
                    type=months==0?0:(months==12?4:3);
                }
                result.add(new Event(o.optString("id",UUID.randomUUID().toString()),
                    o.optString("name","未命名活動"),LocalDate.parse(o.getString("date")),
                    o.optInt("color",0xFF4DD0E1),type,interval));
            }
        }catch(Exception ignored){}
        LocalDate today=LocalDate.now();
        Collections.sort(result,Comparator.comparing(e->e.nextDate(today)));
        return result;
    }

    private static synchronized void save(Context context,List<Event> events){
        JSONArray array=new JSONArray();
        for(Event e:events){
            JSONObject o=new JSONObject();
            try{
                o.put("id",e.id); o.put("name",e.name); o.put("date",e.date.toString());
                o.put("color",e.color); o.put("repeatType",e.repeatType); o.put("repeatInterval",e.repeatInterval);
                array.put(o);
            }catch(Exception ignored){}
        }
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(KEY,array.toString()).apply();
    }

    public static synchronized void add(Context context,String name,LocalDate date,int color,int repeatType,int repeatInterval){
        List<Event> events=getAll(context);
        events.add(new Event(UUID.randomUUID().toString(),name,date,color,repeatType,repeatInterval));
        save(context,events);
    }
    public static synchronized void delete(Context context,String id){
        List<Event> events=getAll(context); events.removeIf(e->e.id.equals(id)); save(context,events);
    }
    public static List<Event> upcoming(Context context){
        LocalDate today=LocalDate.now(); ArrayList<Event> result=new ArrayList<>();
        for(Event e:getAll(context)) if(e.isRecurring()||!e.date.isBefore(today)) result.add(e);
        result.sort(Comparator.comparing(e->e.nextDate(today))); return result;
    }
}
