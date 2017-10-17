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
public interface DevicePoll extends Runnable {

    public long interval();
    public UUID uuid();
}
