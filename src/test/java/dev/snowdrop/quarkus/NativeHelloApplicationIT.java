package dev.snowdrop.quarkus;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeHelloApplicationIT extends HelloApplicationTest {

    // Execute the same tests but in native mode.
}