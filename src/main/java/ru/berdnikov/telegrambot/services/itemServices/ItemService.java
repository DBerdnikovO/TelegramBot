package ru.berdnikov.telegrambot.services.itemServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berdnikov.telegrambot.dto.ResponseDTO;
import ru.berdnikov.telegrambot.entity.Item;
import ru.berdnikov.telegrambot.entity.Person;
import ru.berdnikov.telegrambot.repositories.ItemRepository;
import ru.berdnikov.telegrambot.repositories.PeopleRepository;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Item> getItems() {
        return itemRepository.findAll();
    }

    public void addItem(ResponseDTO responseDTO, Person person){
        itemRepository.save(convertToItem(responseDTO, person));
    }

    public List<ResponseDTO> getItemsByPersonId(int id){
        return itemRepository.findAllByOwnerId(id).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private ResponseDTO convertToResponseDTO(Item item){
        return new ResponseDTO(item.getTitle(), item.getPrice(), item.getImage(), item.getLink());
    }

    private Item convertToItem(ResponseDTO responseDTO, Person person){
        return new Item(responseDTO.getTitle(),
                responseDTO.getImage_url(),
                responseDTO.getLink(),
                responseDTO.getPrice(), person);
    }
}
