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

package org.litecoinj.kits;

import org.litecoinj.base.LitecoinNetwork;
import org.litecoinj.base.ScriptType;
import org.litecoinj.core.Context;
import org.litecoinj.wallet.KeyChainGroupStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

/**
 * WalletAppKit Functional/Integration test. Uses {@link LitecoinNetwork#TESTNET} so is {@code @Ignore}d.
 * To run this test comment-out the {@code @Disabled} annotation.
 */
@Disabled
public class WalletAppKitTest {
    static final LitecoinNetwork network = LitecoinNetwork.TESTNET;
    static final int MAX_CONNECTIONS = 3;

    WalletAppKit kit;

    @BeforeEach
    void setupTest(@TempDir File tempDir) {
        Context.propagate(new Context());
        kit = new WalletAppKit(network,
                ScriptType.P2WPKH,
                KeyChainGroupStructure.BIP43,
                tempDir,
                "prefix") {
            @Override
            protected void onSetupCompleted() {
                peerGroup().setMaxConnections(MAX_CONNECTIONS);
            }
        };
    }

    // Construct the kit and immediately stop it
    @Test
    public void constructAndStop() {
        kit.stopAsync();
        kit.awaitTerminated();
    }

    // Construct the kit, start it, and immediately stop it
    @Test
    public void constructStartAndStop() {
        kit.setBlockingStartup(false);
        kit.startAsync();
        kit.awaitRunning();
        kit.stopAsync();
        kit.awaitTerminated();
    }

    // Construct the kit, start it, wait for it to sync, and then stop it
    @Test
    public void constructStartSyncAndStop() {
        // blockStartup is true by default, so this will sync the blockchain before awaitRunning completes
        kit.startAsync();
        kit.awaitRunning();
        kit.stopAsync();
        kit.awaitTerminated();
    }
}
