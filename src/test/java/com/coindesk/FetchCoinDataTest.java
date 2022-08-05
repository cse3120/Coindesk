package com.coindesk;

import nl.altindag.log.LogCaptor;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FetchCoinDataTest {

    String fetchAPIEndpoint = "";

    @BeforeEach
    void setUp() {
        fetchAPIEndpoint = "https://api.coindesk.com/v1/bpi/historical/close.json?start=" + LocalDate.now().minusDays(30) + "&end=" + LocalDate.now() + "&currency=eur";
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testValidUserInput() {
        String userInput ="eur";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(userInput.getBytes());
        System.setIn(byteArrayInputStream);
        LogCaptor logCaptor=LogCaptor.forClass(FetchCoinData.class);
        String expected = "Enter the currency code";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);
        FetchCoinData.getUserInput();
        assertThat(logCaptor.getLogs()).hasSize(1).contains(expected);
    }

    @Test
    void testProcessFetchSuccessFul() {
        String userInput = "eur";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(userInput.getBytes());
        System.setIn(byteArrayInputStream);
        LogCaptor logCaptor=LogCaptor.forClass(FetchCoinData.class);
        String expected = "The highest Bitcoin rate in the last 30 days, in the requested currency is: 20745.4181";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);
        FetchCoinData.main(new String[0]);
        assertThat(logCaptor.getLogs()).hasSize(4).contains(expected);
    }

    @Test
    void testProcessFetchFailure() {
        String userInput = "tyr";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(userInput.getBytes());
        System.setIn(byteArrayInputStream);
        String expected = "Sorry, your requested currency tyr is not supported or is invalid";
        LogCaptor logCaptor=LogCaptor.forClass(FetchCoinData.class);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);
        FetchCoinData.processFetchData();
        assertThat(logCaptor.getLogs()).hasSize(2).contains(expected);
    }

    @Test
    void testValidCurrentPriceURL() throws IOException {
        HttpURLConnection connection = FetchCoinData.fetchCurrentPriceAPIHTTPStatus("eur");
        Assertions.assertEquals(200, connection.getResponseCode());
    }

    @Test
    void testInValidCurrentPriceURL() throws IOException {
        HttpURLConnection connection = FetchCoinData.fetchCurrentPriceAPIHTTPStatus("tyr");
        Assertions.assertEquals(404, connection.getResponseCode());
    }

    @Test
    void testCurrentPriceSuccessfulData() throws IOException {
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        InputStream anyInputStream = new ByteArrayInputStream("{\"time\":{\"updated\":\"Aug 4, 2022 13:57:00 UTC\",\"updatedISO\":\"2022-08-04T13:57:00+00:00\",\"updateduk\":\"Aug 4, 2022 at 14:57 BST\"},\"disclaimer\":\"This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org\",\"bpi\":{\"USD\":{\"code\":\"USD\",\"rate\":\"23,088.6224\",\"description\":\"United States Dollar\",\"rate_float\":23088.6224},\"EUR\":{\"code\":\"EUR\",\"rate\":\"22,491.6891\",\"description\":\"Euro\",\"rate_float\":22491.6891}}}".getBytes());
        when(mockHttpURLConnection.getRequestMethod()).thenReturn("GET");
        when(mockHttpURLConnection.getInputStream()).thenReturn(anyInputStream);
        JSONObject mockJson = mock(JSONObject.class);
        JSONObject jsonString = mock(JSONObject.class);
        when(mockJson.getJSONObject("bpi")).thenReturn(jsonString);
        String message = FetchCoinData.fetchCurrentPriceSuccessfulData("eur", mockHttpURLConnection);
        Assertions.assertEquals("The current Bitcoin rate 22491.6891, in the requested currency is : EUR",message);
    }

    @Test
    void testCurrentPriceInvalidData() throws IOException {
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        InputStream anyInputStream = new ByteArrayInputStream("Test".getBytes());
        when(mockHttpURLConnection.getRequestMethod()).thenReturn("GET");
        when(mockHttpURLConnection.getInputStream()).thenReturn(anyInputStream);
        JSONObject mockJson = mock(JSONObject.class);
        JSONObject jsonString = mock(JSONObject.class);
        when(mockJson.getJSONObject("bpi")).thenReturn(jsonString);
        String message = FetchCoinData.fetchCurrentPriceSuccessfulData("eur", mockHttpURLConnection);
        Assertions.assertEquals("A JSONObject text must begin with '{' at 1 [character 2 line 1]",message);
    }

    @Test
    void testCurrentPriceFailureData() throws IOException {
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        InputStream anyInputStream = new ByteArrayInputStream("{\"time\":{\"updated\":\"Aug 4, 2022 13:57:00 UTC\",\"updatedISO\":\"2022-08-04T13:57:00+00:00\",\"updateduk\":\"Aug 4, 2022 at 14:57 BST\"},\"disclaimer\":\"This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org\",\"bpi\":{\"Test\":\"Test\"}}".getBytes());
        when(mockHttpURLConnection.getRequestMethod()).thenReturn("GET");
        when(mockHttpURLConnection.getInputStream()).thenReturn(anyInputStream);
        JSONObject mockJson = mock(JSONObject.class);
        JSONObject jsonString = mock(JSONObject.class);
        when(mockJson.getJSONObject("bpi")).thenReturn(jsonString);
        String message = FetchCoinData.fetchCurrentPriceSuccessfulData("tyr", mockHttpURLConnection);
        Assertions.assertEquals("JSONObject[\"TYR\"] not found.",message);
    }
    @Test
    void testCurrentPriceEmptyData() throws IOException {
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        InputStream anyInputStream = new ByteArrayInputStream("{\"time\":{\"updated\":\"Aug 4, 2022 13:57:00 UTC\",\"updatedISO\":\"2022-08-04T13:57:00+00:00\",\"updateduk\":\"Aug 4, 2022 at 14:57 BST\"},\"disclaimer\":\"This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org\",\"bpi\":{\"USD\":{\"code\":\"USD\",\"rate\":\"23,088.6224\",\"description\":\"United States Dollar\",\"rate_float\":23088.6224},\"EUR\":{}}}".getBytes());
        when(mockHttpURLConnection.getRequestMethod()).thenReturn("GET");
        when(mockHttpURLConnection.getInputStream()).thenReturn(anyInputStream);
        JSONObject mockJson = mock(JSONObject.class);
        JSONObject jsonString = mock(JSONObject.class);
        when(mockJson.getJSONObject("bpi")).thenReturn(jsonString);
        String message = FetchCoinData.fetchCurrentPriceSuccessfulData("eur", mockHttpURLConnection);
        Assertions.assertEquals("Sorry, your requested currency eur is not supported or is invalid",message);
    }
    @Test
    void testSetDataDetails() {
        String endPoint = FetchCoinData.setDateDetailsAndCallHistoricalAPI("eur");
        Assertions.assertEquals(fetchAPIEndpoint, endPoint);
    }

    @Test
    void testValidHistoricalURL() throws IOException {
        HttpURLConnection connection = FetchCoinData.fetchHistoricalHTTPStatus(fetchAPIEndpoint);
        Assertions.assertEquals(200, connection.getResponseCode());
    }

    @Test
    void testInValidHistoricalURL() throws IOException {
        HttpURLConnection connection = FetchCoinData.fetchCurrentPriceAPIHTTPStatus("tyr");
        Assertions.assertEquals(404, connection.getResponseCode());
    }

    @Test
    void testValidHistoricalData() {
        String json = "{\"bpi\":{\"2022-07-05\":19364.2864,\"2022-07-06\":19415.5529,\"2022-07-10\":20745.4181},\"disclaimer\":\"This data was produced from the CoinDesk Bitcoin Price Index. BPI value data returned as EUR.\",\"time\":{\"updated\":\"Aug 4, 2022 14:24:46 UTC\",\"updatedISO\":\"2022-08-04T14:24:46+00:00\"}}";
        List<BigDecimal> bigDecimalList = FetchCoinData.fetchHistoricalData(json);
        Assertions.assertEquals(3, bigDecimalList.size());
    }

    @Test
    void testInValidHistoricalData() {
        String json = "Test";
        List<BigDecimal> bigDecimalList = FetchCoinData.fetchHistoricalData(json);
        Assertions.assertEquals(0, bigDecimalList.size());
    }
}