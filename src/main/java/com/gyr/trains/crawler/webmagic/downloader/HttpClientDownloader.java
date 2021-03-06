package com.gyr.trains.crawler.webmagic.downloader;

import com.gyr.trains.crawler.webmagic.Page;
import com.gyr.trains.crawler.webmagic.Request;
import com.gyr.trains.crawler.webmagic.Site;
import com.gyr.trains.crawler.webmagic.Task;
import com.gyr.trains.crawler.webmagic.proxy.Proxy;
import com.gyr.trains.crawler.webmagic.proxy.ProxyProvider;
import com.gyr.trains.crawler.webmagic.selector.PlainText;
import com.gyr.trains.crawler.webmagic.utils.CharsetUtils;
import com.gyr.trains.crawler.webmagic.utils.HttpClientUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * The http downloader based on HttpClient.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@Component
public class HttpClientDownloader extends AbstractDownloader {

    private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpClientGenerator httpClientGenerator = new HttpClientGenerator();
    private final boolean responseHeader = true;
    private HttpUriRequestConverter httpUriRequestConverter = new HttpUriRequestConverter();
    private ProxyProvider proxyProvider;

    public void setHttpUriRequestConverter(HttpUriRequestConverter httpUriRequestConverter) {
        this.httpUriRequestConverter = httpUriRequestConverter;
    }

    public void setProxyProvider(ProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }

    private CloseableHttpClient getHttpClient(Site site) {
        if (site == null) {
            return httpClientGenerator.getClient(null);
        }
        String domain = site.getDomain();
        CloseableHttpClient httpClient = httpClients.get(domain);
        if (httpClient == null) {
            synchronized (this) {
                httpClient = httpClients.get(domain);
                if (httpClient == null) {
                    httpClient = httpClientGenerator.getClient(site);
                    httpClients.put(domain, httpClient);
                }
            }
        }
        return httpClient;
    }

//    private Proxy getProxy() {
//        String body = "";
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpGet httpGet = new HttpGet("http://localhost:5010/pop");
//        try {
//            CloseableHttpResponse response = client.execute(httpGet);
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                body = EntityUtils.toString(entity, "utf-8");
//            }
//            EntityUtils.consume(entity);
//            Thread.sleep(2000);
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
//        JSONObject jsonObject = JSON.parseObject(body);
//        if (jsonObject == null) return null;
//        String proxyString = jsonObject.getString("proxy");
//        if (proxyString == null) return null;
//        String host = proxyString.substring(0, proxyString.indexOf(":"));
//        int port = Integer.parseInt(proxyString.substring(proxyString.indexOf(":") + 1));
//        logger.info("?????????ip: " + host + ":" + port);
//        return new Proxy(host, port);
//    }

    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site can not be null");
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = getHttpClient(task.getSite());
        Proxy proxy = proxyProvider != null ? proxyProvider.getProxy(task) : null;
//        Proxy proxy = getProxy();
        HttpClientRequestContext requestContext = httpUriRequestConverter.convert(request, task.getSite(), proxy);
        Page page = Page.fail();
        try {
            httpResponse = httpClient.execute(requestContext.getHttpUriRequest(), requestContext.getHttpClientContext());
            page = handleResponse(request, request.getCharset() != null ? request.getCharset() : task.getSite().getCharset(), httpResponse, task);
            onSuccess(request);
//            logger.info("downloading page success {}", request.getUrl());
            return page;
        } catch (IOException e) {
//            logger.warn("download page {} error", request.getUrl(), e);
            onError(request);
            return page;
        } finally {
            if (httpResponse != null) {
                //ensure the connection is released back to pool
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
            if (proxyProvider != null && proxy != null) {
                proxyProvider.returnProxy(proxy, page, task);
            }
        }
    }

    @Override
    public void setThread(int thread) {
        httpClientGenerator.setPoolSize(thread);
    }

    protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
        byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
        String contentType = httpResponse.getEntity().getContentType() == null ? "" : httpResponse.getEntity().getContentType().getValue();
        Page page = new Page();
        page.setBytes(bytes);
        if (!request.isBinaryContent()) {
            if (charset == null) {
                charset = getHtmlCharset(contentType, bytes);
            }
            page.setCharset(charset);
            page.setRawText(new String(bytes, charset));
        }
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        page.setDownloadSuccess(true);
        if (responseHeader) {
            page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
        }
        return page;
    }

    private String getHtmlCharset(String contentType, byte[] contentBytes) throws IOException {
        String charset = CharsetUtils.detectCharset(contentType, contentBytes);
        if (charset == null) {
            charset = Charset.defaultCharset().name();
            logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
        }
        return charset;
    }
}
