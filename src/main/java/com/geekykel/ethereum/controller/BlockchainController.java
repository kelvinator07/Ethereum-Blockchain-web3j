package com.geekykel.ethereum.controller;

import com.geekykel.ethereum.model.BlockchainTransaction;
import com.geekykel.ethereum.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class BlockchainController {

    @Autowired
    private BlockchainService service;

    @PostMapping("/transaction")
    public BlockchainTransaction execute(@RequestBody BlockchainTransaction transaction)
            throws IOException {
        return service.process(transaction);
    }

}
