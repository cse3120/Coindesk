package com.coindesk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

/**
 * @author gopin
 */
public class FetchCoinData {
    private static final Logger LOGGER = LogManager.getLogger(FetchCoinData.class);
    private static final String CURRENT_PRICE_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice/";
    private static final String FETCH_BITCOIN_ENDPOINT = "https://api.coindesk.com/v1/bpi/historical/close.json?start=";
    private static final int HTTP_SUCCESS_STATUS = 200;

    public static void main(String[] args) {
        processFetchData();
    }

    /**
     * process Fetch data.
     */
    protected static void processFetchData() {
        try {
            String currencyCode = getUserInput();
            HttpURLConnection currentPriceHttpURLCon = fetchCurrentPriceAPIHTTPStatus(currencyCode);
            if (currentPriceHttpURLCon.getResponseCode() == HTTP_SUCCESS_STATUS) {
                String message = fetchCurrentPriceSuccessfulData(currencyCode, currentPriceHttpURLCon);
                LOGGER.info(message);
                String fetchAPI = setDateDetailsAndCallHistoricalAPI(currencyCode);
                HttpURLConnection historicalHttpURLCon = fetchHistoricalHTTPStatus(fetchAPI);
                if (historicalHttpURLCon.getResponseCode() == HTTP_SUCCESS_STATUS) {
                    String result = getBufferedReader(historicalHttpURLCon);
                    List<BigDecimal> bigDecimalList = fetchHistoricalData(result);
                    LOGGER.info("The lowest Bitcoin rate in the last 30 days, in the requested currency is: {}", bigDecimalList.get(0));
                    LOGGER.info("The highest Bitcoin rate in the last 30 days, in the requested currency is: {}", bigDecimalList.get(bigDecimalList.size() - 1));
                } else {
                    LOGGER.info("Sorry, that currency was not found");
                }
            } else {
                LOGGER.info("Sorry, your requested currency {} is not supported or is invalid", currencyCode);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while fetching data from API ", e);
        }
    }

    protected static List<BigDecimal> fetchHistoricalData(final String result) {
        try {
            JSONObject fetchBitCoinJsonObj = new JSONObject(result);
            //Get the required object from the above created object
            JSONObject bpiObj = fetchBitCoinJsonObj.getJSONObject("bpi");
            if (!bpiObj.isEmpty()) {
                final Map<String, BigDecimal> sortedMap = getSortedMap(bpiObj).
                        entrySet()
                        .stream()
                        .sorted(comparingByValue())
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
                return new ArrayList<>(sortedMap.values());
            }
        } catch (JSONException jsonException) {
            LOGGER.error("Error while reading JSON data", jsonException);
        }
        return Collections.emptyList();
    }

    protected static HttpURLConnection fetchHistoricalHTTPStatus(final String fetchAPI) throws IOException {
        return getURLConnection(fetchAPI);
    }

    protected static String setDateDetailsAndCallHistoricalAPI(final String currencyCode) {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        return FETCH_BITCOIN_ENDPOINT.concat(startDate.toString()).concat("&end=").concat(endDate.toString()).concat("&currency=").concat(currencyCode);
    }

    /**
     * Fetch current price successful data.
     *
     * @param currencyCode           String
     * @param currentPriceHttpURLCon HttpURLConnection
     * @return String
     */
    protected static String fetchCurrentPriceSuccessfulData(final String currencyCode, final HttpURLConnection currentPriceHttpURLCon) throws IOException {
        try {
            String currentPriceJSON = getBufferedReader(currentPriceHttpURLCon);
            JSONObject currentAPIJSONObject = new JSONObject(currentPriceJSON);
            JSONObject currentBPIObj = currentAPIJSONObject.getJSONObject("bpi");
            JSONObject currencyObj = currentBPIObj.getJSONObject(currencyCode.toUpperCase(Locale.ROOT));
            if (currencyObj.isEmpty()) {
                return "Sorry, your requested currency ".concat(currencyCode).concat(" is not supported or is invalid");
            } else {
                BigDecimal currentRate = currencyObj.getBigDecimal("rate_float");
                String upperCaseCurrencyCode = currencyCode.toUpperCase(Locale.ROOT);
                return "The current Bitcoin rate ".concat(currentRate.toString()).concat(", in the requested currency is : ").concat(upperCaseCurrencyCode);
            }
        } catch (JSONException jsonException) {
            LOGGER.error("Error while reading JSON data", jsonException);
            return jsonException.getMessage();
        }
    }

    protected static HttpURLConnection fetchCurrentPriceAPIHTTPStatus(final String currencyCode) throws IOException {
        String currentBitCoinEndpoint = CURRENT_PRICE_ENDPOINT.concat(currencyCode).concat(".json");
        return getURLConnection(currentBitCoinEndpoint);
    }

    /**
     * Get user input.
     *
     * @return String
     */
    protected static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        LOGGER.info("Enter the currency code");
        return scanner.nextLine();
    }

    /**
     * Sort the map.
     *
     * @param bpiObj JSONObject
     * @return Map<String, BigDecimal>
     */
    private static Map<String, BigDecimal> getSortedMap(final JSONObject bpiObj) {
        //Get the required data using its key
        Map<String, BigDecimal> map = new HashMap<>();
        Iterator<String> iterator = bpiObj.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            BigDecimal value = (BigDecimal) bpiObj.get(key);
            map.put(key, value);
        }
        return map;
    }

    /**
     * Get buffered reader.
     *
     * @param httpURLConnection HttpURLConnection
     * @return String
     */
    private static String getBufferedReader(final HttpURLConnection httpURLConnection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;
        StringBuilder result = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        return result.toString();
    }

    /**
     * Get URL connection.
     *
     * @param currentBitCoinEndpoint HttpURLConnection
     * @return String
     */
    private static HttpURLConnection getURLConnection(final String currentBitCoinEndpoint) throws IOException {
        URL currentBitCoinURL = new URL(currentBitCoinEndpoint);
        HttpURLConnection conn = (HttpURLConnection) currentBitCoinURL.openConnection();
        conn.setRequestMethod("GET");
        return conn;
    }
}