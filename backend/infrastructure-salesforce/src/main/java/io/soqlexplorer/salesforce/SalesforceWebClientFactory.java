package io.soqlexplorer.salesforce;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Builds {@link WebClient} instances for Salesforce calls.
 *
 * <p>Two flavours are needed:
 *
 * <ul>
 *   <li>An unauthenticated client used to POST to {@code /services/oauth2/token} on the SF login
 *       host (production or sandbox).
 *   <li>A per-connection client bound to the connection's {@code instanceUrl} that injects the
 *       refreshed access token as a Bearer header on each call.
 * </ul>
 */
@Component
public class SalesforceWebClientFactory {

  private final SalesforceProperties props;

  public SalesforceWebClientFactory(SalesforceProperties props) {
    this.props = Objects.requireNonNull(props, "props");
  }

  public WebClient base(String baseUrl) {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
        .build();
  }

  private HttpClient buildHttpClient() {
    int timeoutMillis = (int) props.getHttpTimeout().toMillis();
    return HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMillis)
        .doOnConnected(
            conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeoutMillis, TimeUnit.MILLISECONDS)));
  }
}
