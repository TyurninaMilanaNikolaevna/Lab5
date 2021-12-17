package lab5;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import javafx.util.Pair;

import java.util.Collection;
import java.util.Collections;

public class HttpClient {

    private Sink<Pair<String, Integer>,
        return Flow.<Pair<String,Integer>>create()
            .mapConcat((request) -> Collections.nCopies(request.second(), request.first()))
            .mapAsync(3, (request) -> {
                Long startTime = 
    })
            >
}