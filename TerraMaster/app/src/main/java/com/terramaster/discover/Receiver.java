package com.terramaster.discover;

import java.util.ArrayList;

public interface Receiver {
    /**
     * Process the list of discovered servers. This is always called once
     * after a short timeout.
     *
     * @param servers list of discovered servers, null on error
     */
    public void addAnnouncedServers(ArrayList<DeviceData> devicesList);
}
