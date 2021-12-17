package lab5;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Pair;

import javax.xml.ws.Response;
import java.util.HashMap;
import java.util.Map;

public class CacheActor extends AbstractActor {

    private Map<String, Long> cache = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Pair.class, pair-> {
                    String key = (String) pair.first();
                    if (cache.containsKey(key))
                        sender().tell(new Response(key, cache.get(key)), self());
                    else sender().tell("NO RESPONSE", self());
                })
                .match(Response.class, response -> cache.put(response.getHostName(), response.getResponseTime()))
                .build();
    }

    static Props props() {
        return Props.create(CacheActor.class);
    }
}