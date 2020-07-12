package mecaPlanner;


import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Log {

    public static class Level {
        public static final int DEBUG   = 0;
        public static final int INFO    = 1;
        public static final int WARNING = 2;
        public static final int SEVERE  = 3;
    }

    private static int threshold = Level.INFO;

    private static PrintWriter writer = new PrintWriter(System.out);

    public static void setOutput(String str) {
        if (str.equalsIgnoreCase("stdout")) {
            writer = new PrintWriter(System.out);
        }
        else {
            try {
                writer = new PrintWriter(str);
            }
            catch (FileNotFoundException ex) { 
                throw new RuntimeException("failed to set up log file \"" + str + "\": " + ex.getMessage());
            }
        }
    }

    public static void setThreshold(int newThreshold) {
        if (newThreshold < Level.DEBUG || newThreshold > Level.SEVERE) {
            throw new RuntimeException("threshold out of bounds: " + Integer.toString(newThreshold));
        }
        threshold = newThreshold;
    }

    public static void setThreshold(String str) {
        if (str.equalsIgnoreCase("debug")) {
            setThreshold(Level.DEBUG);
        }
        else if (str.equalsIgnoreCase("info")) {
            setThreshold(Level.INFO);
        }
        else if (str.equalsIgnoreCase("warning")) {
            setThreshold(Level.WARNING);
        }
        else if (str.equalsIgnoreCase("severe")) {
            setThreshold(Level.SEVERE);
        }
        else {
            throw new RuntimeException("unknown threshold: " + str);
        }
    }


    private static void log(String level, String message){
        writer.print("[");
        writer.print(level);
        writer.print("] ");
        writer.println(message);
        writer.flush();
    }

    public static void debug(String message) {
        if (threshold <= Level.DEBUG) {
            log("DEBUG", message);
        }
    }

    public static void info(String message) {
        if (threshold <= Level.INFO) {
            log("INFO", message);
        }
    }

    public static void warning(String message) {
        if (threshold <= Level.WARNING) {
            log("WARNING", message);
        }
    }

    public static void severe(String message) {
        if (threshold <= Level.SEVERE) {
            log("SEVERE", message);
        }
    }

}
