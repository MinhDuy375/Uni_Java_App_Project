package views;

import java.util.ArrayList;
import java.util.List;

public class ComputerStatusManager {
    private static ComputerStatusManager instance;
    private List<ComputerStatusListener> listeners = new ArrayList<>();

    private ComputerStatusManager() {
    }

    public static ComputerStatusManager getInstance() {
        if (instance == null) {
            instance = new ComputerStatusManager();
        }
        return instance;
    }

    public void addListener(ComputerStatusListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ComputerStatusListener listener) {
        listeners.remove(listener);
    }

    public void notifyComputerStatusChanged(String maMay, String newStatus) {
        for (ComputerStatusListener listener : listeners) {
            listener.onComputerStatusChanged(maMay, newStatus);
        }
    }
}

interface ComputerStatusListener {
    void onComputerStatusChanged(String maMay, String newStatus);
}