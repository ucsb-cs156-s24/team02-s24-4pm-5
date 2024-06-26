package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/UCSBDiningCommonsMenuItem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {
    
    @Autowired
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @Operation(summary= "List all ucsb dining commons menu items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItem() {
        Iterable<UCSBDiningCommonsMenuItem> commonsMenuItems = ucsbDiningCommonsMenuItemRepository.findAll();
        return commonsMenuItems;
    }

    @Operation(summary= "Create a new commons menu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
        @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode,
        @Parameter(name="name") @RequestParam String name,
        @Parameter(name="station") @RequestParam String station
        )
        throws JsonProcessingException {

        UCSBDiningCommonsMenuItem commonsMenuItem = new UCSBDiningCommonsMenuItem();
        commonsMenuItem.setDiningCommonsCode(diningCommonsCode);
        commonsMenuItem.setName(name);
        commonsMenuItem.setStation(station);

        UCSBDiningCommonsMenuItem savedMenuItems = ucsbDiningCommonsMenuItemRepository.save(commonsMenuItem);

        return savedMenuItems;
    }

    @Operation(summary= "Get a single dining commons menu items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItem getById(
            @Parameter(name="id") @RequestParam Long id) {
            UCSBDiningCommonsMenuItem commonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        return commonsMenuItem;
    }

    @Operation(summary= "Update a single dining commons menu items")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenuItem updatedMenuItem) {

        UCSBDiningCommonsMenuItem commonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        commonsMenuItem.setDiningCommonsCode(updatedMenuItem.getDiningCommonsCode());
        commonsMenuItem.setName(updatedMenuItem.getName());
        commonsMenuItem.setStation(updatedMenuItem.getStation());

        ucsbDiningCommonsMenuItemRepository.save(commonsMenuItem);

        return commonsMenuItem;
    }

    @Operation(summary= "Delete a dining commons menu items")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBDiningCommonsMenuItem(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem commonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbDiningCommonsMenuItemRepository.delete(commonsMenuItem);
        return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
    }
}