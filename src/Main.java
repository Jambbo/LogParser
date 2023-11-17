import java.awt.image.AreaAveragingScaleFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        LogParser logParser = new LogParser(Paths.get("C:\\Users\\My PC\\IdeaProjects\\untitled5\\src\\example.log"));
        System.out.println(logParser.getNumberOfUniqueIPs(null, new Date()));
    }
}