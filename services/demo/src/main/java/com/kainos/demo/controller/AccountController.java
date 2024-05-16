package com.kainos.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kainos.demo.vendors.sfdc.SfdcService;

import jakarta.validation.Valid;

@RestController
public class AccountController {

    @Autowired
    private SfdcService sfdcService;

    @PostMapping("/accounts/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAccount(@Valid @PathVariable String name) {
        sfdcService.createAccount(name);
    }
}
