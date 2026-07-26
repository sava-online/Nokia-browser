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

    // ВАЖНО: Замени этот URL на адрес своего приложения на PythonAnywhere!
    private static final String SERVER_URL = "http://твой_логин.pythonanywhere.com/view?url=";

    public MiniBrowser() {
        display = Display.getDisplay(this);

        // Создаем элементы интерфейса
        mainForm = new Form("2G Web Browser");
        urlField = new TextField("Адрес сайта:", "google.com", 256, TextField.ANY);
        statusLabel = new StringItem("Статус:", "Готов к работе");
        contentArea = new StringItem("Контент:", "");

        // Кнопки управления
        goCommand = new Command("Перейти", Command.OK, 1);
        exitCommand = new Command("Выход", Command.EXIT, 2);

        // Собираем экран
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
            // Запускаем сеть в отдельном потоке, чтобы экран не зависал
            Thread thread = new Thread(this);
            thread.start();
        } else if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        }
    }

    public void run() {
        String inputUrl = urlField.getString().trim();
        if (inputUrl.length() == 0) {
            statusLabel.setText("Ошибка: введите URL!");
            return;
        }

        statusLabel.setText("Соединение с сервером...");
        contentArea.setText("");

        HttpConnection http = null;
        InputStream is = null;

        try {
            // Формируем запрос к твоему PythonAnywhere
            String requestUrl = SERVER_URL + inputUrl;
            http = (HttpConnection) Connector.open(requestUrl);
            http.setRequestMethod(HttpConnection.GET);

            int responseCode = http.getResponseCode();

            if (responseCode == HttpConnection.HTTP_OK) {
                statusLabel.setText("Загрузка данных...");
                is = http.openInputStream();
                
                StringBuffer sb = new StringBuffer();
                int ch;
                // Читаем ответ побайтово
                while ((ch = is.read()) != -1) {
                    sb.append((char) ch);
                }

                statusLabel.setText("Готово!");
                contentArea.setText(sb.toString());
            } else {
                statusLabel.setText("Ошибка сервера: " + responseCode);
            }

        } catch (Exception e) {
            statusLabel.setText("Ошибка сети: " + e.getMessage());
        } finally {
            // Обязательно закрываем соединения
            try {
                if (is != null) is.close();
                if (http != null) http.close();
            } catch (Exception e) {}
        }
    }
}
