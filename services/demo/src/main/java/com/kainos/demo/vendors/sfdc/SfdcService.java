package com.kainos.demo.vendors.sfdc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sfdc.account.model.Account;

import sfdc.client.SfdcAuthenticatedClient;

@Service
public class SfdcService {

    @Autowired
    private SfdcAuthenticatedClient sfdcClient;

    public void createAccount(String name) {
        Account account = new Account().name(name);
        sfdcClient.createNewAccount(account);
    }
}
