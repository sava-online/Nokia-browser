import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;

public class MiniBrowser extends MIDlet implements CommandListener, Runnable {
    private Display display;
    private Form mainForm;
    private TextField urlField;
    private StringItem statusLabel;
    private StringItem contentArea;
    private Command goCommand;
    private Command exitCommand;

    public MiniBrowser() {
        display = Display.getDisplay(this);
        mainForm = new Form("2G Browser");
        urlField = new TextField("URL:", "google.com", 256, TextField.ANY);
        statusLabel = new StringItem("Status:", "Ready");
        contentArea = new StringItem("Content:", "");

        goCommand = new Command("Go", Command.OK, 1);
        exitCommand = new Command("Exit", Command.EXIT, 2);

        mainForm.append(urlField);
        mainForm.append(statusLabel);
        mainForm.append(contentArea);

        mainForm.addCommand(goCommand);
        mainForm.addCommand(exitCommand);
        mainForm.setCommandListener(this);
    }

    protected void startApp() {
        display.setCurrent(mainForm);
    }

    protected void pauseApp() {}
    protected void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == goCommand) {
            Thread t = new Thread(this);
            t.start();
        } else if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        }
    }

    public void run() {
        statusLabel.setText("Connecting...");
        contentArea.setText("");
        HttpConnection http = null;
        InputStream is = null;
        try {
            http = (HttpConnection) Connector.open("http://" + urlField.getString().trim());
            int rc = http.getResponseCode();
            if (rc == HttpConnection.HTTP_OK) {
                statusLabel.setText("Loading...");
                is = http.openInputStream();
                StringBuffer sb = new StringBuffer();
                int ch;
                while ((ch = is.read()) != -1) {
                    sb.append((char) ch);
                }
                statusLabel.setText("Done!");
                contentArea.setText(sb.toString());
            } else {
                statusLabel.setText("HTTP Error: " + rc);
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        } finally {
            try { if (is != null) is.close(); } catch (Exception e) {}
            try { if (http != null) http.close(); } catch (Exception e) {}
        }
    }
}
