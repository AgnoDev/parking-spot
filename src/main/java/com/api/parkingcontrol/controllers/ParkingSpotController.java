package com.api.parkingcontrol.controllers;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {
    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
        if (parkingSpotService.existsByLicensePlaceCar(parkingSpotDto.getLicensePlaceCar())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Licence Place Car in use!");
        }
        if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Parking Spot Number in use!");
        }
        if (parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Apartment and Block in use!");
        }
        var parkingSpotModel = new ParkingSpotModel();  // substitui "ParkingSpotModel parkingSpotModel = new ParkingSpotModel();"
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel); // use para converter o 'dto' em um 'model'
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));  // use para registrar a data automaticamente, pois esse campo não será preenchido no dto
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));   //no body vem o retorno que será construído no 'save'
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpot // o campo 'sort' pode ser qualquer atributo: id, data de registro...
            (@PageableDefault(page = 0, size = 10, sort = "apartment", direction = Sort.Direction.DESC)Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Object> getOneParkingSpot(@PathVariable (value = "id") UUID id){
//        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
//        if(!parkingSpotModelOptional.isPresent()){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
//        }
//        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        return parkingSpotModelOptional
                .<ResponseEntity<Object>>map(parkingSpotModel -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(parkingSpotModel))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if (!parkingSpotModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }
        parkingSpotService.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,
                                                    @RequestBody @Valid ParkingSpotDto parkingSpotDto){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if (!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel); //nesse caso eu tenho que setar os atributos que não vieram no dto
        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    }
}








