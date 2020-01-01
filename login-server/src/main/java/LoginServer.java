import io.netty.buffer.ByteBuf;
import io.rsocket.*;
import io.rsocket.resume.InMemoryResumableFramesStore;
import io.rsocket.resume.ResumableFramesStore;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class LoginServer {
    public static void main(String[] args) throws InterruptedException {
        final Function<? super ByteBuf, ? extends ResumableFramesStore> resumeStoreFactory =
                token -> new InMemoryResumableFramesStore("server", 100_000);
        Duration resumeSessionDuration = Duration.ofSeconds(120);
        Duration resumeStreamTimeout = Duration.ofSeconds(10);
        RSocketFactory.receive()
                .resume()
                .resumeStore(resumeStoreFactory)
                .resumeSessionDuration(resumeSessionDuration)
                .resumeStreamTimeout(resumeStreamTimeout)
                .acceptor(new LoginSocketAcceptor())
                .transport(WebsocketServerTransport.create("192.168.1.6", 9000))
                .start()
                .subscribe();
        Thread.currentThread().join();
    }

    @Slf4j
    static class LoginSocketAcceptor implements SocketAcceptor {
        @Override
        public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
            return Mono.just(new AbstractRSocket() {
                @Override
                public Mono<Void> fireAndForget(Payload payload) {
                    System.out.println("fireAndForget");
                    return Mono.empty();
                }

                @Override
                public Mono<Payload> requestResponse(Payload payload) {
                    System.out.println("requestResponse");
                    return Mono.just(DefaultPayload.create("Hello " + payload.getDataUtf8(), "responder-meta"));
                }

                @Override
                public Flux<Payload> requestStream(Payload payload) {
                    System.out.println("requestStream");
                    return Flux.interval(Duration.ofSeconds(1))
                            .map(time -> DefaultPayload.create("Hello " + payload.getDataUtf8() + " @ " + Instant.now(), "responder-meta"));
                }

                @Override
                public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                    System.out.println("requestChannel");

                    return Flux.from(payloads)
                            .doOnNext(new Consumer<Payload>() {
                                @Override
                                public void accept(Payload payload) {
//                                    System.out.println(payload.getDataUtf8());
                                }
                            })
                            .map(new Function<Payload, Payload>() {

                                @Override
                                public Payload apply(Payload payload) {
                                    System.out.println(payload.getMetadataUtf8());
                                    String dataUtf8 = payload.getDataUtf8();
                                    if (dataUtf8.equals("login")) {
                                        JSONObject jsonObject = new JSONObject(payload.getMetadataUtf8());
                                        String username = jsonObject.getString("username");
                                        String password = jsonObject.getString("password");

                                        if (username.equals("maohuawei") && password.equals("maohuawei")) {
                                            return DefaultPayload.create("res", "success");
                                        }
                                    }
                                    return DefaultPayload.create("res", "err");
                                }
                            }).subscribeOn(Schedulers.parallel());
                }

                @Override
                public Mono<Void> metadataPush(Payload payload) {
                    System.out.println("metadataPush");
                    return Mono.empty();
                }
            });
        }
    }
}