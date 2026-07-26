import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;

public class MiniBrowser extends MIDlet implements CommandListener {

    private Display display;
    private Form mainForm;
    private TextField urlField;
    private StringItem statusLabel;
    private StringItem contentArea;
    private Command goCommand;
    private Command exitCommand;

    // IP-адрес твоего сервера из Pydroid 3
    private static final String PROXY_SERVER = "http://192.168.1.51:5000/parse?url=";

    public MiniBrowser() {
        mainForm = new Form("Supercash v0.1 Alpha 1");

        urlField = new TextField("Address:", "google.com", 256, TextField.URL);
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
        display = Display.getDisplay(this);
        display.setCurrent(mainForm);
    }

    protected void pauseApp() {}

    protected void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == goCommand) {
            new Thread(new Runnable() {
                public void run() {
                    loadWebPage();
                }
            }).start();
        } else if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }

    private void loadWebPage() {
        statusLabel.setText("Connecting...");
        contentArea.setText("");
        HttpConnection http = null;
        InputStream is = null;

        try {
            String target = urlField.getString().trim();
            String fullUrl = PROXY_SERVER + target;

            http = (HttpConnection) Connector.open(fullUrl);
            int rc = http.getResponseCode();

            if (rc == HttpConnection.HTTP_OK) {
                statusLabel.setText("Loading...");
                is = http.openInputStream();
                
                StringBuffer sb = new StringBuffer();
                int ch;
                while ((ch = is.read()) != -1) {
                    sb.append((char) ch);
                }

                statusLabel.setText("Done");
                contentArea.setText(sb.toString());
            } else {
                statusLabel.setText("Error code: " + rc);
            }

        } catch (Exception e) {
            statusLabel.setText("Error");
            contentArea.setText(e.getMessage());
        } finally {
            try { if (is != null) is.close(); } catch (Exception e) {}
            try { if (http != null) http.close(); } catch (Exception e) {}
        }
    }
}
