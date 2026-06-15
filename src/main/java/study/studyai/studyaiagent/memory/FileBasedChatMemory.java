package study.studyai.studyaiagent.memory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileBasedChatMemory implements ChatMemory {

    private static final Kryo KRYO = new Kryo();

    private final String baseDir;

    static {
        KRYO.setRegistrationRequired(false);
        KRYO.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String baseDir) {
        this.baseDir = baseDir;
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> messageList = get(conversationId);
        messageList.addAll(messages);
        saveConversation(conversationId, messageList);
    }

    @Override
    public List<Message> get(String conversationId) {
        File file = getConversationFile(conversationId);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (Input input = new Input(new FileInputStream(file))) {
            return KRYO.readObject(input, ArrayList.class);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            KRYO.writeObject(output, messages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getConversationFile(String conversationId) {
        String fileName = conversationId.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".kryo";
        return new File(baseDir, fileName);
    }
}
