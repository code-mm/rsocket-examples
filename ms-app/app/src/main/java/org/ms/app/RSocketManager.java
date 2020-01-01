package org.ms.app;

import org.ms.module.supper.client.Modules;
import org.ms.module.supper.inter.common.ICallBack;

import java.util.function.Consumer;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import reactor.core.publisher.Mono;

public class RSocketManager {


    private static RSocketManager instance;

    public static RSocketManager getInstance() {
        if (instance == null) {
            synchronized (RSocketManager.class) {
                instance = new RSocketManager();
            }
        }
        return instance;
    }


    private  final RSocketTcp mRSocketTcp = new RSocketTcp();
    private  final RSocketWebSocket mRSocketWebSocket = new RSocketWebSocket();


    public  RSocketTcp getRSocketTcp() {
        return mRSocketTcp;
    }

    public  RSocketWebSocket getRSocketWebSocket() {
        return mRSocketWebSocket;
    }

    interface IRSocket {
        void connect(String host, int port, ICallBack callBack);
    }


     static final class RSocketTcp implements IRSocket {
        private RSocket mRsocket;
        private TcpClientTransport mTcpClientTransport;

        @Override
        public void connect(String host, int port, ICallBack callBack) {

            mTcpClientTransport = TcpClientTransport.create(host, port);


            Modules.getUtilsModule().getThreadPoolUtils().runSubThread(new Runnable() {
                @Override
                public void run() {

                    Mono<RSocket> socket = RSocketFactory.connect()
                            .errorConsumer(new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {
                                    if (callBack != null) {
                                        callBack.onException(throwable);
                                    }
                                }
                            })
                            .keepAlive()
                            .transport(mTcpClientTransport)

                            .start();
                    socket.blockOptional()
                            .ifPresent(new Consumer<RSocket>() {
                                @Override
                                public void accept(RSocket rSocket) {
                                    mRsocket = rSocket;
                                    if (callBack != null) {
                                        callBack.onSuccess(rSocket);
                                    }
                                }
                            });
                }
            });


        }
    }


     static final class RSocketWebSocket implements IRSocket {
        private RSocket mRsocket;
        private WebsocketClientTransport mWebsocketClientTransport;


        @Override
        public void connect(String host, int port, ICallBack callBack) {
            mWebsocketClientTransport = WebsocketClientTransport.create(host, port);
            Modules.getUtilsModule().getThreadPoolUtils().runSubThread(new Runnable() {
                @Override
                public void run() {
                    Mono<RSocket> socket = RSocketFactory.connect()
                            .errorConsumer(new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {
                                    if (callBack != null) {
                                        callBack.onException(throwable);
                                    }
                                }
                            })
                            .keepAlive()
                            .transport(mWebsocketClientTransport)
                            .start();
                    try {
                        socket.blockOptional()
                                .ifPresent(new Consumer<RSocket>() {
                                    @Override
                                    public void accept(RSocket rSocket) {
                                        mRsocket = rSocket;
                                        if (callBack != null) {
                                            callBack.onSuccess(rSocket);
                                        }
                                    }
                                });
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        if (callBack != null) {
                            callBack.onException(throwable);
                        }
                    }
                }
            });

        }
    }
}