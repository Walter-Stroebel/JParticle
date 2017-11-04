/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.jparticle;

import java.util.UUID;

/**
 * Implement this interface to receive publish events.
 *
 * @author walter
 */
public interface DeviceEvent {

    /**
     * Called whenever a subscribed event arrives.
     *
     * @param e The event from the cloud.
     */
    public void event(Event e);

    /**
     * Used to cancel a subscription.
     *
     * @return Should always return the same UUID for a given task.
     */
    public UUID uuid();

    /**
     * If not null, we only want events for this device Id.
     *
     * @return null or deviceId to match.
     */
    public String forDeviceId();

    /**
     * If not null, we only want events for this device name.
     *
     * @return null or device name to match.
     */
    public String forDeviceName();
}
