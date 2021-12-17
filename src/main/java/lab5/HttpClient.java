package lab5;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import javafx.util.Pair;
import org.asynchttpclient.Dsl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class HttpClient {

    private Sink<Pair<String, Integer>,
        return Flow.<Pair<String,Integer>>create()
            .mapConcat((request) -> Collections.nCopies(request.second(), request.first()))
            .mapAsync(3, (request) -> {
                Long startTime = System.currentTimeMillis();
                return Dsl.asyncHttpClient()
                        .prepareGet(request)
                        .execute()
                        .toCompletableFuture()
                        .thenCompose((response -> CompletableFuture.completedFuture(System.currentTimeMillis() - startTime)))
                })
            .toMat(Sink.fold())
            >
}