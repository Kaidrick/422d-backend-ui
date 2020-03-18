package moe.ofs.backend.plugin.greeting;

import moe.ofs.backend.Plugin;
import moe.ofs.backend.PluginClassLoader;
import moe.ofs.backend.function.Message;
import moe.ofs.backend.function.MessageQueue;
import moe.ofs.backend.function.MessageQueueFactory;
import moe.ofs.backend.handlers.BackgroundTaskRestartObservable;
import moe.ofs.backend.handlers.ExportUnitSpawnObservable;
import moe.ofs.backend.domain.ExportObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Greeting addon implements the functionality to send Message of the Day to players who just spawn into game
 * The message to be send should be read from a file? external or internal?
 * If there are multiple message to be sent, user should be able to specify the delay between each message
 */

@Component
public class Greeting implements Plugin {

    public final String name = "Server Greeting";
    public final String desc = "Say Hello on player spawn";

    private boolean isLoaded;

    private List<Message> list;

    public List<Message> getList() {
        return list;
    }

    public void setList(List<Message> list) {
        this.list = list;
    }

    private ExportUnitSpawnObservable exportUnitSpawnObservable;
    private BackgroundTaskRestartObservable backgroundTaskRestartObservable;

    private final MessageQueueFactory messageQueueFactory;

    @Autowired
    public Greeting(MessageQueueFactory messageQueueFactory) {
        this.messageQueueFactory = messageQueueFactory;

        // or load from xml file?
        list = new ArrayList<>();
    }

    @PostConstruct
    @Override
    public void init() {
        System.out.println("Greeting plugin bean constructed...register");
        Plugin.super.init();
        PluginClassLoader.loadedPluginSet.add(this);
    }

    @Override
    public void register() {
        exportUnitSpawnObservable = this::greet;
        exportUnitSpawnObservable.register();
        isLoaded = true;
        writeConfiguration("enabled", "true");
    }

    @Override
    public void unregister() {
        exportUnitSpawnObservable.unregister();
        isLoaded = false;
        writeConfiguration("enabled", "false");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    private void greet(ExportObject unit) {
        testMessageFunction(unit);
    }

    private void testMessageFunction(ExportObject object) {
        if(object.getFlags().get("Human")) {
            messageQueueFactory.setExportObject(object);
            MessageQueue messageQueue = messageQueueFactory.getObject();
            if (messageQueue != null) {
                list.forEach(messageQueue::pend);

                messageQueue.send();
            } else {
                throw new RuntimeException("MessageQueueFactory failed to provide a new MessageQueue instance.");
            }

        }
    }
}

