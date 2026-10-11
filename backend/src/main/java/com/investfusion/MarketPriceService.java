package com.investfusion;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.regex.*;

@Service
public class MarketPriceService {
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    public double fetch(String symbol) {
        if(symbol==null||symbol.isBlank()) return 0;
        try {
            String url="https://query1.finance.yahoo.com/v8/finance/chart/"+java.net.URLEncoder.encode(symbol,java.nio.charset.StandardCharsets.UTF_8)+"?range=1d&interval=1m";
            HttpRequest req=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).header("User-Agent","Mozilla/5.0").GET().build();
            HttpResponse<String> res=client.send(req,HttpResponse.BodyHandlers.ofString());
            if(res.statusCode()!=200)return 0;
            Matcher m=Pattern.compile("\"regularMarketPrice\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)").matcher(res.body());
            return m.find()?Double.parseDouble(m.group(1)):0;
        } catch(Exception e){return 0;}
    }
}
