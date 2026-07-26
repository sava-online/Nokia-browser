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

    // ВНИМАНИЕ: Замени этот IP на IP-адрес твоего телефона с Pydroid в локальной сети!
    private static final String PROXY_SERVER = "http://192.168.1.15:5000/parse?url=";

    public MiniBrowser() {
        display = Display.getDisplay(this);

        mainForm = new Form("Supercash v0.1");

        urlField = new TextField("Address:", "google.com", 256, TextField.URL);
        statusLabel = new StringItem("Status:", "Ready");
        contentArea = new StringItem("Content:", "");

        mainForm.append(urlField);
        mainForm.append(statusLabel);
        mainForm.append(contentArea);

        goCommand = new Command("GO", Command.OK, 1);
        exitCommand = new Command("EXIT", Command.EXIT, 2);

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
            new Thread(this).start();
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
        InputStreamReader isr = null;

        try {
            String targetUrl = urlField.getString().trim();
            
            // Если запрос идет не к локальному серверу напрямую, то оборачиваем через прокси
            String fullUrl = PROXY_SERVER + targetUrl;

            http = (HttpConnection) Connector.open(fullUrl);
            int rc = http.getResponseCode();

            if (rc == HttpConnection.HTTP_OK) {
                statusLabel.setText("Loading...");
                is = http.openInputStream();
                
                // Вот та самая строчка: читаем поток строго в UTF-8, чтобы не было крякозябр!
                isr = new InputStreamReader(is, "UTF-8");
                StringBuffer sb = new StringBuffer();
                int ch;
                while ((ch = isr.read()) != -1) {
                    sb.append((char) ch);
                }

                statusLabel.setText("Done");
                contentArea.setText(sb.toString());
            } else {
                statusLabel.setText("HTTP Error: " + rc);
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        } finally {
            try { if (isr != null) isr.close(); } catch (Exception e) {}
            try { if (is != null) is.close(); } catch (Exception e) {}
            try { if (http != null) http.close(); } catch (Exception e) {}
        }
    }
}
