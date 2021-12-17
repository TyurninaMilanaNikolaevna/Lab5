package lab5;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem actorSystem = ActorSystem.create("routes");

        final Http http = Http.get(actorSystem);
        final ActorMaterializer actorMaterializer = ActorMaterializer.create(actorSystem);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow =
                new src.main.java.AsyncHttpClient(actorSystem).flowHttp(actorMaterializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", 8080),
                actorMaterializer
        );
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> actorSystem.terminate()); // and shutdown when done
    }
}