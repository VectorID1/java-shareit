package ru.practicum.shareit.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpHeadersConstantsTest {

    @Test
    void httpHeadersConstants_coverageTest() throws Exception {
        Constructor<HttpHeadersConstants> constructor = HttpHeadersConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();

        String header = HttpHeadersConstants.USER_ID_HEADER;
        assertNotNull(header);
    }

}