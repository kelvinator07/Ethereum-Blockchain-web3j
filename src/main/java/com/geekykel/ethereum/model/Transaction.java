package com.geekykel.ethereum.model;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Numeric;

public class Transaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);

    public static void main(String[] args) {

        LOGGER.info("Connecting to Ethereum ...");
        Web3j web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/083836b2784f48e19e03487eb3209923"));
        LOGGER.info("Successfuly connected to Ethereum");

        try {
            String pk = "0d28da19791f465f65cc61bd35f7e78855c4cb3a4aaabfa05c39a0f08b5b0060"; // Add a private key here

            // Decrypt and open the wallet into a Credential object
            Credentials credentials = Credentials.create(pk);
            LOGGER.info("Account address: {}", credentials.getAddress());
            LOGGER.info("Balance: {}", Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Unit.ETHER));

            // Get the latest nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            // Recipient address
            String recipientAddress = "0xAA6325C45aE6fAbD028D19fa1539663Df14813a8";

            // Value to transfer (in wei)
            BigInteger value = Convert.toWei("1", Unit.ETHER).toBigInteger();

            // Gas Parameters
            BigInteger gasLimit = BigInteger.valueOf(21000);
            BigInteger gasPrice = Convert.toWei("1", Unit.GWEI).toBigInteger();

            // Prepare the rawTransaction
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    recipientAddress,
                    value);

            // Sign the transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // Send transaction
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
            String transactionHash = ethSendTransaction.getTransactionHash();
            LOGGER.info("transactionHash: {}", transactionHash);

            // Wait for transaction to be mined
            Optional<TransactionReceipt> transactionReceipt = null;
            do {
                LOGGER.info("checking if transaction {} is mined....", transactionHash);
                EthGetTransactionReceipt ethGetTransactionReceiptResp = web3.ethGetTransactionReceipt(transactionHash).send();
                transactionReceipt = ethGetTransactionReceiptResp.getTransactionReceipt();
                Thread.sleep(3000); // Wait 3 sec
            } while (!transactionReceipt.isPresent());

            LOGGER.info("Transaction {} was mined in block # {}", transactionHash, transactionReceipt.get().getBlockNumber());
            LOGGER.info("Balance: {}", Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Unit.ETHER));


        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }
}
