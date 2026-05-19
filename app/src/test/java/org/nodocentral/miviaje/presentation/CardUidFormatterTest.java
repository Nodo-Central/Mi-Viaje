package org.nodocentral.miviaje.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CardUidFormatterTest {
    @Test
    public void formatUid_whenHidden_showsOnlyLastFourCharacters() {
        assertEquals("\u2022\u2022\u2022\u20222390", CardUidFormatter.formatUid("046587C2192390", true));
    }

    @Test
    public void formatUid_whenVisible_returnsFullUid() {
        assertEquals("046587C2192390", CardUidFormatter.formatUid("046587C2192390", false));
    }

    @Test
    public void formatUid_whenShortUid_returnsOriginalValue() {
        assertEquals("2390", CardUidFormatter.formatUid("2390", true));
    }

    @Test
    public void formatUid_whenNull_returnsNull() {
        assertNull(CardUidFormatter.formatUid(null, true));
    }
}
