package Repositories;

import EntityClasses.FightPVP;
import EntityClasses.PVPFightCompositeKey;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PVPFightsRepository extends CrudRepository<FightPVP, PVPFightCompositeKey> {
    @Query("SELECT f from FightPVP f where f.pvpId.firstFighter = :id OR f.pvpId.secondFighter = :id")
    List<FightPVP> getUsersPVPFights(@Param("id") int id);
}