package com.p3212.Repositories;

import com.p3212.EntityClasses.FightVsAI;
import com.p3212.EntityClasses.UserAIFight;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FightVsAIRepository extends CrudRepository<FightVsAI, Integer> {
    @Query("SELECT uf FROM FightVsAI f inner join UserAIFight uf WHERE uf.id = :id")
    List<UserAIFight> getAIFightsByUser(@Param("id") int id);
}
