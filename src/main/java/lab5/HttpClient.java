package lab5;

import akka.actor.ActorSystem;
import org.asynchttpclient.AsyncHttpClient;

public class HttpClient {

    AsyncHttpClient(ActorSystem actorSystem) {
        cacheActor = actorSystem.actorOf(CacheActor.props(), "cacheActor");
    }
}