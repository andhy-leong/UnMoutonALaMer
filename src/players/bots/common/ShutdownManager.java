package players.bots.common;
import java.util.ArrayList;
import java.util.List;

public class ShutdownManager {
    private static ShutdownManager instance;
    private List<AutoCloseable> resourcesToClose;

    private ShutdownManager() {
        resourcesToClose = new ArrayList<>();
    }

    public static ShutdownManager getInstance() {
        if (instance == null) {
            instance = new ShutdownManager();
        }
        return instance;
    }

    public void registerResource(AutoCloseable resource) {
        resourcesToClose.add(resource);
    }

    public void shutdown() {
        for (AutoCloseable resource : resourcesToClose) {
            try {
                resource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
