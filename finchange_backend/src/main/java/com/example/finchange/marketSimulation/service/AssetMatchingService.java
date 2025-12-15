package com.example.finchange.marketSimulation.service;

public interface AssetMatchingService {

    void findAndWriteAllMatchesToRedis();

    void findAndWriteSingleMatchToRedis(String bistCode);
}
