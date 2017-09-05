package networkcommunication.unused;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConfigFileWriter {

    private Charset utf8 = StandardCharsets.UTF_8;
    private static final ConfigFileWriter instance = new ConfigFileWriter();

    private ConfigFileWriter() {}

    public static ConfigFileWriter getInstance() { return instance; }

    public synchronized void writeToConfigFile(String configFilePath,
                                               String nodeInformation) throws IOException {

        String informationWithNewline = nodeInformation + "\n";
        Files.write(Paths.get(configFilePath), informationWithNewline.getBytes(utf8), StandardOpenOption.APPEND);
    }

    public void clearFile(String configFilePath) throws IOException {
        Files.write(Paths.get(configFilePath), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

}
