/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.jparticle;

import java.util.UUID;

/**
 * Called for any device, any event
 */
public abstract class AnyDeviceEvent implements DeviceEvent {

    private final UUID uuid = UUID.randomUUID();

    /**
     * Returns null to match any device id
     */
    @Override
    public String forDeviceId() {
        return null;
    }

    /**
     * Returns null to match any device id
     */
    @Override
    public String forDeviceName() {
        return null;
    }

    /**
     * Returns null to match any event name
     */
    @Override
    public String forEventName() {
        return null;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

}
