package io.github.mufasa1976.spring.wiremock.example;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("calculator")
@Validated
public class CalculatorRestController {
  private final CalculatorService calculatorService;

  @GetMapping("add/{augend}/{addend}")
  public int add(@PathVariable("augend") int augend, @PathVariable("addend") int addend) {
    return calculatorService.add(augend, addend);
  }

  @GetMapping("subtract/{minuend}/{subtrahend}")
  public int subtract(@PathVariable("minuend") int minuend, @PathVariable("subtrahend") int subtrahend) {
    return calculatorService.subtract(minuend, subtrahend);
  }

  @GetMapping("multiply/{multiplier}/{multiplicand}")
  public int multiply(@PathVariable("multiplier") int mulitplier, @PathVariable("multiplicand") int multiplicand) {
    return calculatorService.multiply(mulitplier, multiplicand);
  }

  @GetMapping("divide/{dividend}/{divisor}")
  public int divide(@PathVariable("dividend") int dividend, @PathVariable("divisor") int divisor) {
    return calculatorService.divide(dividend, divisor);
  }
}
