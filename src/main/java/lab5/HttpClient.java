package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.japi.Pair;
import org.asynchttpclient.Dsl;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HttpClient {

    private ActorRef cacheActor;

    HttpClient (ActorSystem actorSystem) {
        cacheActor = actorSystem.actorOf(CacheActor.props(), "cacheActor");
    }

    private Sink<Pair<String, Integer>, CompletionStage<Long>> sink() {
        return Flow.<Pair<String,Integer>>create()
            .mapConcat((request) -> Collections.nCopies(request.second(), request.first()))
            .mapAsync(5, (request) -> {
                Long startTime = System.currentTimeMillis();
                return Dsl.asyncHttpClient()
                        .prepareGet(request)
                        .execute()
                        .toCompletableFuture()
                        .thenCompose((response -> CompletableFuture.completedFuture(System.currentTimeMillis() - startTime)));
                })
            .toMat(Sink.fold(0L, Long::sum), Keep.right());
    }

    final Flow<HttpRequest, HttpResponse, NotUsed> flowHttp(ActorMaterializer actorMaterializer) {
        return Flow
                .of(HttpRequest.class)
                .map(
                        request -> new Pair<>(
                                request.getUri().query().getOrElse("testUrl", ""),
                                Integer.parseInt(request.getUri().query().getOrElse("count", ""))
                    )
                ).mapAsync(5, (request) -> Patterns
                        .ask(cacheActor, request, Duration.ofSeconds(5))
                        .thenCompose((response) -> {
                            if (response.getClass() == String.class) {
                                return Source.from(Collections.singletonList(request))
                                        .toMat(sink(), Keep.right()).run(actorMaterializer)
                                        .thenApply((t) -> new Response(request.first(), t / request.second()));
                            }
                            else return CompletableFuture.completedFuture(response);
                        })
                ).map(param -> {
                    cacheActor.tell(param, ActorRef.noSender());
                    return HttpResponse.create()
                            .withEntity(
                                    HttpEntities.create(
                                            ((Response) param).getResponseTime() + " " + ((Response) param).getHostName()
                                    )
                            );
                });
    }
}