/*
 * Copyright 2014 devrandom
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

import org.litecoinj.base.ScriptType;
import org.litecoinj.crypto.ChildNumber;
import org.litecoinj.crypto.DeterministicKey;
import org.litecoinj.crypto.KeyCrypter;

import java.util.List;

import static org.litecoinj.base.internal.Preconditions.check;

/**
 * Factory interface for creation keychains while de-serializing a wallet.
 */
public interface KeyChainFactory {
    /**
     * Make a keychain (but not a watching one) with the specified account path
     *
     * @param seed             the seed
     * @param crypter          the encrypted/decrypter
     * @param outputScriptType type of addresses (aka output scripts) to generate for receiving
     * @param accountPath      account path to generate receiving addresses on
     */
    DeterministicKeyChain makeKeyChain(DeterministicSeed seed, KeyCrypter crypter,
                                       ScriptType outputScriptType, List<ChildNumber> accountPath);

    /** @deprecated use {@link #makeKeyChain(DeterministicSeed, KeyCrypter, ScriptType, List)} */
    @Deprecated
    default DeterministicKeyChain makeKeyChain(DeterministicSeed seed, KeyCrypter crypter, boolean isMarried,
                                               ScriptType outputScriptType, List<ChildNumber> accountPath) {
        check(!isMarried, () -> { throw new UnsupportedOperationException("married wallets not supported"); });
        return makeKeyChain(seed, crypter, outputScriptType, accountPath);
    }

    /**
     * Make a watching keychain.
     *
     * <p>isMarried and isFollowingKey must not be true at the same time.
     *
     * @param accountKey       the account extended public key
     * @param outputScriptType type of addresses (aka output scripts) to generate for watching
     */
    DeterministicKeyChain makeWatchingKeyChain(DeterministicKey accountKey,
                                               ScriptType outputScriptType) throws UnreadableWalletException;

    /** @deprecated use {@link #makeWatchingKeyChain(DeterministicKey, ScriptType)} */
    @Deprecated
    default DeterministicKeyChain makeWatchingKeyChain(DeterministicKey accountKey, boolean isFollowingKey, boolean isMarried,
            ScriptType outputScriptType) throws UnreadableWalletException {
        check(!isMarried && !isFollowingKey, () -> { throw new UnsupportedOperationException("married wallets not supported"); });
        return makeWatchingKeyChain(accountKey, outputScriptType);
    }

    /**
     * Make a spending keychain.
     *
     * <p>isMarried and isFollowingKey must not be true at the same time.
     *
     * @param accountKey       the account extended public key
     * @param outputScriptType type of addresses (aka output scripts) to generate for spending
     */
    DeterministicKeyChain makeSpendingKeyChain(DeterministicKey accountKey,
                                               ScriptType outputScriptType) throws UnreadableWalletException;

    /** @deprecated use {@link #makeSpendingKeyChain(DeterministicKey, ScriptType)} */
    @Deprecated
    default DeterministicKeyChain makeSpendingKeyChain(DeterministicKey accountKey, boolean isMarried,
                                                       ScriptType outputScriptType) throws UnreadableWalletException {
        check(!isMarried, () -> { throw new UnsupportedOperationException("married wallets not supported"); });
        return makeSpendingKeyChain(accountKey, outputScriptType);
    }
}
