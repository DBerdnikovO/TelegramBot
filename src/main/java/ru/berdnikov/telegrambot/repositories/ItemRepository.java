package ru.berdnikov.telegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.berdnikov.telegrambot.entity.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findAllByOwnerId(int personId);
}
