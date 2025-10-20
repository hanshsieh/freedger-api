package org.freedger.service;

import javax.inject.Inject;

import org.freedger.repository.ditto.DittoClient;

public class LedgerService {
  private final DittoClient dittoClient;
  
  @Inject
  public LedgerService(DittoClient dittoClient) {
    this.dittoClient = dittoClient;
  }
}
