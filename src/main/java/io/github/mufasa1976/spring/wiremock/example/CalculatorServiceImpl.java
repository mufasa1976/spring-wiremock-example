package io.github.mufasa1976.spring.wiremock.example;

import com.dneonline.calculator.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl extends WebServiceGatewaySupport implements CalculatorService {
  public static final String WEBSERVICE_LOCATION = "http://www.dneonline.com/calculator.asmx";

  public static final String ADD_ACTION = "http://tempuri.org/Add";
  public static final String SUBTRACT_ACTION = "http://tempuri.org/Subtract";
  public static final String MULTIPLY_ACTION = "http://tempuri.org/Multiply";
  public static final String DIVIDE_ACTION = "http://tempuri.org/Divide";

  private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

  @PostConstruct
  public void init() {
    setDefaultUri(WEBSERVICE_LOCATION);
    setMessageSender(new HttpComponentsMessageSender());

    Jaxb2Marshaller jaxb2Marshaller = createMarshaller();
    setMarshaller(jaxb2Marshaller);
    setUnmarshaller(jaxb2Marshaller);
  }

  @Override
  protected void initGateway() throws Exception {
    super.initGateway();
    getWebServiceTemplate().setFaultMessageResolver(this::resolveFault);
  }

  private void resolveFault(WebServiceMessage message) {
    throw Optional.of(message)
                  .filter(SoapMessage.class::isInstance)
                  .map(SoapMessage.class::cast)
                  .map(SoapMessage::getSoapBody)
                  .map(SoapBody::getFault)
                  .map(SoapFault::getFaultStringOrReason)
                  .map(ArithmeticException::new)
                  .orElseGet(ArithmeticException::new);
  }

  private static Jaxb2Marshaller createMarshaller() {
    try {
      Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
      marshaller.setContextPath(OBJECT_FACTORY.getClass().getPackageName());
      marshaller.afterPropertiesSet();
      return marshaller;
    } catch (Exception e) {
      throw new IllegalStateException("Error while creating Jaxb2Marshaller for Package " + OBJECT_FACTORY.getClass().getPackageName(), e);
    }
  }

  @Override
  public int add(int augend, int addend) {
    final Add request = OBJECT_FACTORY.createAdd();
    request.setIntA(augend);
    request.setIntB(addend);
    return sendOperation(request, AddResponse.class, AddResponse::getAddResult, ADD_ACTION);
  }

  private <REQUEST, RESPONSE> int sendOperation(REQUEST request, Class<RESPONSE> targetClass, Function<RESPONSE, Integer> extractor, String soapAction) {
    return Optional.ofNullable(getWebServiceTemplate().marshalSendAndReceive(request, message -> Optional.ofNullable(soapAction)
                                                                                                         .filter(StringUtils::isNotBlank)
                                                                                                         .ifPresent(((SoapMessage) message)::setSoapAction)))
                   .filter(targetClass::isInstance)
                   .map(targetClass::cast)
                   .map(extractor)
                   .orElseThrow(() -> new NullPointerException(String.format("Error while executing SOAP-Action %s", soapAction)));
  }

  @Override
  public int subtract(int minuend, int subtrahend) {
    final Subtract request = OBJECT_FACTORY.createSubtract();
    request.setIntA(minuend);
    request.setIntB(subtrahend);
    return sendOperation(request, SubtractResponse.class, SubtractResponse::getSubtractResult, SUBTRACT_ACTION);
  }

  @Override
  public int multiply(int multiplier, int multiplicand) {
    final Multiply request = OBJECT_FACTORY.createMultiply();
    request.setIntA(multiplier);
    request.setIntB(multiplicand);
    return sendOperation(request, MultiplyResponse.class, MultiplyResponse::getMultiplyResult, MULTIPLY_ACTION);
  }

  @Override
  public int divide(int dividend, int divisor) {
    final Divide request = OBJECT_FACTORY.createDivide();
    request.setIntA(dividend);
    request.setIntB(divisor);
    return sendOperation(request, DivideResponse.class, DivideResponse::getDivideResult, DIVIDE_ACTION);
  }
}
