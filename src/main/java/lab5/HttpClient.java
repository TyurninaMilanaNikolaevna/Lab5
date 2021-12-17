package lab5;

import akka.actor.ActorSystem;

public class HttpClient {

    void AsyncHttpClient(ActorSystem actorSystem) {
        cacheActor = actorSystem.actorOf(CacheActor.props(), "cacheActor");
    }
}