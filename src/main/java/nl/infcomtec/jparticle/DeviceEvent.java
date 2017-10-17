/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.jparticle;

import java.util.UUID;

/**
 *
 * @author walter
 */
public interface DeviceEvent {
    public void event(Event e);
    public UUID uuid();
    public String forDeviceId();
    public String forDeviceName();
}
