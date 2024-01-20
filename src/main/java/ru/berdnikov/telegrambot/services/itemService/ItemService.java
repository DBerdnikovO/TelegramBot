package ru.berdnikov.telegrambot.services.itemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berdnikov.telegrambot.dto.ItemDTO;
import ru.berdnikov.telegrambot.entity.Item;
import ru.berdnikov.telegrambot.entity.Person;
import ru.berdnikov.telegrambot.repositories.ItemRepository;

import java.util.List;
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

    public void addItem(ItemDTO itemDTO, Person person){
        itemRepository.save(convertToItem(itemDTO, person));
    }

    @Transactional(readOnly = true)
    public List<ItemDTO> getItemsByPersonId(int id){
        return itemRepository.findAllByOwnerId(id).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private ItemDTO convertToResponseDTO(Item item){
        return new ItemDTO(item.getTitle(), item.getPrice(), item.getImage(), item.getLink());
    }

    private Item convertToItem(ItemDTO itemDTO, Person person){
        return new Item(itemDTO.getTitle(),
                itemDTO.getImage_url(),
                itemDTO.getLink(),
                itemDTO.getPrice(), person);
    }
}
