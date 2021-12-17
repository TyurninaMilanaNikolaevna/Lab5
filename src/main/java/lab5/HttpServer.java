package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;
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

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow
                = new src.main.java.AsyncHttpClient(actorSystem).flowHttp(actorMaterializer);

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