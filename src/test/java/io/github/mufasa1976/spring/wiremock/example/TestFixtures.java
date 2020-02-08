package io.github.mufasa1976.spring.wiremock.example;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class TestFixtures {
  @Data
  @Builder
  public static class SomeContext {
    @Data
    @Builder
    public static class SomeSubContext {
      private int result;
    }

    private SomeSubContext sub;
  }
}
