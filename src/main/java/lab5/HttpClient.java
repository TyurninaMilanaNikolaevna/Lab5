package lab5;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import javafx.util.Pair;

import java.util.Collection;

public class HttpClient {

    private Sink<Pair<String, Integer>,
        return Flow.<Pair<String,Integer>>create()
            .mapConcat((request) -> Collection)
            >
}