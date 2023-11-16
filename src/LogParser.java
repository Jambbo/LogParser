import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogParser implements IPQuery {
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

    private class LogEntity {
        private String ip;
        private String user;
        private Date date;
        private Event event;
        private int evenetAdditionalParameter;
        private Status status;
        public LogEntity(String ip, String user, Date date, Event event, int eventAdditionalParameter, Status status) {
            this.ip=ip;
            this.user = user;
            this.date = date;
            this.event = event;
            this.evenetAdditionalParameter = eventAdditionalParameter;
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
            return evenetAdditionalParameter;
        }

        public Status getStatus() {
            return status;
        }
    }
}