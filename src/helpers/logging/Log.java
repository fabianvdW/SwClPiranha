package helpers.logging;

import helpers.GlobalFlags;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private BufferedWriter bw;

    public Log(String path) {
        if (GlobalFlags.MAKE_LOGS) {
            File f = new File(path);
            try {
                this.bw = new BufferedWriter(new FileWriter(f));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void flush() {
        try {
            this.bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(LogLevel level, String message) {
        if (GlobalFlags.MAKE_LOGS) {
            DateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSS");
            String timeStamp = sdf.format(new Date());
            try {
                if (level != LogLevel.ERROR) {
                    this.bw.write("[" + timeStamp + "]  " + level + ": " + message + "\n");
                } else {
                    this.bw.write("[" + timeStamp + "] " + level + ": " + message + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onClose() {
        if (GlobalFlags.MAKE_LOGS) {
            try {
                this.bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
