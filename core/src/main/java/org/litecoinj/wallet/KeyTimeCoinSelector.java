/*
 * Copyright 2013 Google Inc.
 * Copyright 2019 Andreas Schildbach
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

package org.litecoinj.wallet;

import org.litecoinj.base.Coin;
import org.litecoinj.base.ScriptType;
import org.litecoinj.crypto.ECKey;
import org.litecoinj.core.Transaction;
import org.litecoinj.core.TransactionConfidence;
import org.litecoinj.core.TransactionOutput;
import org.litecoinj.script.Script;
import org.litecoinj.script.ScriptException;
import org.litecoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A coin selector that takes all coins assigned to keys created before the given timestamp.
 * Used as part of the implementation of {@link Wallet#setKeyRotationTime(java.time.Instant)}.
 */
public class KeyTimeCoinSelector implements CoinSelector {
    private static final Logger log = LoggerFactory.getLogger(KeyTimeCoinSelector.class);

    /** A number of inputs chosen to avoid hitting {@link Transaction#MAX_STANDARD_TX_SIZE} */
    public static final int MAX_SIMULTANEOUS_INPUTS = 600;

    private final Instant time;
    private final Wallet wallet;
    private final boolean ignorePending;

    public KeyTimeCoinSelector(Wallet wallet, Instant time, boolean ignorePending) {
        this.time = Objects.requireNonNull(time);
        this.wallet = wallet;
        this.ignorePending = ignorePending;
    }

    /** @deprecated use {@link #KeyTimeCoinSelector(Wallet, Instant, boolean)} */
    @Deprecated
    public KeyTimeCoinSelector(Wallet wallet, long timeSecs, boolean ignorePending) {
        this(wallet, Instant.ofEpochSecond(timeSecs), ignorePending);
    }

    @Override
    public CoinSelection select(Coin target, List<TransactionOutput> candidates) {
        try {
            LinkedList<TransactionOutput> gathered = new LinkedList<>();
            for (TransactionOutput output : candidates) {
                if (ignorePending && !isConfirmed(output))
                    continue;
                // Find the key that controls output, assuming it's a regular P2PK or P2PKH output.
                // We ignore any other kind of exotic output on the assumption we can't spend it ourselves.
                final Script scriptPubKey = output.getScriptPubKey();
                ECKey controllingKey;
                if (ScriptPattern.isP2PK(scriptPubKey)) {
                    controllingKey = wallet.findKeyFromPubKey(ScriptPattern.extractKeyFromP2PK(scriptPubKey));
                } else if (ScriptPattern.isP2PKH(scriptPubKey)) {
                    controllingKey = wallet.findKeyFromPubKeyHash(ScriptPattern.extractHashFromP2PKH(scriptPubKey), ScriptType.P2PKH);
                } else if (ScriptPattern.isP2WPKH(scriptPubKey)) {
                    controllingKey = wallet.findKeyFromPubKeyHash(ScriptPattern.extractHashFromP2WH(scriptPubKey), ScriptType.P2WPKH);
                } else {
                    log.info("Skipping tx output {} because it's not of simple form.", output);
                    continue;
                }
                Objects.requireNonNull(controllingKey, "Coin selector given output as candidate for which we lack the key");
                if (controllingKey.creationTime().orElse(Instant.EPOCH).compareTo(time) >= 0) continue;
                // It's older than the cutoff time so select.
                gathered.push(output);
                if (gathered.size() >= MAX_SIMULTANEOUS_INPUTS) {
                    log.warn("Reached {} inputs, going further would yield a tx that is too large, stopping here.", gathered.size());
                    break;
                }
            }
            return new CoinSelection(gathered);
        } catch (ScriptException e) {
            throw new RuntimeException(e);  // We should never have problems understanding scripts in our wallet.
        }
    }

    private boolean isConfirmed(TransactionOutput output) {
        return output.getParentTransaction().getConfidence().getConfidenceType().equals(TransactionConfidence.ConfidenceType.BUILDING);
    }
}
