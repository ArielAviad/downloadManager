import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

/**
 * Created by ariel on 14/09/16.
 */
class Download extends Observable implements Runnable{
    private static final int MAX_BUFFER_SIZE = 1024;

    public static final String STATUSES[] = {
            "Downloading", "Paused","Complete", "Cancelled","Error"
    };

    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;

    private URL url;
    private int size;
    private int downloaded;
    private int status;

    //not allow default constructor.
    private Download(){}

    Download(URL url){
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
        download();
    }

    String getURL(){return url.toString(); }

    int getSize(){return size;}

    float getProgress(){return (float)(downloaded)/size;}

    int getStatus(){return status;}

    void pause(){
        status = PAUSED;
        stateChanged();
    }

    void resume(){
        status = DOWNLOADING;
        stateChanged();
    }

    void cancel(){
        status = CANCELLED;
        stateChanged();
    }

    private void error() {
        status = ERROR;
        stateChanged();
    }

    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @NotNull
    private String getFileName(URL url) {
        return url.getFile().substring(url.getFile().lastIndexOf('/')+1);
    }

    @Override
    public void run() {
        RandomAccessFile file = null;
        InputStream stream = null;
        System.out.println("inter run...");
        try {
            // Open connection to URL.
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            // Specify what portion of file to download.
            connection.setRequestProperty("Range",
                    "bytes=" + downloaded + "-");
            // Connect to server.
            connection.connect();
            // Make sure response code is in the 200 range.
            if (connection.getResponseCode() / 100 != 2) {
                error();
            }
            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error();
            }
            /* Set the size for this download if it
            hasn't been already set. */
            if (size == -1) {
                size = contentLength;
                stateChanged();
            }
            // Open file and seek to the end of it.
            file = new RandomAccessFile("/home/ariel/IdeaProjects/Downloader/src/" + getFileName(url), "rw");
            file.seek(downloaded);
            stream = connection.getInputStream();
            System.out.println("Before downloading...");
            while (status == DOWNLOADING) {
                System.out.println("File: " + getFileName(url) +
                                "\nSize: " + size
                        );
                /* Size buffer according to how much of the
                file is left to download. */
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }
                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1)
                    break;
                // Write buffer to file.
                file.write(buffer, 0, read);
                downloaded += read;
                stateChanged();
            }
/* Change status to complete if this point was
reached because downloading has finished. */
            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
            }
        } catch (Exception e) {
            error();
        }finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void stateChanged() {
        setChanged();
        notifyObservers();
    }
}