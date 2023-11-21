/*
 * Copyright by the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litecoinj.base;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BitcoinNetworkTest {
    @Test
    public void valueOf() {
        assertEquals(LitecoinNetwork.MAINNET, LitecoinNetwork.valueOf("MAINNET"));
        assertEquals(LitecoinNetwork.TESTNET, LitecoinNetwork.valueOf("TESTNET"));
        assertEquals(LitecoinNetwork.SIGNET, LitecoinNetwork.valueOf("SIGNET"));
        assertEquals(LitecoinNetwork.REGTEST, LitecoinNetwork.valueOf("REGTEST"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOf_alternate() {
        LitecoinNetwork.valueOf("PROD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOf_notExisting() {
        LitecoinNetwork.valueOf("xxx");
    }

    @Test
    public void fromString() {
        assertEquals(LitecoinNetwork.MAINNET, LitecoinNetwork.fromString("mainnet").get());
        assertEquals(LitecoinNetwork.MAINNET, LitecoinNetwork.fromString("main").get());
        assertEquals(LitecoinNetwork.MAINNET, LitecoinNetwork.fromString("prod").get());
        assertEquals(LitecoinNetwork.TESTNET, LitecoinNetwork.fromString("test").get());
        assertEquals(LitecoinNetwork.TESTNET, LitecoinNetwork.fromString("testnet").get());
        assertEquals(LitecoinNetwork.SIGNET, LitecoinNetwork.fromString("signet").get());
        assertEquals(LitecoinNetwork.SIGNET, LitecoinNetwork.fromString("sig").get());
        assertEquals(LitecoinNetwork.REGTEST, LitecoinNetwork.fromString("regtest").get());
    }

    @Test
    public void fromString_uppercase() {
        assertFalse(LitecoinNetwork.fromString("MAIN").isPresent());
    }

    @Test
    public void fromString_notExisting() {
        assertFalse(LitecoinNetwork.fromString("xxx").isPresent());
    }

    @Test
    public void fromIdString() {
        assertEquals(LitecoinNetwork.MAINNET, LitecoinNetwork.fromIdString("org.litecoin.production").get());
        assertEquals(LitecoinNetwork.TESTNET, LitecoinNetwork.fromIdString("org.litecoin.test").get());
        assertEquals(LitecoinNetwork.SIGNET, LitecoinNetwork.fromIdString("org.litecoin.signet").get());
        assertEquals(LitecoinNetwork.REGTEST, LitecoinNetwork.fromIdString("org.litecoin.regtest").get());
    }

    @Test
    public void fromIdString_uppercase() {
        assertFalse(LitecoinNetwork.fromIdString("ORG.LITECOIN.PRODUCTION").isPresent());
    }

    @Test
    public void fromIdString_notExisting() {
        assertFalse(LitecoinNetwork.fromIdString("a.b.c").isPresent());
    }
}
