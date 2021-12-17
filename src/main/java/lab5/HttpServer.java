package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;
import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class HttpServer {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private ActorRef routeActor;

    private HttpServer(ActorRef routeActor) {
        this.routeActor = routeActor;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem actorSystem = ActorSystem.create("routes");

        final Http http = Http.get(actorSystem);
        final ActorMaterializer actorMaterializer = ActorMaterializer.create(actorSystem);

        final AsyncHttpClient asyncHttpClient = asyncHttpClient();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = Flow
                .of(HttpRequest.class)
                .map(
                        request -> new Pair<>(
                                request.getUri().query().getOrElse("testUrl", ""),
                                Integer.parseInt(request.getUri().query().getOrElse("count", ""))
                        )
                ).mapAsync(3, (request) -> Patterns
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

        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(HOST, PORT),
                actorMaterializer
        );

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();

        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done

        try {
            asyncHttpClient.close();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }
}