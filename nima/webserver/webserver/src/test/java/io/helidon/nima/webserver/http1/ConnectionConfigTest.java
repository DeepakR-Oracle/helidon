/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.nima.webserver.http1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;

import io.helidon.common.buffers.BufferData;
import io.helidon.common.buffers.DataReader;
import io.helidon.common.buffers.DataWriter;
import io.helidon.common.socket.HelidonSocket;
import io.helidon.common.socket.PeerInfo;
import io.helidon.config.Config;
import io.helidon.nima.webserver.ConnectionContext;
import io.helidon.nima.webserver.Router;
import io.helidon.nima.webserver.Routing;
import io.helidon.nima.webserver.ServerConnectionSelector;
import io.helidon.nima.webserver.ServerContext;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.DirectHandlers;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConnectionConfigTest {

    // ConnectionContext mockup
    private static class TestContext implements ConnectionContext {

        @Override
        public PeerInfo remotePeer() {
            return null;
        }

        @Override
        public PeerInfo localPeer() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String socketId() {
            return null;
        }

        @Override
        public String childSocketId() {
            return null;
        }

        @Override
        public ServerContext serverContext() {
            return null;
        }

        @Override
        public ExecutorService sharedExecutor() {
            return null;
        }

        @Override
        public DataWriter dataWriter() {
            return null;
        }

        @Override
        public DataReader dataReader() {
            return new DataReader(new HelidonSocket() {
                @Override
                public void close() {
                }

                @Override
                public int read(BufferData buffer) {
                    return 0;
                }

                @Override
                public void write(BufferData buffer) {
                }

                @Override
                public PeerInfo remotePeer() {
                    return null;
                }

                @Override
                public PeerInfo localPeer() {
                    return null;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public String socketId() {
                    return null;
                }

                @Override
                public String childSocketId() {
                    return null;
                }

                @Override
                public byte[] get() {
                    return new byte[0];
                }
            });
        }

        @Override
        public Router router() {
            return new Router() {
                @Override
                public <T extends Routing> T routing(Class<T> routingType, T defaultValue) {
                    return null;
                }

                @Override
                public void afterStop() {
                }

                @Override
                public void beforeStart() {
                }
            };
        }

        @Override
        public long maxPayloadSize() {
            return 0;
        }

        @Override
        public DirectHandlers directHandlers() {
            return null;
        }

    }

    @Test
    void testConnectionConfig()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // This will pick up application.yaml from the classpath as default configuration file
        Config config = Config.create();

        // Builds LoomServer instance including connectionProviders list.
        WebServer.Builder wsBuilder = WebServer.builder()
                .config(config.get("server"));

        // Call wsBuilder.connectionProviders() trough reflection
        Method connectionProviders
                = WebServer.Builder.class.getDeclaredMethod("connectionProviders", (Class<?>[]) null);
        connectionProviders.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ServerConnectionSelector> providers
                = (List<ServerConnectionSelector>) connectionProviders.invoke(wsBuilder, (Object[]) null);

        // Check whether at least one Http2ConnectionProvider was found
        boolean haveHttp1Provider = false;

        for (ServerConnectionSelector provider : providers) {
            if (provider instanceof Http1ConnectionSelector) {
                haveHttp1Provider = true;
                Http1Connection conn = (Http1Connection) provider.connection(new TestContext());
                // Verify values to be updated from configuration file
                assertThat(conn.config().maxPrologueLength(), is(4096));
                assertThat(conn.config().maxHeadersSize(), is(8192));
                assertThat(conn.config().validatePath(), is(false));
                assertThat(conn.config().validateHeaders(), is(false));
            }
        }
        assertThat("No Http12ConnectionProvider was found", haveHttp1Provider, is(true));
    }

}
