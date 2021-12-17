package lab5;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import javafx.util.Pair;
import org.asynchttpclient.Dsl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HttpClient {

    private Sink<Pair<String, Integer>, CompletionStage<Long>>
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
            .toMat(Sink.fold(0L, Long::sum), Keep.right());
            }
}