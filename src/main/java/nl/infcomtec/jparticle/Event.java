/*
 * Copyright (c) 2017 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.jparticle;

import java.util.Date;
import org.json.JSONObject;

/**
 *
 * @author walter
 */
public class Event {

    final public String name;
    final public String coreId;
    final public String data;
    final public Date publishedAt;
    final public long ttl;

    public Event(String name, JSONObject jo) {
        this.name = name;
        this.coreId = jo.getString("coreid");
        this.data = jo.getString("data");
        this.publishedAt = Cloud.parseDateTime(jo.getString("published_at"));
        long _ttl = jo.getLong("ttl") * 1000L;
        this.ttl = Math.min(60000L, _ttl);
    }

    public Date expires() {
        return new Date(publishedAt.getTime() + ttl);
    }

    @Override
    public String toString() {
        return "\nEvent{" + "name=" + name + ", coreId=" + coreId + ", data=" + data + ", publishedAt=" + publishedAt + ", ttl=" + ttl + ", expires=" + expires() + '}';
    }

}
