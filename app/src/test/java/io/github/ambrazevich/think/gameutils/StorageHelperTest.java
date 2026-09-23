package io.github.ambrazevich.think.gameutils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StorageHelperTest {

    @Test
    public void historyHasABoundedSize() {
        assertEquals(100, StorageHelper.MAX_RESULTS);
    }
}
