import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class LogParser implements IPQuery, UserQuery,DateQuery {
    private Path logDir;
    private List<LogEntity> logEntities = new ArrayList<>();
    private DateFormat simpleDateFormat = new SimpleDateFormat("d.M.yyyy H:m:s");



    public LogParser(Path logDir) {
        this.logDir = logDir;
        readLogs();
    }

    public int getNumberOfUniqueIPs(Date after, Date before) {
        return getUniqueIPs(after, before).size();
    }

    public Set<String> getUniqueIPs(Date after,Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                set.add(logEntities.get(i).getIp());
            }
        }
        return set;
    }

    public Set<String> getIPsForUser(String user, Date after, Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getUser().equals(user)){
                    set.add(logEntities.get(i).getIp());
                }
            }
        }
        return set;
    }

    public Set<String> getIPsForEvent(Event event, Date after, Date before) {
        Set<String> set = new HashSet<>();
        for(int i=0;i<logEntities.size();i++){
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(event)){
                    set.add(logEntities.get(i).getIp());
                }
            }
        }
        return set;
    }

    public Set<String> getIPsForStatus(Status status,Date after,Date before) {
        Set<String> set = new HashSet<>();
        for(int i=0;i< logEntities.size();i++){
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getStatus().equals(status)){
                    set.add(logEntities.get(i).getIp());
                }
            }
        }
        return set;
    }
    private void readLogs(){
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logDir)){
        for(Path file:directoryStream){
            if(file.toString().toLowerCase().endsWith(".log")){
                try(BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))){
                    String line = null;
                    while((line=reader.readLine())!=null){
                        String[] params = line.split("\t");
                        if(params.length!=5){continue;}
                        String ip=params[0];
                        String user = params[1];
                        Date date = readDate(params[2]);
                        Event event = readEvent(params[3]);
                        int eventAdditionalParameter = -1;
                        if(event.equals(Event.SOLVE_TASK) || event.equals(Event.DONE_TASK)){
                            eventAdditionalParameter = readAdditionalParameter(params[3]);
                        }
                        Status status = readStatus(params[4]);
                        LogEntity logEntity = new LogEntity(ip,user,date,event,eventAdditionalParameter,status);
                        logEntities.add(logEntity);
                    }
                }
            }
        }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private Date readDate(String lineToParse){
        Date date = null;
        try{
            date = simpleDateFormat.parse(lineToParse);
        }catch(Exception ignore){

        }
        return date;
    }
    private Event readEvent(String lineToParse){
        Event event = null;
        if(lineToParse.contains("SOLVE_TASK")){
            event=Event.SOLVE_TASK;
        }else if(lineToParse.contains("DONE_TASK")){
            event=Event.DONE_TASK;
        }else{
            switch (lineToParse){
                case "LOGIN"-> event=Event.LOGIN;
                case "DOWNLOAD_PLUGIN"-> event=Event.DOWNLOAD_PLUGIN;
                case "WRITE_MESSAGE"->event = Event.WRITE_MESSAGE;
            }
        }
        return event;
    }
    private int readAdditionalParameter(String lineToParse){
        if(lineToParse.contains("SOLVE_TASK")){
            lineToParse.replace("SOLVE_TASK","").replaceAll(" ","");
        }else{
            lineToParse.replace("DONE_TASK","").replaceAll(" ","");
        }
        return Integer.parseInt(lineToParse);
    }
    private Status readStatus(String lineToParse){
        Status status = null;
        switch(lineToParse){
            case "OK": status =Status.OK;
            break;
            case "ERROR": status = Status.ERROR;
            break;
            case "FAILED" : status = Status.FAILED;
        }
        return status;
    }
    private boolean dateBetweenDates(Date current, Date after, Date before){
        if(after==null){
            after = new Date(0);
        }
        if(before ==null){
            before = new Date(Long.MAX_VALUE);
        }
        return current.after(after) && current.before(before);
    }

    @Override
    public Set<String> getAllUsers() {
        Set<String> users = new HashSet<>();
        for(int i=0;i<logEntities.size();i++){
            users.add(logEntities.get(i).getUser());
        }
        return users;
    }

    @Override
    public int getNumberOfUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                set.add(logEntities.get(i).getUser());
            }
        }
        return set.size();
    }

    @Override
    public int getNumberOfUserEvents(String user, Date after, Date before) {
        Set<Event> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getUser().equals(user)){
                    set.add(logEntities.get(i).getEvent());
                }
            }
        }
        return set.size();
    }

    @Override
    public Set<String> getUsersForIP(String ip, Date after, Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getIp().equals(ip)){
                    set.add(logEntities.get(i).getUser());
                }
            }
        }
        return set;
    }

    @Override
    public Set<String> getLoggedUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(Event.LOGIN)){
                    set.add(logEntities.get(i).getUser());
                }
            }
        }
        return set;
    }

    @Override
    public Set<String> getDownloadedPluginUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(Event.DOWNLOAD_PLUGIN)){
                    set.add(logEntities.get(i).getUser());
                }
            }
        }
        return set;
    }

    @Override
    public Set<String> getWroteMessageUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(Event.WRITE_MESSAGE)){
                    set.add(logEntities.get(i).getUser());
                }
            }
        }
        return set;
    }

    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(Event.SOLVE_TASK)){
                    set.add(logEntities.get(i).getUser());
                }
            }
        }
        return set;
    }

    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before, int task) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(Event.SOLVE_TASK)
                && logEntities.get(i).getEventAdditionalParameter()==task){
                    set.add(logEntities.get(i).getUser());
                }
            }
        }
        return set;
    }

    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(Event.DONE_TASK)){
                    set.add(logEntities.get(i).getUser());
                }
            }
        }
        return set;
    }

    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before, int task) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(Event.DONE_TASK)
                && logEntities.get(i).getEventAdditionalParameter()==task){
                    set.add(logEntities.get(i).getUser());
                }
            }
        }
        return set;
    }

    @Override
    public Set<Date> getDatesForUserAndEvent(String user, Event event, Date after, Date before) {
        Set<Date> res = new HashSet<>();
        for(int i=0;i<logEntities.size();i++){
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(event)
                        && logEntities.get(i).getUser().equals(user)){
                    res.add(logEntities.get(i).getDate());
                }
            }
        }
        return res;
    }

    @Override
    public Set<Date> getDatesWhenSomethingFailed(Date after, Date before) {
        Set<Date> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getStatus().equals(Status.FAILED)){
                    set.add(logEntities.get(i).getDate());
                }
            }
        }
        return set;
    }

    @Override
    public Set<Date> getDatesWhenErrorHappened(Date after, Date before) {
        Set<Date> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getStatus().equals(Status.ERROR)){
                    set.add(logEntities.get(i).getDate());
                }
            }
        }
        return set;
    }

    @Override
    public Date getDateWhenUserLoggedFirstTime(String user, Date after, Date before) {
        Set<Date> set = new HashSet<>();
        for (int i = 0; i < logEntities.size(); i++) {
            if(dateBetweenDates(logEntities.get(i).getDate(),after,before)){
                if(logEntities.get(i).getEvent().equals(Event.LOGIN)
                &&logEntities.get(i).getUser().equals(user)){
                    set.add(logEntities.get(i).getDate());
                }
            }
        }
        if(set.size()==0)return null;
        Date minDate = set.iterator().next();
        for (Date date:set){
            if(date.getTime()<minDate.getTime()){
                minDate=date;
            }
        }
        return minDate;
    }

    @Override
    public Date getDateWhenUserSolvedTask(String user, int task, Date after, Date before) {
        return null;
    }

    @Override
    public Date getDateWhenUserDoneTask(String user, int task, Date after, Date before) {
        return null;
    }

    @Override
    public Set<Date> getDatesWhenUserWroteMessage(String user, Date after, Date before) {
        return null;
    }

    @Override
    public Set<Date> getDatesWhenUserDownloadedPlugin(String user, Date after, Date before) {
        return null;
    }

    private class LogEntity {
        private String ip;
        private String user;
        private Date date;
        private Event event;
        private int eventAdditionalParameter;
        private Status status;
        public LogEntity(String ip, String user, Date date, Event event, int eventAdditionalParameter, Status status) {
            this.ip=ip;
            this.user = user;
            this.date = date;
            this.event = event;
            this.eventAdditionalParameter = eventAdditionalParameter;
            this.status = status;
        }

        public String getIp() {
            return ip;
        }

        public String getUser() {
            return user;
        }

        public Date getDate() {
            return date;
        }

        public Event getEvent() {
            return event;
        }

        public int getEventAdditionalParameter() {
            return eventAdditionalParameter;
        }

        public Status getStatus() {
            return status;
        }
    }
}