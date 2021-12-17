package lab5;

import akka.actor.AbstractActor;
import akka.japi.Pair;

import javax.xml.ws.Response;
import java.util.HashMap;
import java.util.Map;

public class CacheActor extends AbstractActor {

    private Map<String, Long> cache = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Pair.class, t-> {
                    String key = (String) t.first();
                    if (cache.containsKey(key))
                        sender().tell(new Response(key, cache.get(key), self());
                        else
                            );
                } )
    }
}