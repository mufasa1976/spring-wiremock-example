package io.github.mufasa1976.spring.wiremock.example;

import com.dneonline.calculator.AddResponse;
import com.dneonline.calculator.DivideResponse;
import com.dneonline.calculator.MultiplyResponse;
import com.dneonline.calculator.SubtractResponse;
import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ws.client.WebServiceTransportException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@ExtendWith(WireMockExtension.class)
@Slf4j
public class CalculatorServiceTest {
  private static final String TEXT_XML_UTF8 = "text/xml; charset=UTF-8";
  private static final String SOAP_ENVELOPE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header/><soapenv:Body>%s</soapenv:Body></soapenv:Envelope>";

  @InjectServer
  WireMockServer wireMock;

  @ConfigureWireMock
  Options options = wireMockConfig().dynamicPort()
                                    .bindAddress("127.0.0.1")
                                    .notifier(new Slf4jNotifier(true));

  private static Marshaller marshaller;
  private CalculatorService calculatorService;

  @BeforeAll
  public static void setUpClass() throws Exception {
    final JAXBContext jaxbContext = JAXBContext.newInstance(AddResponse.class, SubtractResponse.class, MultiplyResponse.class, DivideResponse.class);
    marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
  }

  @BeforeEach
  public void setUp() {
    wireMock.resetAll();
    calculatorService = new CalculatorServiceImpl();
    ((CalculatorServiceImpl) calculatorService).init();
    ((CalculatorServiceImpl) calculatorService).setDefaultUri("http://127.0.0.1:" + wireMock.port());
  }

  @Test
  public void ok_add() throws Exception {
    final AddResponse response = new AddResponse();
    response.setAddResult(5);
    stubFor(post("/").withHeader(CONTENT_TYPE, equalTo(TEXT_XML_UTF8))
                     .withHeader("SOAPAction", equalTo("\"http://tempuri.org/Add\""))
                     .willReturn(aResponse().withBody(soapEnveloped(response))));
    assertThat(calculatorService.add(2, 3)).isEqualTo(5);
  }

  private String soapEnveloped(Object object) throws IOException, JAXBException {
    try (final StringWriter writer = new StringWriter()) {
      marshaller.marshal(object, writer);
      return String.format(SOAP_ENVELOPE, writer.toString());
    }
  }

  @Test
  public void nok_add_serverError() throws Exception {
    stubFor(post("/").withHeader(CONTENT_TYPE, equalTo(TEXT_XML_UTF8))
                     .withHeader("SOAPAction", equalTo("\"http://tempuri.org/Add\""))
                     .willReturn(aResponse().withStatus(500)));
    try {
      calculatorService.add(1, 1);
      fail("%s should be thrown", WebServiceTransportException.class);
    } catch (WebServiceTransportException e) {
      assertThat(e).hasMessage("Server Error [500]");
    }
  }

  @Test
  public void ok_subtract() throws Exception {
    final SubtractResponse response = new SubtractResponse();
    response.setSubtractResult(2);
    stubFor(post("/").withHeader(CONTENT_TYPE, equalTo(TEXT_XML_UTF8))
                     .withHeader("SOAPAction", equalTo("\"http://tempuri.org/Subtract\""))
                     .willReturn(aResponse().withBody(soapEnveloped(response))));
    assertThat(calculatorService.subtract(5, 3)).isEqualTo(2);
  }
}
