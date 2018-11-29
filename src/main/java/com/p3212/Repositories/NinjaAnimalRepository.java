package com.p3212.Repositories;

import com.p3212.EntityClasses.NinjaAnimal;
import com.p3212.EntityClasses.NinjaAnimalRace;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NinjaAnimalRepository extends CrudRepository<NinjaAnimal, String> {
    @Query("select n from NinjaAnimal n where n.race = :rc")
    List<NinjaAnimal>getRaceAnimals(@Param("rc") NinjaAnimalRace race);
}
