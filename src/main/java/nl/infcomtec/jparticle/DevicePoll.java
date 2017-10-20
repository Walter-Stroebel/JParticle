/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.jparticle;

import java.util.UUID;

/**
 * Use this interface to create a poller.
 * 
 * @author walter
 */
public interface DevicePoll extends Runnable {

    /** Should return the time in milliseconds to wait between polls.
     * 
     * @return The time in milliseconds between polls. 
     */
    public long interval();
    /**
     * Unique identifier for this task.
     * @return Should always return the same UUID for a task.
     */
    public UUID uuid();
}
