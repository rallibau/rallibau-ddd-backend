package com.rallibau.shared.infrastructure.bus.query;

import com.rallibau.shared.domain.Utils;
import com.rallibau.shared.domain.bus.query.Query;

import java.io.Serializable;
import java.util.HashMap;

public final class QueryJsonSerializer {
    public static String serialize(Query query) {
        HashMap<String, Serializable> attributes = query.toPrimitives();
        attributes.put("id", query.correlationId());

        return Utils.jsonEncode(new HashMap<String, Serializable>() {{
            put("data", new HashMap<String, Serializable>() {{
                put("type", query.getClass().getName());
                put("occurred_on", query.occurredOn());
                put("attributes", attributes);
            }});
            HashMap<String, Serializable> meta = new HashMap<>();
            meta.put("type", "event");
            put("meta", meta);
        }});
    }
}
