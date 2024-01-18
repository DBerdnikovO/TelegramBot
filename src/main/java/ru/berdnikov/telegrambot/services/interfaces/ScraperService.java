package ru.berdnikov.telegrambot.services.interfaces;


import ru.berdnikov.telegrambot.dto.ResponseDTO;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ScraperService {
    List<ResponseDTO> fetchMoney(String money) throws UnsupportedEncodingException;
    ResponseDTO saveByLink(String url);
}
