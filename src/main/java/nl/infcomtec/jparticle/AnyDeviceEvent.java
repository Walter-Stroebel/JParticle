/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.jparticle;

import java.util.UUID;

public abstract class AnyDeviceEvent implements DeviceEvent {

    private final UUID uuid = UUID.randomUUID();

    @Override
    public String forDeviceId() {
        return null;
    }

    @Override
    public String forDeviceName() {
        return null;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

}
