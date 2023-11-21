/*
 * Copyright 2013 Google Inc.
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

package org.litecoinj.testing;

import org.litecoinj.base.LitecoinNetwork;
import org.litecoinj.base.ScriptType;
import org.litecoinj.base.internal.TimeUtils;
import org.litecoinj.core.AbstractBlockChain;
import org.litecoinj.base.Address;
import org.litecoinj.core.Block;
import org.litecoinj.core.BlockChain;
import org.litecoinj.base.Coin;
import org.litecoinj.core.Context;
import org.litecoinj.crypto.ECKey;
import org.litecoinj.core.NetworkParameters;
import org.litecoinj.core.Transaction;
import org.litecoinj.core.VerificationException;
import org.litecoinj.params.MainNetParams;
import org.litecoinj.params.TestNet3Params;
import org.litecoinj.store.BlockStore;
import org.litecoinj.store.MemoryBlockStore;
import org.litecoinj.utils.BriefLogFormatter;
import org.litecoinj.wallet.KeyChainGroupStructure;
import org.litecoinj.wallet.Wallet;
import org.junit.BeforeClass;

import javax.annotation.Nullable;

import static org.litecoinj.testing.FakeTxBuilder.createFakeBlock;
import static org.litecoinj.testing.FakeTxBuilder.createFakeTx;

// TODO: This needs to be somewhat rewritten - the "sendMoneyToWallet" methods aren't sending via the block chain object

/**
 * A utility class that you can derive from in your unit tests. TestWithWallet sets up an empty wallet,
 * an in-memory block store and a block chain object. It also provides helper methods for filling the wallet
 * with money in whatever ways you wish. Note that for simplicity with amounts, this class sets the default
 * fee per kilobyte to zero in setUp.
 */
public class TestWithWallet {
    protected static final NetworkParameters TESTNET = TestNet3Params.get();
    protected static final NetworkParameters MAINNET = MainNetParams.get();

    protected ECKey myKey;
    protected Address myAddress;
    protected Wallet wallet;
    protected BlockChain chain;
    protected BlockStore blockStore;

    @BeforeClass
    public static void setUpClass() throws Exception {
        TimeUtils.clearMockClock();
    }

    public void setUp() throws Exception {
        BriefLogFormatter.init();
        Context.propagate(new Context(100, Coin.ZERO, false, true));
        wallet = Wallet.createDeterministic(LitecoinNetwork.TESTNET, ScriptType.P2PKH, KeyChainGroupStructure.BIP32);
        myKey = wallet.freshReceiveKey();
        myAddress = wallet.freshReceiveAddress(ScriptType.P2PKH);
        blockStore = new MemoryBlockStore(TESTNET.getGenesisBlock());
        chain = new BlockChain(TESTNET, wallet, blockStore);
    }

    public void tearDown() throws Exception {
    }

    @Nullable
    protected Transaction sendMoneyToWallet(Wallet wallet, AbstractBlockChain.NewBlockType type, Transaction... transactions)
            throws VerificationException {
        if (type == null) {
            // Pending transaction
            for (Transaction tx : transactions)
                if (wallet.isPendingTransactionRelevant(tx))
                    wallet.receivePending(tx, null);
        } else {
            FakeTxBuilder.BlockPair bp = createFakeBlock(blockStore, Block.BLOCK_HEIGHT_GENESIS, transactions);
            for (Transaction tx : transactions)
                wallet.receiveFromBlock(tx, bp.storedBlock, type, 0);
            if (type == AbstractBlockChain.NewBlockType.BEST_CHAIN)
                wallet.notifyNewBestBlock(bp.storedBlock);
        }
        if (transactions.length == 1)
            return wallet.getTransaction(transactions[0].getTxId());  // Can be null if tx is a double spend that's otherwise irrelevant.
        else
            return null;
    }

    @Nullable
    protected Transaction sendMoneyToWallet(Wallet wallet, AbstractBlockChain.NewBlockType type, Coin value, Address toAddress) throws VerificationException {
        return sendMoneyToWallet(wallet, type, createFakeTx(TESTNET.network(), value, toAddress));
    }

    @Nullable
    protected Transaction sendMoneyToWallet(Wallet wallet, AbstractBlockChain.NewBlockType type, Coin value, ECKey toPubKey) throws VerificationException {
        return sendMoneyToWallet(wallet, type, createFakeTx(value, toPubKey));
    }

    @Nullable
    protected Transaction sendMoneyToWallet(AbstractBlockChain.NewBlockType type, Transaction... transactions) throws VerificationException {
        return sendMoneyToWallet(this.wallet, type, transactions);
    }

    @Nullable
    protected Transaction sendMoneyToWallet(AbstractBlockChain.NewBlockType type, Coin value) throws VerificationException {
        return sendMoneyToWallet(this.wallet, type, value, myAddress);
    }

    @Nullable
    protected Transaction sendMoneyToWallet(AbstractBlockChain.NewBlockType type, Coin value, Address toAddress) throws VerificationException {
        return sendMoneyToWallet(this.wallet, type, value, toAddress);
    }

    @Nullable
    protected Transaction sendMoneyToWallet(AbstractBlockChain.NewBlockType type, Coin value, ECKey toPubKey) throws VerificationException {
        return sendMoneyToWallet(this.wallet, type, value, toPubKey);
    }
}
